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

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClientConfig;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.FormEntityBuilder;
import org.apache.sling.testing.clients.util.HttpUtils;

import java.net.URI;
import java.util.ArrayList;

import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;

/**
 * The base client for all Multi Site Manager (MSM) related tests. It provides a core set of commonly used MSM
 * functions e.g. creating a blueprint / site / live copy. The MSM enables you to create a site (called a live copy)
 * based on another site (called a blueprint) and to actively manage the relationships between the blueprint and the
 * live copy.
 * <br>
 * It extends from {@link CQClient} which in turn provides a core set of
 * commonly used website and page functionality.
 */
public class MSMClient extends CQClient {
    public static final String JCR_PRIMARYTYPE = "jcr:primaryType";
    public static final String JCR_CONTENT = "jcr:content";

    public static final String PARAM_SOURCEPATH = "msm:sourcePath";
    public static final String PARAM_IS_DEEP = "msm:isDeep";
    public static final String PN_ROLLOUT_CONFIGS = "cq:rolloutConfigs";

    /**
     * LiveCopy configuration
     */
    public static final String LIVECOPY_POST_EXTENSION = ".msm.conf";
    public static final String LIVECOPY_GET_EXTENSION = ".msm.json";

    /**
     * Blueprint rollout configuration
     */
    public static final String BLUEPRINT_POST_EXTENSION = ".blueprint.conf";
    public static final String BLUEPRINT_GET_EXTENSION = ".blueprint.json";

    /**
     * LiveCopy general properties
     */
    public static final String PROPERTY_LASTROLLEDOUT = "cq:lastRolledout";
    public static final String PROPERTY_LASTROLLEDOUTBY = "cq:lastRolledoutBy";


    /**
     * Rollout Configuration
     */
    private static final String ROLLOUTCONFIG_FOLDER = "/etc/msm/rolloutconfigs";

    public static final String ROLLOUTCONFIG_DEFAULT = ROLLOUTCONFIG_FOLDER + "/default";
    public static final String ROLLOUTCONFIG_ACTIVATE = ROLLOUTCONFIG_FOLDER + "/activate";
    public static final String ROLLOUTCONFIG_DEACTIVATE = ROLLOUTCONFIG_FOLDER + "/deactivate";
    public static final String ROLLOUTCONFIG_PUSHONMODIFY = ROLLOUTCONFIG_FOLDER + "/pushonmodify";

    /**
     * BluePrint location
     */
    public final static String ETC_BLUEPRINT = "/etc/blueprints";

    /**
     * RolloutConfiguration ResourceType
     */
    private static final String RT_ROLLUTCONFIG = "wcm/msm/components/rolloutconfig";

    /**
     * LiveAction NodeType
     */
    private static final String NT_LIVE_SNCCONFIG = "cq:LiveSyncAction";

    /**
     * RolloutConfiguration Property Name for tigger
     */
    private static final String PN_TRIGGER = "cq:trigger";

    public MSMClient(CloseableHttpClient http, SlingClientConfig config) throws ClientException {
        super(http, config);
    }

    public MSMClient(URI serverUrl, String user, String password) throws ClientException {
        super(serverUrl, user, password);
    }

    /**
     * Creates a blueprint.
     * <br>
     * The blueprint makes it possible to define structure and content centrally. The structure and content of the
     * blueprint can then be used in the live copy
     *
     * @param name           the label/name of the blueprint
     * @param title          the title of the blueprint
     * @param sitePath       the path to an existing branch (site)
     * @param expectedStatus list of allowed HTTP Status to be returned. If not set,
     *                       http status 200 (OK) is assumed.
     * @return Sling response
     * @throws ClientException If something fails during request/response cycle
     */
    public SlingHttpResponse createBluePrint(String name, String title, String sitePath, int... expectedStatus)
            throws ClientException {
        // create blueprint
        String blueprintPath = createPage(name, title, "/etc/blueprints", "/libs/wcm/msm/templates/blueprint",
                expectedStatus).getSlingPath();

        // define blueprint site path, aka source path
        String postPath = blueprintPath + "/jcr:content";

        return doPost(postPath, FormEntityBuilder.create().addParameter("./sitePath", sitePath).build(),
                HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Delete a blueprint.
     * <br>
     * The blueprint makes it possible to define structure and content centrally. The structure and content of the
     * blueprint can then be used in the live copy
     *
     * @param blueprintPath  the path to the blueprint
     * @param expectedStatus list of allowed HTTP Status to be returned. If not set,
     *                       http status 200 (OK) is assumed.
     * @return Sling response
     * @throws ClientException If something fails during request/response cycle
     */
    public SlingHttpResponse deleteBluePrint(String blueprintPath, int... expectedStatus) throws ClientException {
        return deletePage(new String[]{blueprintPath}, false, false, expectedStatus);
    }

    /**
     * Creates an new site.
     * A site can be considered as 1:1 copy of an existing branch or page. Every site can be defined as live copy
     * which manage the relationships between the blueprint and the live copy.
     *
     * @param label          the label/name of the new site
     * @param title          the title of the new site
     * @param destPath       the location where the new site gets created
     * @param isLiveCopy     true if the new site works as a live copy
     * @param languages      the languages labels to copy
     * @param chapterPaths   the chapters' pages to copy taken from first language of site
     * @param blueprintPath  the path to the blueprint
     * @param rolloutConfigs the rollout configuration for the live copy
     * @param siteOwner      the site owner for the new site
     * @param expectedStatus list of allowed HTTP Status to be returned. If not set,
     *                       http status 200 (OK) is assumed.
     * @return Sling response
     * @throws ClientException If something fails during request/response cycle
     */
    public SlingHttpResponse createSite(String label, String title, String destPath, boolean isLiveCopy,
                                        String[] languages,
                                        String[] chapterPaths, String blueprintPath, String[] rolloutConfigs,
                                        String siteOwner,
                                        int... expectedStatus)
            throws ClientException {
        // create the site
        return wcmCommands.createSite(label, title, destPath, isLiveCopy, languages, chapterPaths,
                blueprintPath, rolloutConfigs, siteOwner, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Creates a live copy.
     * A live copy can be created from an existing branch or page or a site that is a live copy by using a blueprint.
     *
     * @param label           the label/name of the new site
     * @param title           the title of the new site
     * @param destPath        the location where the new site gets created
     * @param srcPath         the source path of the liveCopy
     * @param shallow         if set to true subpages are excluded
     * @param rolloutConfigs  the rollout configuration for the live copy
     * @param missingPages    pages which are not rolled out yet (missing) but should be part of the rollout
     * @param excludeSubPages true if subpages of missing pages are excluded (subpages of missing pages are not par of
     *                        rollout)
     * @param expectedStatus  list of allowed HTTP Status to be returned. If not set,
     *                        http status 200 (OK) is assumed.
     * @return Sling response
     * @throws ClientException If something fails during request/response cycle
     */
    public SlingHttpResponse createLiveCopy(String label, String title, String destPath, String srcPath,
                                            boolean shallow, String[] rolloutConfigs, String[] missingPages,
                                            boolean excludeSubPages, int... expectedStatus) throws ClientException {
        // create the livecopy
        return wcmCommands.createLiveCopy(label, title, destPath, srcPath, shallow, rolloutConfigs,
                missingPages, excludeSubPages, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Edit the LiveRelationship at a given Path
     *
     * @param target          the LiveCopy to edit
     * @param sourcePath      the blueprint path
     * @param deep            if the LiveCopy is deep
     * @param rolloutConfigs  the RolloutConfigs to take for LiveCopy
     * @param expectedStatus  list of allowed HTTP Status to be returned. If not set, http status 200 (OK) is assumed.
     * @return Sling response
     * @throws ClientException If something fails during request/response cycle
     */
    public SlingHttpResponse editRelationship(String target, String sourcePath,
                                              boolean deep, String[] rolloutConfigs,
                                              int... expectedStatus) throws ClientException {
        String postPath = target + LIVECOPY_POST_EXTENSION;

        FormEntityBuilder entityBuilder = FormEntityBuilder.create();
        entityBuilder.addParameter(PARAM_SOURCEPATH, sourcePath);
        entityBuilder.addParameter(PARAM_IS_DEEP, Boolean.toString(deep));

        for (String rolloutConfig : ((rolloutConfigs != null) ? rolloutConfigs : new String[0])) {
            entityBuilder.addParameter(PN_ROLLOUT_CONFIGS, rolloutConfig);
        }

        return doPost(postPath, entityBuilder.build(), HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Create a new RolloutConfiguration with given Trigger and given LiveActions
     *
     * @param name        to give for the RolloutConfig
     * @param above       optional, Page name the RolloutConfig to be next to the new
     * @param trigger     Trigger to be used for the RolloutConfig
     * @param liveActions names of the LiveActions to be contained in the RolloutConfig  @return the Path to the newly created RolloutConfig
     * @return the path to the rollout config
     * @throws ClientException in case of Errors during RolloutConfiguration creation
     */
    public String createRolloutConfig(String name, String above, String trigger, String... liveActions)
            throws ClientException {

        String path = createPage(name, null, ROLLOUTCONFIG_FOLDER, null, SC_OK).getSlingPath();
        ArrayList<NameValuePair> props = new ArrayList<>(2);
        props.add(new BasicNameValuePair("sling:resourceType", RT_ROLLUTCONFIG));
        props.add(new BasicNameValuePair(PN_TRIGGER, trigger));
        adaptTo(JsonClient.class).setPageProperties(path, props, 200);

        if (above != null) {
            UrlEncodedFormEntity entity = FormEntityBuilder.create().addParameter(":order", "before " + above).build();
            doPost(path, entity, SC_OK);
        }

        if (liveActions != null) {
            FormEntityBuilder entityBuilder = FormEntityBuilder.create();
            for (String liveAction : liveActions) {
                entityBuilder.addParameter(":name", liveAction);
            }
            entityBuilder.addParameter("./"  + JCR_PRIMARYTYPE, NT_LIVE_SNCCONFIG);
            doPost(path + "/" + JCR_CONTENT + "/", entityBuilder.build(), SC_CREATED);
        }

        return path;
    }

}
