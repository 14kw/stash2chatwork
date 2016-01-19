package com.pragbits.stash;


import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.apache.xpath.operations.Bool;

public class DefaultGlobalChatworkSettingsService implements ChatworkGlobalSettingsService {

    private final PluginSettings pluginSettings;

    public DefaultGlobalChatworkSettingsService(PluginSettingsFactory pluginSettingsFactory) {
        this.pluginSettings = pluginSettingsFactory.createGlobalSettings();
    }

    @Override
    public String getWebHookUrl(String key) {
        Object retval = pluginSettings.get(key);
        if (null == retval) {
            return "";
        }

        return retval.toString();
    }

    @Override
    public void setWebHookUrl(String key, String value) {
        pluginSettings.put(key, value);
    }

    @Override
    public String getChannelName(String key) {
        Object retval = pluginSettings.get(key);
        if (null == retval) {
            return "";
        }

        return retval.toString();
    }

    @Override
    public void setChannelName(String key, String value) {
        pluginSettings.put(key, value);
    }

    @Override
    public boolean getChatworkNotificationsEnabled(String key) {
        return Boolean.parseBoolean((String) pluginSettings.get(key));
    }

    @Override
    public void setChatworkNotificationsEnabled(String key, String value) {
        pluginSettings.put(key, value);
    }

    @Override
    public boolean getChatworkNotificationsOpenedEnabled(String key) {
        return Boolean.parseBoolean((String)pluginSettings.get(key));
    }

    @Override
    public void setChatworkNotificationsOpenedEnabled(String key, String value) {
        pluginSettings.put(key, value);
    }

    @Override
    public boolean getChatworkNotificationsReopenedEnabled(String key) {
        return Boolean.parseBoolean((String)pluginSettings.get(key));
    }

    @Override
    public void setChatworkNotificationsReopenedEnabled(String key, String value) {
        pluginSettings.put(key, value);
    }

    @Override
    public boolean getChatworkNotificationsUpdatedEnabled(String key) {
        return Boolean.parseBoolean((String)pluginSettings.get(key));
    }

    @Override
    public void setChatworkNotificationsUpdatedEnabled(String key, String value) {
        pluginSettings.put(key, value);
    }

    @Override
    public boolean getChatworkNotificationsApprovedEnabled(String key) {
        return Boolean.parseBoolean((String)pluginSettings.get(key));
    }

    @Override
    public void setChatworkNotificationsApprovedEnabled(String key, String value) {
        pluginSettings.put(key, value);
    }

    @Override
    public boolean getChatworkNotificationsUnapprovedEnabled(String key) {
        return Boolean.parseBoolean((String)pluginSettings.get(key));
    }

    @Override
    public void setChatworkNotificationsUnapprovedEnabled(String key, String value) {
        pluginSettings.put(key, value);
    }

    @Override
    public boolean getChatworkNotificationsDeclinedEnabled(String key) {
        return Boolean.parseBoolean((String)pluginSettings.get(key));
    }

    @Override
    public void setChatworkNotificationsDeclinedEnabled(String key, String value) {
        pluginSettings.put(key, value);
    }

    @Override
    public boolean getChatworkNotificationsMergedEnabled(String key) {
        return Boolean.parseBoolean((String)pluginSettings.get(key));
    }

    @Override
    public void setChatworkNotificationsMergedEnabled(String key, String value) {
        pluginSettings.put(key, value);
    }

    @Override
    public boolean getChatworkNotificationsCommentedEnabled(String key) {
        return Boolean.parseBoolean((String)pluginSettings.get(key));
    }

    @Override
    public void setChatworkNotificationsCommentedEnabled(String key, String value) {
        pluginSettings.put(key, value);
    }

    @Override
    public boolean getChatworkNotificationsEnabledForPush(String key) {
        return Boolean.parseBoolean((String)pluginSettings.get(key));
    }

    @Override
    public void setChatworkNotificationsEnabledForPush(String key, String value) {
        pluginSettings.put(key, value);
    }

    @Override
    public boolean getChatworkNotificationsEnabledForPersonal(String key) {
        return Boolean.parseBoolean((String)pluginSettings.get(key));
    }

    @Override
    public void setChatworkNotificationsEnabledForPersonal(String key, String value) {
        pluginSettings.put(key, value);
    }

    @Override
    public NotificationLevel getNotificationLevel(String key) {
        Object retval = pluginSettings.get(key);
        if (null == retval) {
            return NotificationLevel.VERBOSE;
        } else {
            return NotificationLevel.valueOf((String)pluginSettings.get(key));
        }
    }

    @Override
    public void setNotificationLevel(String key, String value) {
        pluginSettings.put(key, value);
    }

    @Override
    public NotificationLevel getNotificationPrLevel(String key) {
        Object retval = pluginSettings.get(key);
        if (null == retval) {
            return NotificationLevel.VERBOSE;
        } else {
            return NotificationLevel.valueOf((String)pluginSettings.get(key));
        }
    }

    @Override
    public void setNotificationPrLevel(String key, String value) {
        pluginSettings.put(key, value);
    }

}
