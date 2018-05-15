/*
 * Copyright 2017 Adobe Systems Incorporated
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.adobe.cq.testing.client;

import com.adobe.cq.testing.client.notification.Notification;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClientConfig;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.FormEntityBuilder;
import org.apache.sling.testing.clients.util.HttpUtils;
import org.apache.sling.testing.clients.util.JsonUtils;
import org.apache.sling.testing.clients.util.URLParameterBuilder;
import org.codehaus.jackson.JsonNode;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.http.HttpStatus.SC_OK;


/**
 * The base client for all notifications related tests. It provides a core set of commonly used notification
 * functions e.g. list / approve / delete
 * <br>
 * It extends from {@link CQClient} which in turn provides a core set of
 * commonly used website and page functionality.
 */
public class NotificationClient extends CQClient {

    public static final String NOTIFICATION_INBOX_PATH = "/libs/wcm/core/content/inbox.html";

    public static final String NOTIFICATION_CHANNEL_INBOX = "inbox";
    public static final String NOTIFICATION_CHANNEL_EMAIL = "email";

    public static final String NOTIFICATION_CONFIG_PATH = "/bin/wcm/notification/config";

    public static final String NOTIFICATIONS_SUBSCRIPTIONS_PATH = "/home/users/geometrixx/author/wcm/notification/config/subscriptions";

    public static final String NOTIFICATION_MESSAGES_PATH = "/bin/wcm/notification/inbox/messages";
    
    private static final String NOTIFICATION_ACTION_PATH = "/bin/wcm/notification/inbox/action.json";
    private static final String NOTIFICATIONS_DIALOG_OPTIONS_PATH = "/libs/cq/ui/widgets.js";

    // Notifications default actions to be notified for
    public static final String NOTIFICATION_ACTION_ACTIVATE = "ACTIVATE";
    public static final String NOTIFICATION_ACTION_DEACTIVATE = "DEACTIVATE";
    public static final String NOTIFICATION_ACTION_DELETE = "DELETE";
    public static final String NOTIFICATION_ACTION_PAGE_MODIFIED = "PageModified";
    public static final String NOTIFICATION_ACTION_PAGE_CREATED = "PageCreated";
    public static final String NOTIFICATION_ACTION_PAGE_DELETED = "PageDeleted";
    public static final String NOTIFICATION_ACTION_PAGE_ROLLED_OUT = "PageRolledOut";

    public NotificationClient(CloseableHttpClient http, SlingClientConfig config) throws ClientException {
        super(http, config);
    }

    public NotificationClient(URI serverUrl, String user, String password) throws ClientException {
        super(serverUrl, user, password);
    }

    /**
     * Get the list of available action to be notified for. For now this list is hardcoded in a javascript library file
     * which is loaded by the UI
     *
     * @return                  The list of available actions. Null is none is found
     * @throws ClientException  If something fails during request/response cycle
     */
    public ArrayList<String> getAvailableNotificationAction() throws ClientException {
        ArrayList<String> actions = null;

        SlingHttpResponse exec = doGet(NOTIFICATIONS_DIALOG_OPTIONS_PATH, SC_OK);
        String widgetJs = exec.getContent();

        String notifPart = widgetJs.substring(widgetJs.indexOf("CQ.wcm.NotificationInbox"),
                widgetJs.indexOf("CQ.Ext.reg(\"notificationinbox\", CQ.wcm.NotificationInbox);"));

        Pattern p = Pattern.compile("CQ\\.wcm\\.NotificationInbox = .+var options = (\\[.+\\]);.+", Pattern.DOTALL);

        Matcher m = p.matcher(notifPart);

        if (m.find()) {
            actions = new ArrayList<>();

            String optionsJson = m.group(1)
                    .replace("value:", "\"value\":")
                    .replace("text:", "\"text\":")
                    .replace("CQ.I18n.getMessage(", "")
                    .replace(")", "");

            JsonNode options = JsonUtils.getJsonNodeFromString(optionsJson);
            for (Iterator<JsonNode> it = options.getElements(); it.hasNext(); ) {
                JsonNode option = it.next();
                actions.add(option.get("value").getValueAsText());
            }
        }

        return actions;
    }

    /**
     * Get all the existing notification messages.
     *
     * @return                  List of the existing notification messages
     * @throws ClientException  If something fails during request/response cycle
     */
    public ArrayList<Notification> getNotificationMessages() throws ClientException {
        return getNotificationMessages(-1, -1);
    }

    /**
     * Get all the existing notification messages.
     *
     * @param start first notification
     * @param limit max notifications
     *
     * @return                  List of the existing notification messages
     * @throws ClientException  If something fails during request/response cycle
     */
    public ArrayList<Notification> getNotificationMessages(int start, int limit) throws ClientException {
        ArrayList<Notification> notifications = new ArrayList<>();

        URLParameterBuilder params = URLParameterBuilder.create();

        if (start > -1) {
            params.add("start", String.valueOf(start));
        }

        if (limit > -1) {
            params.add("limit", String.valueOf(limit));
        }

        SlingHttpResponse exec = doGet(NOTIFICATION_MESSAGES_PATH + ".json", params.getList(), SC_OK);
        JsonNode messages = JsonUtils.getJsonNodeFromString(exec.getContent()).get("messages");

        for (Iterator<JsonNode> it = messages.getElements(); it.hasNext(); ) {
            JsonNode message = it.next();
            Notification notification = new Notification(message);
            notifications.add(notification);
        }

        return notifications;
    }

    /**
     * Delete any existing subscription
     *
     * @throws ClientException If something fails during request/response cycle
     */
    public void resetConfig() throws ClientException {
        if (exists(NOTIFICATIONS_SUBSCRIPTIONS_PATH)) {
            deletePath(NOTIFICATIONS_SUBSCRIPTIONS_PATH);
        }
    }

    /**
     * Set a notification subscription
     *
     * @param channel           The type of notification (for now only "inbox")
     * @param actions           The list of actions to be notified for
     * @param packages          The paths to survey
     * @param expectedStatus list of allowed HTTP Status to be returned. If not set, http status 200 (OK) is assumed.
     * @return                  Response of the HTTP request
     * @throws ClientException  If something fails during request/response cycle
     */
    public SlingHttpResponse configNotifications(String channel, String[] actions, String packages, int... expectedStatus)
            throws ClientException {
        // prepare the form
        FormEntityBuilder formEntry = FormEntityBuilder.create();
        // set properties
        formEntry.addParameter("type", channel);
        for (String action : actions) {
            formEntry.addParameter("actions", action);
        }
        formEntry.addParameter("configs", packages);

        return doPost(NOTIFICATION_CONFIG_PATH + ".json", formEntry.build(), expectedStatus);
    }

    /**
     * Get the notification inbox html page
     *
     * @param expectedStatus    list of allowed HTTP Status to be returned. If not set,
     *                          http status 200 (OK) is assumed.
     * @return                  Response of the HTTP request
     * @throws ClientException  If something fails during request/response cycle
     */
    public SlingHttpResponse getNotificationInboxPage(int... expectedStatus) throws ClientException {
        return doGet(NOTIFICATION_INBOX_PATH, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Approve a notification message (mark it as read)
     *
     * @param notification      The notification message to approve
     * @param expectedStatus    list of allowed HTTP Status to be returned. If not set,
     *                          http status 200 (OK) is assumed.
     * @return                  Response of the HTTP request
     * @throws ClientException  If something fails during request/response cycle
     */
    public SlingHttpResponse approveNotification(Notification notification, int... expectedStatus) throws ClientException {
        return performAction(notification, Notification.NOTIFICATION_COMMAND_APPROVE, expectedStatus);
    }

    /**
     * Delete a notification message
     *
     * @param notification      The notification message to delete
     * @param expectedStatus    list of allowed HTTP Status to be returned. If not set,
     *                          http status 200 (OK) is assumed.
     * @return                  Response of the HTTP request
     * @throws ClientException  If something fails during request/response cycle
     */
    public SlingHttpResponse deleteNotification(Notification notification, int... expectedStatus) throws ClientException {
        return performAction(notification, Notification.NOTIFICATION_COMMAND_DELETE, expectedStatus);
    }

    /**
     * Delete a list notification messages
     *
     * @param notifications     The notification messages to delete
     * @param expectedStatus    list of allowed HTTP Status to be returned. If not set,
     *                          http status 200 (OK) is assumed.
     * @throws ClientException  If something fails during request/response cycle
     */
    public void deleteNotifications(ArrayList<Notification> notifications, int... expectedStatus) throws ClientException {
        for (Notification n : notifications) {
            deleteNotification(n, expectedStatus);
        }
    }

    /**
     * Delete all the notification messages
     *
     * @throws ClientException  If something fails during request/response cycle
     */
    public void deleteAllNotifications() throws ClientException {
        deleteNotifications(getNotificationMessages(), SC_OK);
    }

    /**
     * Do an action on a notification message (delete/approve)
     *
     * @param notification      The notification message to apply the action to
     * @param action            Action to perform (for now delete/approve)
     * @param expectedStatus    list of allowed HTTP Status to be returned. If not set,
     *                          http status 200 (OK) is assumed.
     * @return                  Response of the HTTP request
     * @throws ClientException  If something fails during request/response cycle
     */
    public SlingHttpResponse performAction(Notification notification, String action, int... expectedStatus) throws ClientException {
        FormEntityBuilder formEntry = FormEntityBuilder.create();

        // set properties
        if (notification != null) {
            formEntry.addParameter("path", notification.getId());
        }
        if (action != null) {
            formEntry.addParameter("cmd", action);
        }

        // send the request
        return doPost(NOTIFICATION_ACTION_PATH, formEntry.build(), HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }
}
