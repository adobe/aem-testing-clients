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

package com.adobe.cq.testing.client.tagging;

import com.adobe.cq.testing.client.TagClient;
import org.apache.sling.testing.clients.ClientException;
import org.codehaus.jackson.JsonNode;

import java.util.StringTokenizer;

/**
 * Created with IntelliJ IDEA.
 * User: catalan
 * Date: 5/30/12
 * Time: 3:37 PM
 */
public class Tag {

    public static final String PRIMARY_TYPE = "cq:Tag";
    public static final String RESOURCE_TYPE = "cq/tagging/components/tag";
    public static final String TAG_ROOT_PATH = "/content/cq:tags/";
    public static final String TAG_DEFAULT_NAMESPACE_PATH = TAG_ROOT_PATH + "default/";

    public static final String TAG_PROP_TITLE = "jcr:title";
    public static final String TAG_PROP_DESCRIPTION = "jcr:description";

    protected String id = "";
    protected String title = null;
    protected String description = null;
    protected String parentTagId = null;
    protected String path = "";

    protected TagClient client = null;

    /**
     * contains the Json Structure as it is currently saved on the server
     */
    protected JsonNode jsonNode = null;

    public Tag(TagClient client, String id, String title, String description, String parentTagId) {
        this.client = client;
        this.title = title;
        this.description = description;
        this.id = id;

        try {
            client.createTag(title, id, description, parentTagId);
        } catch (ClientException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        this.parentTagId = (parentTagId == null) ? id + ":" : parentTagId + this.id + "/";
    }

    public String getParentTagId() {
        return this.parentTagId;
    }

    public String moveTo(Tag tag) throws ClientException {
        String destPath = tag.getPath() + this.id;
        client.moveTag(getPath(), destPath);
        return destPath;
    }

    public Tag addTag(String id, String title, String description) {
        return new Tag(this.client, id, title, description, this.parentTagId);
    }

    public String getPath() {
        return TAG_ROOT_PATH + this.parentTagId.replace(":", "/");
    }

    public String getNamespacePath() {
        String namespacePath = parentTagId;
        if (namespacePath.endsWith("/") || namespacePath.endsWith(":")) {
            namespacePath = namespacePath.substring(0, namespacePath.length() - 1);
        }
        return namespacePath;
    }

    public JsonNode getJsonNode() throws ClientException {
        String path = getPath().substring(0, getPath().length() - 1);
        jsonNode = client.doGetJson(path + ".tag", -1);
        return jsonNode;
    }

    /**
     * Get the JsonNode of a property. The property can have multiple levels.
     *
     * @param propName the name of the property
     * @return the json node for the property
     */
    public JsonNode getJsonNode(String propName) {
        StringTokenizer st = new StringTokenizer(propName, "/");
        JsonNode prop = jsonNode;
        while (st.hasMoreElements()) {
            prop = prop.get((String) st.nextElement());
        }
        return prop;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}

