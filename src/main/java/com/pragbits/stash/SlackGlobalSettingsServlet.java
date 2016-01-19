package com.pragbits.stash;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.atlassian.stash.exception.AuthorisationException;
import com.atlassian.stash.nav.NavBuilder;
import com.pragbits.stash.PluginMetadata;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.repository.RepositoryService;
import com.atlassian.stash.avatar.AvatarService;
import com.atlassian.stash.user.Permission;
import com.atlassian.stash.user.PermissionValidationService;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import com.atlassian.stash.i18n.I18nService;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.pragbits.stash.soy.SelectFieldOptions;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class ChatworkGlobalSettingsServlet extends HttpServlet {
    static final String KEY_GLOBAL_SETTING_HOOK_URL = "stash2chatwork.globalsettings.hookurl";
    static final String KEY_GLOBAL_SETTING_CHANNEL_NAME = "stash2chatwork.globalsettings.channelname";
    static final String KEY_GLOBAL_SETTING_NOTIFICATIONS_ENABLED = "stash2chatwork.globalsettings.chatworknotificationsenabled";
    static final String KEY_GLOBAL_SETTING_NOTIFICATIONS_OPENED_ENABLED = "stash2chatwork.globalsettings.chatworknotificationsopenedenabled";
    static final String KEY_GLOBAL_SETTING_NOTIFICATIONS_REOPENED_ENABLED = "stash2chatwork.globalsettings.chatworknotificationsreopenedenabled";
    static final String KEY_GLOBAL_SETTING_NOTIFICATIONS_UPDATED_ENABLED = "stash2chatwork.globalsettings.chatworknotificationsupdatedenabled";
    static final String KEY_GLOBAL_SETTING_NOTIFICATIONS_APPROVED_ENABLED = "stash2chatwork.globalsettings.chatworknotificationsapprovedenabled";
    static final String KEY_GLOBAL_SETTING_NOTIFICATIONS_UNAPPROVED_ENABLED = "stash2chatwork.globalsettings.chatworknotificationsunapprovedenabled";
    static final String KEY_GLOBAL_SETTING_NOTIFICATIONS_DECLINED_ENABLED = "stash2chatwork.globalsettings.chatworknotificationsdeclinedenabled";
    static final String KEY_GLOBAL_SETTING_NOTIFICATIONS_MERGED_ENABLED = "stash2chatwork.globalsettings.chatworknotificationsmergedenabled";
    static final String KEY_GLOBAL_SETTING_NOTIFICATIONS_COMMENTED_ENABLED = "stash2chatwork.globalsettings.chatworknotificationscommentedenabled";
    static final String KEY_GLOBAL_SETTING_NOTIFICATIONS_LEVEL = "stash2chatwork.globalsettings.chatworknotificationslevel";
    static final String KEY_GLOBAL_SETTING_NOTIFICATIONS_PR_LEVEL = "stash2chatwork.globalsettings.chatworknotificationsprlevel";
    static final String KEY_GLOBAL_SETTING_NOTIFICATIONS_PUSH_ENABLED = "stash2chatwork.globalsettings.chatworknotificationspushenabled";
    static final String KEY_GLOBAL_SETTING_NOTIFICATIONS_PERSONAL_ENABLED = "stash2chatwork.globalsettings.chatworknotificationspersonalenabled";

    private final PageBuilderService pageBuilderService;
    private final ChatworkGlobalSettingsService chatworkGlobalSettingsService;
    private final SoyTemplateRenderer soyTemplateRenderer;
    private final PermissionValidationService validationService;
    private final I18nService i18nService;

    public ChatworkGlobalSettingsServlet(PageBuilderService pageBuilderService,
                                      ChatworkGlobalSettingsService chatworkGlobalSettingsService,
                                      SoyTemplateRenderer soyTemplateRenderer,
                                      PermissionValidationService validationService,
                                      I18nService i18nService) {
        this.pageBuilderService = pageBuilderService;
        this.chatworkGlobalSettingsService = chatworkGlobalSettingsService;
        this.soyTemplateRenderer = soyTemplateRenderer;
        this.validationService = validationService;
        this.i18nService = i18nService;

    }

    @Override
    protected  void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        try {
            validationService.validateForGlobal(Permission.SYS_ADMIN);
        } catch (AuthorisationException e) {
            // Skip form processing
            doGet(req, res);
            return;
        }

        String globalWebHookUrl = req.getParameter("chatworkGlobalWebHookUrl");
        if (null != globalWebHookUrl) {
            chatworkGlobalSettingsService.setWebHookUrl(KEY_GLOBAL_SETTING_HOOK_URL, globalWebHookUrl);
        }

        String chatworkChannelName = req.getParameter("chatworkChannelName");
        if (null != chatworkChannelName) {
            chatworkGlobalSettingsService.setChannelName(KEY_GLOBAL_SETTING_CHANNEL_NAME, chatworkChannelName);
        }

        Boolean chatworkNotificationsEnabled = false;
        if (null != req.getParameter("chatworkNotificationsEnabled") && req.getParameter("chatworkNotificationsEnabled").equals("on")) {
            chatworkNotificationsEnabled = true;
        }
        chatworkGlobalSettingsService.setChatworkNotificationsEnabled(KEY_GLOBAL_SETTING_NOTIFICATIONS_ENABLED, chatworkNotificationsEnabled.toString());

        Boolean chatworkNotificationsOpenedEnabled = false;
        if (null != req.getParameter("chatworkNotificationsOpenedEnabled") && req.getParameter("chatworkNotificationsOpenedEnabled").equals("on")) {
            chatworkNotificationsOpenedEnabled = true;
        }
        chatworkGlobalSettingsService.setChatworkNotificationsOpenedEnabled(KEY_GLOBAL_SETTING_NOTIFICATIONS_OPENED_ENABLED, chatworkNotificationsOpenedEnabled.toString());

        Boolean chatworkNotificationsReopenedEnabled = false;
        if (null != req.getParameter("chatworkNotificationsReopenedEnabled") && req.getParameter("chatworkNotificationsReopenedEnabled").equals("on")) {
            chatworkNotificationsReopenedEnabled = true;
        }
        chatworkGlobalSettingsService.setChatworkNotificationsReopenedEnabled(KEY_GLOBAL_SETTING_NOTIFICATIONS_REOPENED_ENABLED, chatworkNotificationsReopenedEnabled.toString());

        Boolean chatworkNotificationsUpdatedEnabled = false;
        if (null != req.getParameter("chatworkNotificationsUpdatedEnabled") && req.getParameter("chatworkNotificationsUpdatedEnabled").equals("on")) {
            chatworkNotificationsUpdatedEnabled = true;
        }
        chatworkGlobalSettingsService.setChatworkNotificationsUpdatedEnabled(KEY_GLOBAL_SETTING_NOTIFICATIONS_UPDATED_ENABLED, chatworkNotificationsUpdatedEnabled.toString());

        Boolean chatworkNotificationsApprovedEnabled = false;
        if (null != req.getParameter("chatworkNotificationsApprovedEnabled") && req.getParameter("chatworkNotificationsApprovedEnabled").equals("on")) {
            chatworkNotificationsApprovedEnabled = true;
        }
        chatworkGlobalSettingsService.setChatworkNotificationsApprovedEnabled(KEY_GLOBAL_SETTING_NOTIFICATIONS_APPROVED_ENABLED, chatworkNotificationsApprovedEnabled.toString());

        Boolean chatworkNotificationsUnapprovedEnabled = false;
        if (null != req.getParameter("chatworkNotificationsUnapprovedEnabled") && req.getParameter("chatworkNotificationsUnapprovedEnabled").equals("on")) {
            chatworkNotificationsUnapprovedEnabled = true;
        }
        chatworkGlobalSettingsService.setChatworkNotificationsUnapprovedEnabled(KEY_GLOBAL_SETTING_NOTIFICATIONS_UNAPPROVED_ENABLED, chatworkNotificationsUnapprovedEnabled.toString());

        Boolean chatworkNotificationsDeclinedEnabled = false;
        if (null != req.getParameter("chatworkNotificationsDeclinedEnabled") && req.getParameter("chatworkNotificationsDeclinedEnabled").equals("on")) {
            chatworkNotificationsDeclinedEnabled = true;
        }
        chatworkGlobalSettingsService.setChatworkNotificationsDeclinedEnabled(KEY_GLOBAL_SETTING_NOTIFICATIONS_DECLINED_ENABLED, chatworkNotificationsDeclinedEnabled.toString());

        Boolean chatworkNotificationsMergedEnabled = false;
        if (null != req.getParameter("chatworkNotificationsMergedEnabled") && req.getParameter("chatworkNotificationsMergedEnabled").equals("on")) {
            chatworkNotificationsMergedEnabled = true;
        }
        chatworkGlobalSettingsService.setChatworkNotificationsMergedEnabled(KEY_GLOBAL_SETTING_NOTIFICATIONS_MERGED_ENABLED, chatworkNotificationsMergedEnabled.toString());

        Boolean chatworkNotificationsCommentedEnabled = false;
        if (null != req.getParameter("chatworkNotificationsCommentedEnabled") && req.getParameter("chatworkNotificationsCommentedEnabled").equals("on")) {
            chatworkNotificationsCommentedEnabled = true;
        }
        chatworkGlobalSettingsService.setChatworkNotificationsCommentedEnabled(KEY_GLOBAL_SETTING_NOTIFICATIONS_COMMENTED_ENABLED, chatworkNotificationsCommentedEnabled.toString());

        NotificationLevel notificationLevel = NotificationLevel.VERBOSE;
        if (null != req.getParameter("chatworkNotificationLevel")) {
            notificationLevel = NotificationLevel.valueOf(req.getParameter("chatworkNotificationLevel"));
        }
        chatworkGlobalSettingsService.setNotificationLevel(KEY_GLOBAL_SETTING_NOTIFICATIONS_LEVEL, notificationLevel.toString());

        NotificationLevel notificationPrLevel = NotificationLevel.VERBOSE;
        if (null != req.getParameter("chatworkNotificationPrLevel")) {
            notificationPrLevel = NotificationLevel.valueOf(req.getParameter("chatworkNotificationPrLevel"));
        }
        chatworkGlobalSettingsService.setNotificationPrLevel(KEY_GLOBAL_SETTING_NOTIFICATIONS_PR_LEVEL, notificationPrLevel.toString());

        Boolean chatworkNotificationsEnabledForPush = false;
        if (null != req.getParameter("chatworkNotificationsEnabledForPush") && req.getParameter("chatworkNotificationsEnabledForPush").equals("on")) {
            chatworkNotificationsEnabledForPush = true;
        }
        chatworkGlobalSettingsService.setChatworkNotificationsEnabledForPush(KEY_GLOBAL_SETTING_NOTIFICATIONS_PUSH_ENABLED, chatworkNotificationsEnabledForPush.toString());

        Boolean chatworkNotificationsEnabledForPersonal = false;
        if (null != req.getParameter("chatworkNotificationsEnabledForPersonal") && req.getParameter("chatworkNotificationsEnabledForPersonal").equals("on")) {
            chatworkNotificationsEnabledForPersonal = true;
        }
        chatworkGlobalSettingsService.setChatworkNotificationsEnabledForPersonal(KEY_GLOBAL_SETTING_NOTIFICATIONS_PERSONAL_ENABLED, chatworkNotificationsEnabledForPersonal.toString());


        doGet(req, res);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doView(response);

    }

    private void doView(HttpServletResponse response)
            throws ServletException, IOException {

        validationService.validateForGlobal(Permission.ADMIN);

        String webHookUrl = chatworkGlobalSettingsService.getWebHookUrl(KEY_GLOBAL_SETTING_HOOK_URL);
        if (null == webHookUrl || webHookUrl.equals("")) {
            webHookUrl = "";
        }

        String channelName = chatworkGlobalSettingsService.getChannelName(KEY_GLOBAL_SETTING_CHANNEL_NAME);
        if (null == channelName || channelName.equals("")) {
            channelName = "";
        }

        Boolean chatworkNotificationsEnabled = chatworkGlobalSettingsService.getChatworkNotificationsEnabled(KEY_GLOBAL_SETTING_NOTIFICATIONS_ENABLED);
        Boolean chatworkNotificationsOpenedEnabled = chatworkGlobalSettingsService.getChatworkNotificationsOpenedEnabled(KEY_GLOBAL_SETTING_NOTIFICATIONS_OPENED_ENABLED);
        Boolean chatworkNotificationsReopenedEnabled = chatworkGlobalSettingsService.getChatworkNotificationsReopenedEnabled(KEY_GLOBAL_SETTING_NOTIFICATIONS_REOPENED_ENABLED);
        Boolean chatworkNotificationsUpdatedEnabled = chatworkGlobalSettingsService.getChatworkNotificationsUpdatedEnabled(KEY_GLOBAL_SETTING_NOTIFICATIONS_UPDATED_ENABLED);
        Boolean chatworkNotificationsApprovedEnabled = chatworkGlobalSettingsService.getChatworkNotificationsApprovedEnabled(KEY_GLOBAL_SETTING_NOTIFICATIONS_APPROVED_ENABLED);
        Boolean chatworkNotificationsUnapprovedEnabled = chatworkGlobalSettingsService.getChatworkNotificationsUnapprovedEnabled(KEY_GLOBAL_SETTING_NOTIFICATIONS_UNAPPROVED_ENABLED);
        Boolean chatworkNotificationsDeclinedEnabled = chatworkGlobalSettingsService.getChatworkNotificationsDeclinedEnabled(KEY_GLOBAL_SETTING_NOTIFICATIONS_DECLINED_ENABLED);
        Boolean chatworkNotificationsMergedEnabled = chatworkGlobalSettingsService.getChatworkNotificationsMergedEnabled(KEY_GLOBAL_SETTING_NOTIFICATIONS_MERGED_ENABLED);
        Boolean chatworkNotificationsCommentedEnabled = chatworkGlobalSettingsService.getChatworkNotificationsCommentedEnabled(KEY_GLOBAL_SETTING_NOTIFICATIONS_COMMENTED_ENABLED);
        Boolean chatworkNotificationsEnabledForPush = chatworkGlobalSettingsService.getChatworkNotificationsEnabledForPush(KEY_GLOBAL_SETTING_NOTIFICATIONS_PUSH_ENABLED);
        Boolean chatworkNotificationsEnabledForPersonal = chatworkGlobalSettingsService.getChatworkNotificationsEnabledForPersonal(KEY_GLOBAL_SETTING_NOTIFICATIONS_PERSONAL_ENABLED);
        String notificationLevel = chatworkGlobalSettingsService.getNotificationLevel(KEY_GLOBAL_SETTING_NOTIFICATIONS_LEVEL).toString();
        String notificationPrLevel = chatworkGlobalSettingsService.getNotificationPrLevel(KEY_GLOBAL_SETTING_NOTIFICATIONS_PR_LEVEL).toString();

        render(response,
                "stash.page.chatwork.global.settings.viewGlobalChatworkSettings",
                ImmutableMap.<String, Object>builder()
                        .put("chatworkGlobalWebHookUrl", webHookUrl)
                        .put("chatworkChannelName", channelName)
                        .put("chatworkNotificationsEnabled", chatworkNotificationsEnabled)
                        .put("chatworkNotificationsOpenedEnabled", chatworkNotificationsOpenedEnabled)
                        .put("chatworkNotificationsReopenedEnabled", chatworkNotificationsReopenedEnabled)
                        .put("chatworkNotificationsUpdatedEnabled", chatworkNotificationsUpdatedEnabled)
                        .put("chatworkNotificationsApprovedEnabled", chatworkNotificationsApprovedEnabled)
                        .put("chatworkNotificationsUnapprovedEnabled", chatworkNotificationsUnapprovedEnabled)
                        .put("chatworkNotificationsDeclinedEnabled", chatworkNotificationsDeclinedEnabled)
                        .put("chatworkNotificationsMergedEnabled", chatworkNotificationsMergedEnabled)
                        .put("chatworkNotificationsCommentedEnabled", chatworkNotificationsCommentedEnabled)
                        .put("chatworkNotificationsEnabledForPush", chatworkNotificationsEnabledForPush)
                        .put("chatworkNotificationsEnabledForPersonal", chatworkNotificationsEnabledForPersonal)
                        .put("notificationLevel", notificationLevel)
                        .put("notificationPrLevel", notificationPrLevel)
                        .put("notificationLevels", new SelectFieldOptions(NotificationLevel.values()).toSoyStructure())
                        .build()
        );
    }

    private void render(HttpServletResponse response, String templateName, Map<String, Object> data)
            throws IOException, ServletException {
        pageBuilderService.assembler().resources().requireContext("plugin.adminpage.chatwork");
        response.setContentType("text/html;charset=UTF-8");
        try {
            soyTemplateRenderer.render(response.getWriter(), PluginMetadata.getCompleteModuleKey("soy-templates"), templateName, data);
        } catch (SoyException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new ServletException(e);
        }
    }

}
