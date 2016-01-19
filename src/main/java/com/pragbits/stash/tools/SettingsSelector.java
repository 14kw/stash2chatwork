package com.pragbits.stash.tools;

import com.atlassian.stash.repository.Repository;
import com.pragbits.stash.ImmutableChatworkSettings;
import com.pragbits.stash.ChatworkGlobalSettingsService;
import com.pragbits.stash.ChatworkSettings;
import com.pragbits.stash.ChatworkSettingsService;

public class SettingsSelector {

    private ChatworkSettingsService chatworkSettingsService;
    private ChatworkGlobalSettingsService chatworkGlobalSettingsService;
    private ChatworkSettings chatworkSettings;
    private Repository repository;
    private ChatworkSettings resolvedChatworkSettings;


    private static final String KEY_GLOBAL_SETTING_HOOK_URL = "stash2chatwork.globalsettings.hookurl";
    private static final String KEY_GLOBAL_SETTING_CHANNEL_NAME = "stash2chatwork.globalsettings.channelname";
    private static final String KEY_GLOBAL_SETTING_NOTIFICATIONS_ENABLED = "stash2chatwork.globalsettings.chatworknotificationsenabled";
    private static final String KEY_GLOBAL_SETTING_NOTIFICATIONS_OPENED_ENABLED = "stash2chatwork.globalsettings.chatworknotificationsopenedenabled";
    private static final String KEY_GLOBAL_SETTING_NOTIFICATIONS_REOPENED_ENABLED = "stash2chatwork.globalsettings.chatworknotificationsreopenedenabled";
    private static final String KEY_GLOBAL_SETTING_NOTIFICATIONS_UPDATED_ENABLED = "stash2chatwork.globalsettings.chatworknotificationsupdatedenabled";
    private static final String KEY_GLOBAL_SETTING_NOTIFICATIONS_APPROVED_ENABLED = "stash2chatwork.globalsettings.chatworknotificationsapprovedenabled";
    private static final String KEY_GLOBAL_SETTING_NOTIFICATIONS_UNAPPROVED_ENABLED = "stash2chatwork.globalsettings.chatworknotificationsunapprovedenabled";
    private static final String KEY_GLOBAL_SETTING_NOTIFICATIONS_DECLINED_ENABLED = "stash2chatwork.globalsettings.chatworknotificationsdeclinedenabled";
    private static final String KEY_GLOBAL_SETTING_NOTIFICATIONS_MERGED_ENABLED = "stash2chatwork.globalsettings.chatworknotificationsmergedenabled";
    private static final String KEY_GLOBAL_SETTING_NOTIFICATIONS_COMMENTED_ENABLED = "stash2chatwork.globalsettings.chatworknotificationscommentedenabled";
    private static final String KEY_GLOBAL_SETTING_NOTIFICATIONS_LEVEL = "stash2chatwork.globalsettings.chatworknotificationslevel";
    private static final String KEY_GLOBAL_SETTING_NOTIFICATIONS_PR_LEVEL = "stash2chatwork.globalsettings.chatworknotificationsprlevel";
    private static final String KEY_GLOBAL_SETTING_NOTIFICATIONS_PUSH_ENABLED = "stash2chatwork.globalsettings.chatworknotificationspushenabled";
    private static final String KEY_GLOBAL_SETTING_NOTIFICATIONS_PERSONAL_ENABLED = "stash2chatwork.globalsettings.chatworknotificationspersonalenabled";


    public SettingsSelector(ChatworkSettingsService chatworkSettingsService, ChatworkGlobalSettingsService chatworkGlobalSettingsService, Repository repository) {
        this.chatworkSettingsService = chatworkSettingsService;
        this.chatworkGlobalSettingsService = chatworkGlobalSettingsService;
        this.repository = repository;
        this.chatworkSettings = chatworkSettingsService.getChatworkSettings(repository);
        this.setResolvedChatworkSettings();
    }

    public ChatworkSettings getResolvedChatworkSettings() {
        return this.resolvedChatworkSettings;
    }

    private void setResolvedChatworkSettings() {
        resolvedChatworkSettings = new ImmutableChatworkSettings(
                chatworkSettings.isChatworkNotificationsOverrideEnabled() ? true : false,
                chatworkSettings.isChatworkNotificationsOverrideEnabled() ? chatworkSettings.isChatworkNotificationsEnabled() : chatworkGlobalSettingsService.getChatworkNotificationsEnabled(KEY_GLOBAL_SETTING_NOTIFICATIONS_ENABLED),
                chatworkSettings.isChatworkNotificationsOverrideEnabled() ? chatworkSettings.isChatworkNotificationsOpenedEnabled() : chatworkGlobalSettingsService.getChatworkNotificationsOpenedEnabled(KEY_GLOBAL_SETTING_NOTIFICATIONS_OPENED_ENABLED),
                chatworkSettings.isChatworkNotificationsOverrideEnabled() ? chatworkSettings.isChatworkNotificationsReopenedEnabled() : chatworkGlobalSettingsService.getChatworkNotificationsReopenedEnabled(KEY_GLOBAL_SETTING_NOTIFICATIONS_REOPENED_ENABLED),
                chatworkSettings.isChatworkNotificationsOverrideEnabled() ? chatworkSettings.isChatworkNotificationsUpdatedEnabled() : chatworkGlobalSettingsService.getChatworkNotificationsUpdatedEnabled(KEY_GLOBAL_SETTING_NOTIFICATIONS_UPDATED_ENABLED),
                chatworkSettings.isChatworkNotificationsOverrideEnabled() ? chatworkSettings.isChatworkNotificationsApprovedEnabled() : chatworkGlobalSettingsService.getChatworkNotificationsApprovedEnabled(KEY_GLOBAL_SETTING_NOTIFICATIONS_APPROVED_ENABLED),
                chatworkSettings.isChatworkNotificationsOverrideEnabled() ? chatworkSettings.isChatworkNotificationsUnapprovedEnabled() : chatworkGlobalSettingsService.getChatworkNotificationsUnapprovedEnabled(KEY_GLOBAL_SETTING_NOTIFICATIONS_UNAPPROVED_ENABLED),
                chatworkSettings.isChatworkNotificationsOverrideEnabled() ? chatworkSettings.isChatworkNotificationsDeclinedEnabled() : chatworkGlobalSettingsService.getChatworkNotificationsDeclinedEnabled(KEY_GLOBAL_SETTING_NOTIFICATIONS_DECLINED_ENABLED),
                chatworkSettings.isChatworkNotificationsOverrideEnabled() ? chatworkSettings.isChatworkNotificationsMergedEnabled() : chatworkGlobalSettingsService.getChatworkNotificationsMergedEnabled(KEY_GLOBAL_SETTING_NOTIFICATIONS_MERGED_ENABLED),
                chatworkSettings.isChatworkNotificationsOverrideEnabled() ? chatworkSettings.isChatworkNotificationsCommentedEnabled() : chatworkGlobalSettingsService.getChatworkNotificationsCommentedEnabled(KEY_GLOBAL_SETTING_NOTIFICATIONS_COMMENTED_ENABLED),
                chatworkSettings.isChatworkNotificationsOverrideEnabled() ? chatworkSettings.isChatworkNotificationsEnabledForPush() : chatworkGlobalSettingsService.getChatworkNotificationsEnabledForPush(KEY_GLOBAL_SETTING_NOTIFICATIONS_PUSH_ENABLED),
                chatworkSettings.isChatworkNotificationsOverrideEnabled() ? chatworkSettings.isChatworkNotificationsEnabledForPersonal() : chatworkGlobalSettingsService.getChatworkNotificationsEnabledForPersonal(KEY_GLOBAL_SETTING_NOTIFICATIONS_PERSONAL_ENABLED),
                chatworkSettings.isChatworkNotificationsOverrideEnabled() ? chatworkSettings.getNotificationLevel() : chatworkGlobalSettingsService.getNotificationLevel(KEY_GLOBAL_SETTING_NOTIFICATIONS_LEVEL),
                chatworkSettings.isChatworkNotificationsOverrideEnabled() ? chatworkSettings.getNotificationPrLevel() : chatworkGlobalSettingsService.getNotificationPrLevel(KEY_GLOBAL_SETTING_NOTIFICATIONS_PR_LEVEL),
                chatworkSettings.isChatworkNotificationsOverrideEnabled() ? chatworkSettings.getChatworkChannelName() : chatworkGlobalSettingsService.getChannelName(KEY_GLOBAL_SETTING_CHANNEL_NAME),
                chatworkSettings.isChatworkNotificationsOverrideEnabled() ? chatworkSettings.getChatworkWebHookUrl() : chatworkGlobalSettingsService.getWebHookUrl(KEY_GLOBAL_SETTING_HOOK_URL)
        );
    }

}
