package com.pragbits.stash;

public interface ChatworkSettings {

    boolean isChatworkNotificationsOverrideEnabled();
    boolean isChatworkNotificationsEnabled();
    boolean isChatworkNotificationsOpenedEnabled();
    boolean isChatworkNotificationsReopenedEnabled();
    boolean isChatworkNotificationsUpdatedEnabled();
    boolean isChatworkNotificationsApprovedEnabled();
    boolean isChatworkNotificationsUnapprovedEnabled();
    boolean isChatworkNotificationsDeclinedEnabled();
    boolean isChatworkNotificationsMergedEnabled();
    boolean isChatworkNotificationsCommentedEnabled();
    boolean isChatworkNotificationsEnabledForPush();
    boolean isChatworkNotificationsEnabledForPersonal();
    NotificationLevel getNotificationLevel();
    NotificationLevel getNotificationPrLevel();
    String getChatworkChannelName();
    String getChatworkWebHookUrl();

}
