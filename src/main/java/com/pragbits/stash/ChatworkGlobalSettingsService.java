package com.pragbits.stash;

public interface ChatworkGlobalSettingsService {

    // hook and channel name
    String getWebHookUrl(String key);
    void setWebHookUrl(String key, String value);

    String getChannelName(String key);
    void setChannelName(String key, String value);

    // pull requests are enabled and pr events
    boolean getChatworkNotificationsEnabled(String key);
    void setChatworkNotificationsEnabled(String key, String value);

    boolean getChatworkNotificationsOpenedEnabled(String key);
    void setChatworkNotificationsOpenedEnabled(String key, String value);

    boolean getChatworkNotificationsReopenedEnabled(String key);
    void setChatworkNotificationsReopenedEnabled(String key, String value);

    boolean getChatworkNotificationsUpdatedEnabled(String key);
    void setChatworkNotificationsUpdatedEnabled(String key, String value);

    boolean getChatworkNotificationsApprovedEnabled(String key);
    void setChatworkNotificationsApprovedEnabled(String key, String value);

    boolean getChatworkNotificationsUnapprovedEnabled(String key);
    void setChatworkNotificationsUnapprovedEnabled(String key, String value);

    boolean getChatworkNotificationsDeclinedEnabled(String key);
    void setChatworkNotificationsDeclinedEnabled(String key, String value);

    boolean getChatworkNotificationsMergedEnabled(String key);
    void setChatworkNotificationsMergedEnabled(String key, String value);

    boolean getChatworkNotificationsCommentedEnabled(String key);
    void setChatworkNotificationsCommentedEnabled(String key, String value);

    // push notifications are enabled and push options
    boolean getChatworkNotificationsEnabledForPush(String key);
    void setChatworkNotificationsEnabledForPush(String key, String value);

    NotificationLevel getNotificationLevel(String key);
    void setNotificationLevel(String key, String value);

    NotificationLevel getNotificationPrLevel(String key);
    void setNotificationPrLevel(String key, String value);

    boolean getChatworkNotificationsEnabledForPersonal(String key);
    void setChatworkNotificationsEnabledForPersonal(String key, String value);


}
