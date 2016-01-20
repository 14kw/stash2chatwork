package com.pragbits.stash.components;

import com.atlassian.event.api.EventListener;
import com.atlassian.stash.event.pull.PullRequestActivityEvent;
import com.atlassian.stash.event.pull.PullRequestCommentActivityEvent;
import com.atlassian.stash.event.pull.PullRequestRescopeActivityEvent;
import com.atlassian.stash.nav.NavBuilder;
import com.atlassian.stash.pull.PullRequestParticipant;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.avatar.AvatarService;
import com.atlassian.stash.avatar.AvatarRequest;
import com.google.gson.Gson;
import com.pragbits.stash.*;
import com.pragbits.stash.tools.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class PullRequestActivityListener {
    static final String KEY_GLOBAL_SETTING_HOOK_URL = "stash2chatwork.globalsettings.hookurl";
    static final String KEY_GLOBAL_SLACK_CHANNEL_NAME = "stash2chatwork.globalsettings.channelname";
    private static final Logger log = LoggerFactory.getLogger(PullRequestActivityListener.class);

    private final ChatworkGlobalSettingsService chatworkGlobalSettingsService;
    private final ChatworkSettingsService chatworkSettingsService;
    private final NavBuilder navBuilder;
    private final ChatworkNotifier chatworkNotifier;
    private final AvatarService avatarService;
    private final AvatarRequest avatarRequest = new AvatarRequest(true, 16, true);
    private final Gson gson = new Gson();

    public PullRequestActivityListener(ChatworkGlobalSettingsService chatworkGlobalSettingsService,
                                             ChatworkSettingsService chatworkSettingsService,
                                             NavBuilder navBuilder,
                                             ChatworkNotifier chatworkNotifier,
                                             AvatarService avatarService) {
        this.chatworkGlobalSettingsService = chatworkGlobalSettingsService;
        this.chatworkSettingsService = chatworkSettingsService;
        this.navBuilder = navBuilder;
        this.chatworkNotifier = chatworkNotifier;
        this.avatarService = avatarService;
    }

    @EventListener
    public void NotifyChatworkChannel(PullRequestActivityEvent event) {
        // find out if notification is enabled for this repo
        Repository repository = event.getPullRequest().getToRef().getRepository();
        ChatworkSettings chatworkSettings = chatworkSettingsService.getChatworkSettings(repository);
        String globalHookUrl = chatworkGlobalSettingsService.getWebHookUrl(KEY_GLOBAL_SETTING_HOOK_URL);


        SettingsSelector settingsSelector = new SettingsSelector(chatworkSettingsService,  chatworkGlobalSettingsService, repository);
        ChatworkSettings resolvedChatworkSettings = settingsSelector.getResolvedChatworkSettings();

        if (resolvedChatworkSettings.isChatworkNotificationsEnabled()) {

            String localHookUrl = resolvedChatworkSettings.getChatworkWebHookUrl();
            WebHookSelector hookSelector = new WebHookSelector(globalHookUrl, localHookUrl);
            ChannelSelector channelSelector = new ChannelSelector(chatworkGlobalSettingsService.getChannelName(KEY_GLOBAL_SLACK_CHANNEL_NAME), chatworkSettings.getChatworkChannelName());

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
            String repoPath = projectName + "/" + repoName;
            long pullRequestId = event.getPullRequest().getId();
            String userName = event.getUser() != null ? event.getUser().getDisplayName() : "unknown user";
            String activity = event.getActivity().getAction().name();
            String avatar = event.getUser() != null ? avatarService.getUrlForPerson(event.getUser(), avatarRequest) : "";

            NotificationLevel resolvedLevel = resolvedChatworkSettings.getNotificationPrLevel();

            // Ignore RESCOPED PR events
            if (activity.equalsIgnoreCase("RESCOPED") && event instanceof PullRequestRescopeActivityEvent) {
                return;
            }

            if (activity.equalsIgnoreCase("OPENED") && !resolvedChatworkSettings.isChatworkNotificationsOpenedEnabled()) {
                return;
            }

            if (activity.equalsIgnoreCase("REOPENED") && !resolvedChatworkSettings.isChatworkNotificationsReopenedEnabled()) {
                return;
            }

            if (activity.equalsIgnoreCase("UPDATED") && !resolvedChatworkSettings.isChatworkNotificationsUpdatedEnabled()) {
                return;
            }

            if (activity.equalsIgnoreCase("APPROVED") && !resolvedChatworkSettings.isChatworkNotificationsApprovedEnabled()) {
                return;
            }

            if (activity.equalsIgnoreCase("UNAPPROVED") && !resolvedChatworkSettings.isChatworkNotificationsUnapprovedEnabled()) {
                return;
            }

            if (activity.equalsIgnoreCase("DECLINED") && !resolvedChatworkSettings.isChatworkNotificationsDeclinedEnabled()) {
                return;
            }

            if (activity.equalsIgnoreCase("MERGED") && !resolvedChatworkSettings.isChatworkNotificationsMergedEnabled()) {
                return;
            }

            if (activity.equalsIgnoreCase("COMMENTED") && !resolvedChatworkSettings.isChatworkNotificationsCommentedEnabled()) {
                return;
            }
            
            String url = navBuilder
                    .project(projectName)
                    .repo(repoName)
                    .pullRequest(pullRequestId)
                    .overview()
                    .buildAbsolute();

            ChatworkPayload payload = new ChatworkPayload();
            payload.setReqType("PR");
            payload.setLinkNames(true);

            ChatworkAttachment attachment = new ChatworkAttachment();
            attachment.setAuthorName(userName);
            attachment.setAuthorIcon(avatar);
            attachment.setTitle(String.format("[%s] #%d: %s",
                                                  repoPath,
                                                  event.getPullRequest().getId(),
                                                  event.getPullRequest().getTitle()));
            attachment.setTitle_link(url);
            attachment.setPrType(String.format("%s", event.getActivity().getAction()));

            String color = "";
            String fallback = "";
            String text = "";

            switch (event.getActivity().getAction()) {
                case OPENED:
                    attachment.setText(String.format("opened pull request #%d",
                                                            event.getPullRequest().getId()));


                    if (resolvedLevel == NotificationLevel.COMPACT) {
                        this.addField(attachment, "Description", event.getPullRequest().getDescription());
                    }

                    if (resolvedLevel == NotificationLevel.VERBOSE) {
                        this.addReviewers(attachment, event.getPullRequest().getReviewers());
                    }
                    break;

                case REOPENED:
                    attachment.setText(String.format("reopened pull request #%d",
                                                            event.getPullRequest().getId()));

                    if (resolvedLevel == NotificationLevel.COMPACT) {
                        this.addField(attachment, "Description", event.getPullRequest().getDescription());
                    }
                    if (resolvedLevel == NotificationLevel.VERBOSE) {
                        this.addReviewers(attachment, event.getPullRequest().getReviewers());
                    }
                    break;

                case UPDATED:
                    attachment.setText(String.format("updated pull request #%d",
                                                            event.getPullRequest().getId()));

                    if (resolvedLevel == NotificationLevel.COMPACT) {
                        this.addField(attachment, "Description", event.getPullRequest().getDescription());
                    }
                    if (resolvedLevel == NotificationLevel.VERBOSE) {
                        this.addReviewers(attachment, event.getPullRequest().getReviewers());
                    }
                    break;

                case APPROVED:
                    attachment.setText(String.format("approved pull request #%d",
                                                            event.getPullRequest().getId()));
                    break;

                case UNAPPROVED:
                    attachment.setText(String.format("unapproved pull request #%d",
                                                            event.getPullRequest().getId()));
                    break;

                case DECLINED:
                    attachment.setText(String.format("declined pull request #%d",
                                                            event.getPullRequest().getId()));
                    break;

                case MERGED:
                    attachment.setText(String.format("merged pull request #%d",
                                                            event.getPullRequest().getId()));
                    break;

                case RESCOPED:
                    attachment.setText(String.format("rescoped on pull request #%d",
                                                            event.getPullRequest().getId()));
                    break;

                case COMMENTED:
                    if (resolvedLevel == NotificationLevel.MINIMAL) {
                        attachment.setText(String.format("commented on pull request #%d",
                                event.getPullRequest().getId()));
                    }
                    if (resolvedLevel == NotificationLevel.COMPACT || resolvedLevel == NotificationLevel.VERBOSE) {
                        attachment.setText(String.format("commented on pull request #%d",
                                event.getPullRequest().getId()));
                        this.addField(attachment, "Comment", ((PullRequestCommentActivityEvent) event).getActivity().getComment().getText());
                    }
                    break;
            }

            if (resolvedLevel == NotificationLevel.VERBOSE) {
                ChatworkAttachmentField projectField = new ChatworkAttachmentField();
                projectField.setTitle("Source");
                projectField.setValue(String.format("%s — [%s:/%s]",
                        event.getPullRequest().getFromRef().getRepository().getProject().getName(),
                        event.getPullRequest().getFromRef().getRepository().getName(),
                        event.getPullRequest().getFromRef().getDisplayId()));
                projectField.setShort(true);
                attachment.addField(projectField);

                ChatworkAttachmentField repoField = new ChatworkAttachmentField();
                repoField.setTitle("Destination");
                repoField.setValue(String.format("%s — [%s:/%s]",
                        event.getPullRequest().getFromRef().getRepository().getProject().getName(),
                        event.getPullRequest().getToRef().getRepository().getName(),
                        event.getPullRequest().getToRef().getDisplayId()));
                repoField.setShort(true);
                attachment.addField(repoField);
            }

            payload.addAttachment(attachment);

            // chatworkSettings.getChatworkChannelName might be:
            // - empty
            // - comma separated list of channel names, eg: #mych1, #mych2, #mych3

            if (channelSelector.getSelectedChannel().isEmpty()) {
                chatworkNotifier.SendChatworkNotification(hookSelector.getSelectedHook(), gson.toJson(payload));
            } else {
                // send message to multiple channels
                List<String> channels = Arrays.asList(channelSelector.getSelectedChannel().split("\\s*,\\s*"));
                for (String channel: channels) {
                    payload.setChannel(channel.trim());
                    chatworkNotifier.SendChatworkNotification(hookSelector.getSelectedHook(), gson.toJson(payload));
                }
            }
        }

    }

    private void addField(ChatworkAttachment attachment, String title, String message) {
        ChatworkAttachmentField field = new ChatworkAttachmentField();
        field.setTitle(title);
        field.setValue(message);
        field.setShort(false);
        attachment.addField(field);
    }

    private void addReviewers(ChatworkAttachment attachment, Set<PullRequestParticipant> reviewers) {
        if (reviewers.isEmpty()) {
            return;
        }
        String names = "";
        for(PullRequestParticipant p : reviewers) {
            names += String.format("@%s ", p.getUser().getSlug());
        }
        this.addField(attachment, "Reviewers", names);
    }
}
