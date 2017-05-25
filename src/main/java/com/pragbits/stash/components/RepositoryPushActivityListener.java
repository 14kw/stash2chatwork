package com.pragbits.stash.components;

import com.atlassian.event.api.EventListener;
import com.atlassian.stash.commit.CommitService;
import com.atlassian.stash.content.Changeset;
import com.atlassian.stash.content.ChangesetsBetweenRequest;
import com.atlassian.stash.event.RepositoryPushEvent;
import com.atlassian.stash.nav.NavBuilder;
import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.repository.RefChangeType;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.util.Page;
import com.atlassian.stash.util.PageRequest;
import com.atlassian.stash.util.PageUtils;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.pragbits.stash.ColorCode;
import com.pragbits.stash.ChatworkGlobalSettingsService;
import com.pragbits.stash.ChatworkSettings;
import com.pragbits.stash.ChatworkSettingsService;
import com.pragbits.stash.tools.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class RepositoryPushActivityListener {
    static final String KEY_GLOBAL_SETTING_HOOK_URL = "stash2chatwork.globalsettings.hookurl";
    private static final Logger log = LoggerFactory.getLogger(RepositoryPushActivityListener.class);

    private final ChatworkGlobalSettingsService chatworkGlobalSettingsService;
    private final ChatworkSettingsService chatworkSettingsService;
    private final CommitService commitService;
    private final NavBuilder navBuilder;
    private final ChatworkNotifier chatworkNotifier;
    private final Gson gson = new Gson();

    public RepositoryPushActivityListener(ChatworkGlobalSettingsService chatworkGlobalSettingsService,
                                          ChatworkSettingsService chatworkSettingsService,
                                          CommitService commitService,
                                          NavBuilder navBuilder,
                                          ChatworkNotifier chatworkNotifier) {
        this.chatworkGlobalSettingsService = chatworkGlobalSettingsService;
        this.chatworkSettingsService = chatworkSettingsService;
        this.commitService = commitService;
        this.navBuilder = navBuilder;
        this.chatworkNotifier = chatworkNotifier;
    }

    @EventListener
    public void NotifyChatworkChannel(RepositoryPushEvent event) {
        // find out if notification is enabled for this repo
        Repository repository = event.getRepository();
        ChatworkSettings chatworkSettings = chatworkSettingsService.getChatworkSettings(repository);
        String globalHookUrl = chatworkGlobalSettingsService.getWebHookUrl(KEY_GLOBAL_SETTING_HOOK_URL);

        SettingsSelector settingsSelector = new SettingsSelector(chatworkSettingsService,  chatworkGlobalSettingsService, repository);
        ChatworkSettings resolvedChatworkSettings = settingsSelector.getResolvedChatworkSettings();

        if (resolvedChatworkSettings.isChatworkNotificationsEnabledForPush()) {
            String localHookUrl = chatworkSettings.getChatworkWebHookUrl();
            WebHookSelector hookSelector = new WebHookSelector(globalHookUrl, localHookUrl);

            if (!hookSelector.isHookValid()) {
                log.error("There is no valid configured Web hook url! Reason: " + hookSelector.getProblem());
                return;
            }

            if (repository.isFork() && !resolvedChatworkSettings.isChatworkNotificationsEnabledForPersonal()) {
                // simply return silently when we don't want forks to get notifications unless they're explicitly enabled
                return;
            }

            String repoName = repository.getSlug();
            String projectName = repository.getProject().getKey();

            String repoPath = projectName + "/" + event.getRepository().getName();

            for (RefChange refChange : event.getRefChanges()) {
                String text;
                String ref = refChange.getRefId();
                NavBuilder.Repo repoUrlBuilder = navBuilder
                        .project(projectName)
                        .repo(repoName);
                String url = repoUrlBuilder
                        .commits()
                        .until(refChange.getRefId())
                        .buildAbsolute();

                List<Changeset> myChanges = new LinkedList<Changeset>();
                boolean isNewRef = refChange.getFromHash().equalsIgnoreCase("0000000000000000000000000000000000000000");
                boolean isDeleted = refChange.getToHash().equalsIgnoreCase("0000000000000000000000000000000000000000")
                    && refChange.getType() == RefChangeType.DELETE;
                if (isDeleted) {
                    // issue#4: if type is "DELETE" and toHash is all zero then this is a branch delete
                    if (ref.indexOf("refs/tags") >= 0) {
                        text = String.format("Tag [%s] deleted from repository `%s`.",
                                ref.replace("refs/tags/", ""),
                                repoPath);
                    } else {
                        text = String.format("Branch [%s] deleted from repository `%s`.",
                                ref.replace("refs/heads/", ""),
                                repoPath);
                    }
                } else if (isNewRef) {
                    // issue#3 if fromHash is all zero (meaning the beginning of everything, probably), then this push is probably
                    // a new branch or tag, and we want only to display the latest commit, not the entire history
                    Changeset latestChangeSet = commitService.getChangeset(repository, refChange.getToHash());
                    myChanges.add(latestChangeSet);
                    if (ref.indexOf("refs/tags") >= 0) {
                        text = String.format("Tag [%s] pushed on `%s`.",
                                ref.replace("refs/tags/", ""),
                                repoPath
                                );
                    } else {
                        text = String.format("Branch [%s] pushed on `%s`.",
                                ref.replace("refs/heads/", ""),
                                repoPath
                                );
                    }
                } else {
                    ChangesetsBetweenRequest request = new ChangesetsBetweenRequest.Builder(repository)
                            .exclude(refChange.getFromHash())
                            .include(refChange.getToHash())
                            .build();

                    Page<Changeset> changeSets = commitService.getChangesetsBetween(
                            request, PageUtils.newRequest(0, PageRequest.MAX_PAGE_LIMIT));

                    myChanges.addAll(Lists.newArrayList(changeSets.getValues()));

                    int commitCount = myChanges.size();
                    String commitStr = commitCount == 1 ? "commit" : "commits";

                    String branch = ref.replace("refs/heads/", "");
                    text = String.format("Push on [%s] branch [%s] \n by `%s %s` (%d %s).",
                            repoPath,
                            branch,
                            event.getUser() != null ? event.getUser().getDisplayName() : "unknown user",
                            event.getUser() != null ? event.getUser().getEmailAddress() : "unknown email",
                            commitCount, commitStr
                            );
                }

                // Figure out what type of change this is:


                ChatworkPayload payload = new ChatworkPayload();

                payload.setText(text);
                payload.setReqType("Push");

                switch (resolvedChatworkSettings.getNotificationLevel()) {
                    case COMPACT:
                        compactCommitLog(event, refChange, payload, repoUrlBuilder, myChanges);
                        break;
                    case VERBOSE:
                        verboseCommitLog(event, refChange, payload, repoUrlBuilder, text, myChanges);
                        break;
                    case MINIMAL:
                    default:
                        break;
                }

                // chatworkSettings.getChatworkChannelName might be:
                // - empty
                // - comma separated list of channel names, eg: #mych1, #mych2, #mych3

                if (channelSelector.getSelectedChannel().isNotEmpty()) {
                    chatworkNotifier.SendChatworkNotification(hookSelector.getSelectedHook(), gson.toJson(payload));
                }
            }
        }
    }

    private void compactCommitLog(RepositoryPushEvent event, RefChange refChange, ChatworkPayload payload, NavBuilder.Repo urlBuilder, List<Changeset> myChanges) {
        if (myChanges.size() == 0) {
            // If there are no commits, no reason to add anything
        }
        ChatworkAttachment commits = new ChatworkAttachment();
        commits.setColor(ColorCode.GRAY.getCode());
        // Since the branch is now in the main commit line, title is not needed
        //commits.setTitle(String.format("[%s:%s]", event.getRepository().getName(), refChange.getRefId().replace("refs/heads", "")));
        StringBuilder attachmentFallback = new StringBuilder();
        StringBuilder commitListBlock = new StringBuilder();
        for (Changeset ch : myChanges) {
            String commitUrl = urlBuilder.changeset(ch.getId()).buildAbsolute();
            String firstCommitMessageLine = ch.getMessage().split("\n")[0];

            // Note that we changed this to put everything in one attachment because otherwise it
            // doesn't get collapsed in chatwork (the see more... doesn't appear)
            commitListBlock.append(String.format("`%s`: %s - _%s_\n%s\n",
                    ch.getDisplayId(), firstCommitMessageLine, ch.getAuthor().getName(), commitUrl));

            attachmentFallback.append(String.format("%s: %s\n", ch.getDisplayId(), firstCommitMessageLine));
        }
        commits.setText(commitListBlock.toString());
        commits.setFallback(attachmentFallback.toString());

        payload.addAttachment(commits);
    }

    private void verboseCommitLog(RepositoryPushEvent event, RefChange refChange, ChatworkPayload payload, NavBuilder.Repo urlBuilder, String text, List<Changeset> myChanges) {
        for (Changeset ch : myChanges) {
            ChatworkAttachment attachment = new ChatworkAttachment();
            attachment.setFallback(text);
            attachment.setColor(ColorCode.GRAY.getCode());
            ChatworkAttachmentField field = new ChatworkAttachmentField();

            attachment.setTitle(String.format("[%s:%s] - %s", event.getRepository().getName(), refChange.getRefId().replace("refs/heads", ""), ch.getId()));
            attachment.setTitle_link(urlBuilder.changeset(ch.getId()).buildAbsolute());

            field.setTitle("comment");
            field.setValue(ch.getMessage());
            field.setShort(false);
            attachment.addField(field);
            payload.addAttachment(attachment);
        }
    }

}
