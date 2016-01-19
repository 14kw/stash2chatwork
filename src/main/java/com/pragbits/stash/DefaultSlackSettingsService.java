package com.pragbits.stash;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.user.Permission;
import com.atlassian.stash.user.PermissionValidationService;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Preconditions.checkNotNull;

public class DefaultChatworkSettingsService implements ChatworkSettingsService {

    static final ImmutableChatworkSettings DEFAULT_CONFIG = new ImmutableChatworkSettings(
            false,  // pr settings override enabled
            false,  // pull requests enabled
            true,   // opened
            true,   // reopened
            true,   // updated
            true,   // approved
            true,   // unapproved
            true,   // declined
            true,   // merged
            true,   // commented
            false,  // push enabled
            false,  // personal (forks) enabled
            NotificationLevel.VERBOSE,
            NotificationLevel.VERBOSE,
            "",     // channel name override
            "");    // webhook override

    static final String KEY_SLACK_OVERRIDE_NOTIFICATION = "chatworkNotificationsOverrideEnabled";
    static final String KEY_SLACK_NOTIFICATION = "chatworkNotificationsEnabled";
    static final String KEY_SLACK_OPENED_NOTIFICATION = "chatworkNotificationsOpenedEnabled";
    static final String KEY_SLACK_REOPENED_NOTIFICATION = "chatworkNotificationsReopenedEnabled";
    static final String KEY_SLACK_UPDATED_NOTIFICATION = "chatworkNotificationsUpdatedEnabled";
    static final String KEY_SLACK_APPROVED_NOTIFICATION = "chatworkNotificationsApprovedEnabled";
    static final String KEY_SLACK_UNAPPROVED_NOTIFICATION = "chatworkNotificationsUnapprovedEnabled";
    static final String KEY_SLACK_DECLINED_NOTIFICATION = "chatworkNotificationsDeclinedEnabled";
    static final String KEY_SLACK_MERGED_NOTIFICATION = "chatworkNotificationsMergedEnabled";
    static final String KEY_SLACK_COMMENTED_NOTIFICATION = "chatworkNotificationsCommentedEnabled";
    static final String KEY_SLACK_NOTIFICATION_PUSH = "chatworkNotificationsEnabledForPush";
    static final String KEY_SLACK_NOTIFICATION_PERSONAL = "chatworkNotificationsEnabledForPersonal";
    static final String KEY_SLACK_NOTIFICATION_LEVEL = "chatworkNotificationLevel";
    static final String KEY_SLACK_NOTIFICATION_PR_LEVEL = "chatworkNotificationPrLevel";
    static final String KEY_SLACK_CHANNEL_NAME = "chatworkChannelName";
    static final String KEY_SLACK_WEBHOOK_URL = "chatworkWebHookUrl";

    private final PluginSettings pluginSettings;
    private final PermissionValidationService validationService;

    private final Cache<Integer, ChatworkSettings> cache = CacheBuilder.newBuilder().build(
            new CacheLoader<Integer, ChatworkSettings>() {
                @Override
                public ChatworkSettings load(@Nonnull Integer repositoryId) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> data = (Map) pluginSettings.get(repositoryId.toString());
                    return data == null ? DEFAULT_CONFIG : deserialize(data);
                }
            }
    );

    public DefaultChatworkSettingsService(PluginSettingsFactory pluginSettingsFactory, PermissionValidationService validationService) {
        this.validationService = validationService;
        this.pluginSettings = pluginSettingsFactory.createSettingsForKey(PluginMetadata.getPluginKey());
    }

    @Nonnull
    @Override
    public ChatworkSettings getChatworkSettings(@Nonnull Repository repository) {
        validationService.validateForRepository(checkNotNull(repository, "repository"), Permission.REPO_READ);

        try {
            //noinspection ConstantConditions
            return cache.get(repository.getId());
        } catch (ExecutionException e) {
            throw Throwables.propagate(e.getCause());
        }
    }

    @Nonnull
    @Override
    public ChatworkSettings setChatworkSettings(@Nonnull Repository repository, @Nonnull ChatworkSettings settings) {
        validationService.validateForRepository(checkNotNull(repository, "repository"), Permission.REPO_ADMIN);
        Map<String, String> data = serialize(checkNotNull(settings, "settings"));
        pluginSettings.put(Integer.toString(repository.getId()), data);
        cache.invalidate(repository.getId());

        return deserialize(data);
    }

    // note: for unknown reason, pluginSettngs.get() is not getting back the key for an empty string value
    // probably I don't know someyhing here. Applying a hack
    private Map<String, String> serialize(ChatworkSettings settings) {
        ImmutableMap<String, String> immutableMap = ImmutableMap.<String, String>builder()
                .put(KEY_SLACK_OVERRIDE_NOTIFICATION, Boolean.toString(settings.isChatworkNotificationsOverrideEnabled()))
                .put(KEY_SLACK_NOTIFICATION, Boolean.toString(settings.isChatworkNotificationsEnabled()))
                .put(KEY_SLACK_OPENED_NOTIFICATION, Boolean.toString(settings.isChatworkNotificationsOpenedEnabled()))
                .put(KEY_SLACK_REOPENED_NOTIFICATION, Boolean.toString(settings.isChatworkNotificationsReopenedEnabled()))
                .put(KEY_SLACK_UPDATED_NOTIFICATION, Boolean.toString(settings.isChatworkNotificationsUpdatedEnabled()))
                .put(KEY_SLACK_APPROVED_NOTIFICATION, Boolean.toString(settings.isChatworkNotificationsApprovedEnabled()))
                .put(KEY_SLACK_UNAPPROVED_NOTIFICATION, Boolean.toString(settings.isChatworkNotificationsUnapprovedEnabled()))
                .put(KEY_SLACK_DECLINED_NOTIFICATION, Boolean.toString(settings.isChatworkNotificationsDeclinedEnabled()))
                .put(KEY_SLACK_MERGED_NOTIFICATION, Boolean.toString(settings.isChatworkNotificationsMergedEnabled()))
                .put(KEY_SLACK_COMMENTED_NOTIFICATION, Boolean.toString(settings.isChatworkNotificationsCommentedEnabled()))
                .put(KEY_SLACK_NOTIFICATION_PUSH, Boolean.toString(settings.isChatworkNotificationsEnabledForPush()))
                .put(KEY_SLACK_NOTIFICATION_PERSONAL, Boolean.toString(settings.isChatworkNotificationsEnabledForPersonal()))
                .put(KEY_SLACK_NOTIFICATION_LEVEL, settings.getNotificationLevel().toString())
                .put(KEY_SLACK_NOTIFICATION_PR_LEVEL, settings.getNotificationPrLevel().toString())
                .put(KEY_SLACK_CHANNEL_NAME, settings.getChatworkChannelName().isEmpty() ? " " : settings.getChatworkChannelName())
                .put(KEY_SLACK_WEBHOOK_URL, settings.getChatworkWebHookUrl().isEmpty() ? " " : settings.getChatworkWebHookUrl())
                .build();

        return  immutableMap;
    }

    // note: for unknown reason, pluginSettngs.get() is not getting back the key for an empty string value
    // probably I don't know something here. Applying a hack
    private ChatworkSettings deserialize(Map<String, String> settings) {
        return new ImmutableChatworkSettings(
                Boolean.parseBoolean(settings.get(KEY_SLACK_OVERRIDE_NOTIFICATION)),
                Boolean.parseBoolean(settings.get(KEY_SLACK_NOTIFICATION)),
                Boolean.parseBoolean(settings.get(KEY_SLACK_OPENED_NOTIFICATION)),
                Boolean.parseBoolean(settings.get(KEY_SLACK_REOPENED_NOTIFICATION)),
                Boolean.parseBoolean(settings.get(KEY_SLACK_UPDATED_NOTIFICATION)),
                Boolean.parseBoolean(settings.get(KEY_SLACK_APPROVED_NOTIFICATION)),
                Boolean.parseBoolean(settings.get(KEY_SLACK_UNAPPROVED_NOTIFICATION)),
                Boolean.parseBoolean(settings.get(KEY_SLACK_DECLINED_NOTIFICATION)),
                Boolean.parseBoolean(settings.get(KEY_SLACK_MERGED_NOTIFICATION)),
                Boolean.parseBoolean(settings.get(KEY_SLACK_COMMENTED_NOTIFICATION)),
                Boolean.parseBoolean(settings.get(KEY_SLACK_NOTIFICATION_PUSH)),
                Boolean.parseBoolean(settings.get(KEY_SLACK_NOTIFICATION_PERSONAL)),
                settings.containsKey(KEY_SLACK_NOTIFICATION_LEVEL) ? NotificationLevel.valueOf(settings.get(KEY_SLACK_NOTIFICATION_LEVEL)) : NotificationLevel.VERBOSE,
                settings.containsKey(KEY_SLACK_NOTIFICATION_PR_LEVEL) ? NotificationLevel.valueOf(settings.get(KEY_SLACK_NOTIFICATION_PR_LEVEL)) : NotificationLevel.VERBOSE,
                settings.get(KEY_SLACK_CHANNEL_NAME).toString().equals(" ") ? "" : settings.get(KEY_SLACK_CHANNEL_NAME).toString(),
                settings.get(KEY_SLACK_WEBHOOK_URL).toString().equals(" ") ? "" : settings.get(KEY_SLACK_WEBHOOK_URL).toString()
        );
    }

}
