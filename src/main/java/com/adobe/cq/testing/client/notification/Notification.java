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
package com.adobe.cq.testing.client.notification;

import com.adobe.cq.testing.client.CQClient;
import com.fasterxml.jackson.databind.JsonNode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Notification {


    public static final String NOTIFICATION_COMMAND_APPROVE = "approve";
    public static final String NOTIFICATION_COMMAND_DELETE = "delete";

    protected String id = "";
    protected String modification = "";
    protected String path = "";
    protected Date date;
    protected boolean isRead;
    protected boolean isUserMessage;
    protected String user;


    public Notification(JsonNode jsonNotification) {

        this.id = jsonNotification.get("id").textValue();
        this.modification = jsonNotification.get("modification").textValue();
        this.path = jsonNotification.get("path").textValue();
        this.isRead = jsonNotification.get("isRead").booleanValue();
        this.isUserMessage = jsonNotification.get("isUserMessage").booleanValue();
        this.user = jsonNotification.get("user").textValue();
        String d = jsonNotification.get("date").textValue();

        try {
            SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
            this.date = format.parse(d);
        } catch (ParseException e) {
            this.date = null;
        }
    }


    public Notification(CQClient client, String id, String modification, String path, Date date, boolean isRead, boolean isUserMessage,
                        String user) {
        this.id = id;
        this.modification = modification;
        this.path = path;
        this.date = date;
        this.isRead = isRead;
        this.isUserMessage = isUserMessage;
        this.user = user;
    }


    /**
     * @return the id
     */
    public String getId() {
        return id;
    }


    /**
     * @param id
     *            the id to set
     */
    public void setId(String id) {
        this.id = id;
    }


    /**
     * @return the modification
     */
    public String getModification() {
        return modification;
    }


    /**
     * @param modification
     *            the modification to set
     */
    public void setModification(String modification) {
        this.modification = modification;
    }


    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }


    /**
     * @param path
     *            the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }


    /**
     * @return the date
     */
    public Date getDate() {
        return date;
    }


    /**
     * @param date
     *            the date to set
     */
    public void setDate(Date date) {
        this.date = date;
    }


    /**
     * @return the isRead
     */
    public boolean isRead() {
        return isRead;
    }


    /**
     * @param isRead
     *            the isRead to set
     */
    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }


    /**
     * @return the isUserMessage
     */
    public boolean isUserMessage() {
        return isUserMessage;
    }


    /**
     * @param isUserMessage
     *            the isUserMessage to set
     */
    public void setUserMessage(boolean isUserMessage) {
        this.isUserMessage = isUserMessage;
    }


    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }


    /**
     * @param user
     *            the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + (isRead ? 1231 : 1237);
        result = prime * result + (isUserMessage ? 1231 : 1237);
        result = prime * result + ((modification == null) ? 0 : modification.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((user == null) ? 0 : user.hashCode());
        return result;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Notification other = (Notification) obj;
        if (date == null) {
            if (other.date != null)
                return false;
        } else if (!date.equals(other.date))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (isRead != other.isRead)
            return false;
        if (isUserMessage != other.isUserMessage)
            return false;
        if (modification == null) {
            if (other.modification != null)
                return false;
        } else if (!modification.equals(other.modification))
            return false;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        if (user == null) {
            if (other.user != null)
                return false;
        } else if (!user.equals(other.user))
            return false;
        return true;
    }
}
