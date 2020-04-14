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

package com.adobe.cq.testing.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.sling.testing.Constants;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.FormEntityBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Utility class that has separate methods for each available WCM Command.<br>
 * <br>
 * WCM Commands are requests sent to /bin/wcmcommand, with a list of of
 * parameters. one of the parameters must be named "cmd" and contains the actual
 * command string.<br>
 * <br>
 * Currently available commands are:
 * <ul>
 * <li>createPage: Creates a CQ Page</li>
 * </ul>
 */
public class WCMCommands {

    public static final String CMD_CREATE_PAGE = "createPage";
    public static final String CMD_DELETE_PAGE = "deletePage";
    public static final String CMD_COPY_PAGE = "copyPage";
    public static final String CMD_MOVE_PAGE = "movePage";
    public static final String CMD_COPY_LANGUAGE = "copyLanguages";
    public static final String CMD_CREATE_VERSION = "createVersion";
    public static final String CMD_RESTORE_VERSION = "restoreVersion";
    public static final String CMD_RESTORE_TREE = "restoreTree";
    public static final String CMD_LOCK_PAGE = "lockPage";
    public static final String CMD_UNLOCK_PAGE = "unlockPage";
    public static final String CMD_ROLLOUT = "rollout";

    public static final String CMD_CREATE_SITE = "createSite";
    public static final String CMD_CREATE_LIVECOPY = "createLiveCopy";

    public static final String CMD_MODERATE_COMMENT = "moderateComment";
    public static final String CMD_MARK_COMMENT_AS_SPAM = "markCommentAsSpam";
    public static final String CMD_DELETE_COMMENT = "deleteComment";

    public static final String CMD_CREATE_LAUNCH = "createLaunch";
    public static final String CMD_DELETE_LAUNCH = "deleteLaunch";
    public static final String CMD_EDIT_LAUNCH = "editLaunch";
    public static final String CMD_CLONE_LAUNCH = "cloneLaunch";
    public static final String CMD_PROMOTE_LAUNCH = "promoteLaunch";

    public static final String CMD_CREATE_CATALOG = "createCatalog";
    public static final String CMD_ROLLOUT_SECTION = "rolloutSection";

    private SlingClient client;

    public WCMCommands(SlingClient client) {
        this.client = client;
    }


    public SlingHttpResponse createPage(String pageLabel, String pageTitle, String parentPath, String templatePath, int... expectedStatus)
            throws ClientException {

        // build the form data
        FormEntityBuilder feb = FormEntityBuilder.create()
                .addParameter("cmd", CMD_CREATE_PAGE)
                .addParameter("parentPath", parentPath)
                .addParameter("label", pageLabel)
                .addParameter("title", pageTitle)
                .addParameter("template", templatePath);

        return executeWCMCommand(CMD_CREATE_PAGE, feb, expectedStatus);
    }


    public SlingHttpResponse deletePage(String[] pagePaths, boolean force, boolean shallow, int... expectedStatus) throws ClientException {

        // build the form data
        FormEntityBuilder feb = FormEntityBuilder.create()
                .addParameter("cmd", CMD_DELETE_PAGE)
                .addParameter("force", Boolean.valueOf(force).toString())
                .addParameter("shallow", Boolean.valueOf(shallow).toString());

        for (String val : (pagePaths != null) ? pagePaths : new String[0]) {
            feb.addParameter("path", val);
        }

        return executeWCMCommand(CMD_DELETE_PAGE, feb, expectedStatus);
    }


    public SlingHttpResponse copyPage(String[] srcPaths, String destName, String destParentPath, String before,
                                      boolean shallow, int... expectedStatus) throws ClientException {

        FormEntityBuilder feb = FormEntityBuilder.create()
                .addParameter("cmd", CMD_COPY_PAGE)
                .addParameter("destName", destName)
                .addParameter("destParentPath", destParentPath)
                .addParameter("before", before)
                .addParameter("shallow", Boolean.valueOf(shallow).toString());

        for (String val : (srcPaths != null) ? srcPaths : new String[0]) {
            feb.addParameter("srcPath", val);
        }

        return executeWCMCommand(CMD_COPY_PAGE, feb, expectedStatus);
    }


    public SlingHttpResponse movePage(String[] srcPaths, String destName, String destParentPath, String before,
                                      boolean shallow, boolean integrity, String[] adjusts, int... expectedStatus)
            throws ClientException {

        FormEntityBuilder feb = FormEntityBuilder.create()
                .addParameter("cmd", CMD_MOVE_PAGE)
                .addParameter("destName", destName)
                .addParameter("destParentPath", destParentPath)
                .addParameter("before", before)
                .addParameter("shallow", Boolean.valueOf(shallow).toString())
                .addParameter("integrity", Boolean.valueOf(integrity).toString());

        if (srcPaths != null)
            for (String val : srcPaths)
                feb.addParameter("srcPath", val);

        if (adjusts != null)
            for (String val : adjusts)
                feb.addParameter("adjust", val);

        return executeWCMCommand(CMD_MOVE_PAGE, feb, expectedStatus);
    }


    public SlingHttpResponse movePage(String[] srcPaths, String destName, String destParentPath, String before,
                                      boolean shallow, boolean integrity, String[] adjusts, String[] publishes,
                                      int... expectedStatus)
            throws ClientException {

        FormEntityBuilder feb = FormEntityBuilder.create()
                .addParameter("cmd", CMD_MOVE_PAGE)
                .addParameter("destName", destName)
                .addParameter("destParentPath", destParentPath)
                .addParameter("before", before)
                .addParameter("shallow", Boolean.valueOf(shallow).toString())
                .addParameter("integrity", Boolean.valueOf(integrity).toString());

        if (srcPaths != null)
            for (String val : srcPaths)
                feb.addParameter("srcPath", val);

        if (adjusts != null)
            feb.addParameter("adjust", "[\"".concat(StringUtils.join(adjusts, "\",\"")).concat("\"]"));

        if (publishes != null)
            feb.addParameter("publish", "[\"".concat(StringUtils.join(publishes, "\",\"")).concat("\"]"));

        return executeWCMCommand(CMD_MOVE_PAGE, feb, expectedStatus);
    }


    public SlingHttpResponse copyLanguages(String sitePath, List<BasicNameValuePair> relPaths, int... expectedStatus)
            throws ClientException {

        // build the form Data
        FormEntityBuilder feb = FormEntityBuilder.create().addParameter("cmd", CMD_COPY_LANGUAGE).addParameter("path", sitePath);

        for (BasicNameValuePair val : relPaths)
            feb.addParameter(val.getName(), val.getValue());

        return executeWCMCommand(CMD_COPY_LANGUAGE, feb, expectedStatus);
    }


    public SlingHttpResponse createVersion(String pagePath, String comment, String label, int... expectedStatus) throws ClientException {

        FormEntityBuilder feb = FormEntityBuilder.create()
                .addParameter("cmd", CMD_CREATE_VERSION)
                .addParameter("path", pagePath)
                .addParameter("comment", comment)
                .addParameter("label", label);

        return executeWCMCommand(CMD_CREATE_VERSION, feb, expectedStatus);
    }


    public SlingHttpResponse restoreVersion(String[] versionIds, String pagePath, int... expectedStatus) throws ClientException {

        FormEntityBuilder feb = FormEntityBuilder.create().addParameter("cmd", CMD_RESTORE_VERSION).addParameter("path", pagePath);

        if (versionIds != null)
            for (String val : versionIds)
                feb.addParameter("id", val);

        return executeWCMCommand(CMD_RESTORE_VERSION, feb, expectedStatus);
    }


    public SlingHttpResponse restoreTree(String path, Date date, boolean preserveNVP, int... expectedStatus) throws ClientException {
        FormEntityBuilder feb = FormEntityBuilder.create()
                .addParameter("cmd", CMD_RESTORE_TREE)
                .addParameter("path", path)
                .addParameter("preserveNVP", Boolean.toString(preserveNVP));

        if (date != null) {
            feb.addParameter("date", TestUtil.ISO_DATETIME_TIME_ZONE_FORMAT.format(date));
        } else {
            feb.addParameter("date", null);
        }

        return executeWCMCommand(CMD_RESTORE_TREE, feb, expectedStatus);
    }


    /**
     * Lock a page
     *
     * @param path           Path of the page to lock
     * @param expectedStatus list of expected http status codes
     * @return the http response
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse lockPage(String path, int... expectedStatus) throws ClientException {

        FormEntityBuilder feb = FormEntityBuilder.create().addParameter("cmd", "lockPage").addParameter("path", path);

        return executeWCMCommand(CMD_LOCK_PAGE, feb, expectedStatus);
    }


    /**
     * Unlock a page
     *
     * @param path           Path of the page to unlock
     * @param expectedStatus list of expected http status codes
     * @return the http response
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse unlockPage(String path, int... expectedStatus) throws ClientException {

        FormEntityBuilder feb = FormEntityBuilder.create().addParameter("cmd", CMD_UNLOCK_PAGE).addParameter("path", path);

        return executeWCMCommand(CMD_UNLOCK_PAGE, feb, expectedStatus);
    }


    /**
     * Create liveCopy
     *
     * @param label           label of the liveCopy
     * @param title           title of the liveCopy
     * @param destPath        destination path for the liveCopy
     * @param srcPath         source path of the liveCopy
     * @param shallow         if set to true subpages are excluded
     * @param rolloutConfigs  rollout configuration
     * @param missingPages    pages which are not rolled out yet
     * @param excludeSubPages if set to true subpages of missing pages are excluded
     * @param expectedStatus  list of expected http status codes
     * @return the http response
     * @throws ClientException if livecopy cannot be created
     */
    public SlingHttpResponse createLiveCopy(String label, String title, String destPath, String srcPath, boolean shallow,
                                            String[] rolloutConfigs, String[] missingPages, boolean excludeSubPages, int... expectedStatus)
            throws ClientException {
        FormEntityBuilder feb = FormEntityBuilder.create()
                .addParameter("cmd", CMD_CREATE_LIVECOPY)
                .addParameter("title", title)
                .addParameter("label", label)
                .addParameter("destPath", destPath)
                .addParameter("srcPath", srcPath)
                .addParameter("missingPage@Delete", "true");

        fillParameters(feb, "missingPage", missingPages);

        feb.addParameter("excludeSubPages@Delete", "true");

        // in case if missing pages are created the parameter excludeSubPages is used
        feb.addParameter("excludeSubPages", Boolean.valueOf(excludeSubPages).toString());
        feb.addParameter("shallow@Delete", "true");
        feb.addParameter("shallow", Boolean.valueOf(shallow).toString());
        feb.addParameter("cq:rolloutConfigs@Delete", "true");
        fillParameters(feb, "cq:rolloutConfigs", rolloutConfigs);

        return executeWCMCommand(CMD_CREATE_LIVECOPY, feb, expectedStatus);
    }


    /**
     * Creates an new site
     *
     * @param label          Label of the new site
     * @param title          Title of the new site
     * @param destPath       Location where the new site gets created
     * @param isLiveCopy     If the new site works as a liveCopy
     * @param languages      What languages to copy
     * @param chapterPages   What chapters to copy
     * @param bluePrintPath  Whats the blueprint used
     * @param rolloutConfigs Whats the rollout configuration for the liveCopy
     * @param siteOwner      Who is the site owner
     * @param expectedStatus list of expected http status codes
     * @return the http response
     * @throws ClientException if site cannot be created
     */
    public SlingHttpResponse createSite(String label, String title, String destPath, boolean isLiveCopy, String[] languages,
                                        String[] chapterPages, String bluePrintPath, String[] rolloutConfigs, String siteOwner,
                                        int... expectedStatus)
            throws ClientException {
        // build the form
        FormEntityBuilder feb = FormEntityBuilder.create();
        // the commmand used
        feb.addParameter("cmd", CMD_CREATE_SITE);
        // the charset of the form content
        feb.addParameter(Constants.PARAMETER_CHARSET, Constants.CHARSET_UTF8);
        // location where site gets created
        feb.addParameter("destPath", destPath);
        // title of the new site
        feb.addParameter("./jcr:title", title);
        // label of the new site
        feb.addParameter("label", label);
        // if its a liveCopy or not of the master site
        feb.addParameter("isLiveCopy@Delete", "true");
        feb.addParameter("isLiveCopy", Boolean.valueOf(isLiveCopy).toString());
        // all languages to be copied
        if (languages != null) {
            feb.addParameter("msm:masterPages@Delete", "true");
            for (String val : languages)
                feb.addParameter("msm:masterPages", val);
        }

        // all chapters to be copied
        if (chapterPages != null) {
            feb.addParameter("msm:chapterPages@Delete", "true");
            fillParameters(feb, "msm:chapterPages", chapterPages);
        }

        if (rolloutConfigs != null) {
            feb.addParameter("cq:rolloutConfigs@Delete", "true");
            fillParameters(feb, "cq:rolloutConfigs", rolloutConfigs);
        }

        // the path to the blueprint
        feb.addParameter("srcPath", bluePrintPath);

        // site owner if set
        feb.addParameter("./cq:siteOwner", siteOwner);

        return executeWCMCommand(CMD_CREATE_SITE, feb, expectedStatus);
    }


    public SlingHttpResponse moderateComment(String commentPath, boolean approve, int... expectedStatus) throws ClientException {
        FormEntityBuilder feb = FormEntityBuilder.create().addParameter("cmd", CMD_MODERATE_COMMENT).addParameter("path", commentPath)
                .addParameter("approve", Boolean.valueOf(approve).toString());

        return executeWCMCommand(CMD_MODERATE_COMMENT, feb, expectedStatus);
    }


    public SlingHttpResponse markCommentAsSpam(String commentPath, boolean spam, int... expectedStatus) throws ClientException {
        FormEntityBuilder feb = FormEntityBuilder.create().addParameter("cmd", CMD_MARK_COMMENT_AS_SPAM).addParameter("path", commentPath)
                .addParameter("spam", Boolean.valueOf(spam).toString());
        return executeWCMCommand(CMD_MARK_COMMENT_AS_SPAM, feb, expectedStatus);
    }


    public SlingHttpResponse deleteComment(String commentPath, int... expectedStatus) throws ClientException {
        FormEntityBuilder feb = FormEntityBuilder.create().addParameter("cmd", CMD_DELETE_COMMENT).addParameter("path", commentPath);

        return executeWCMCommand(CMD_DELETE_COMMENT, feb, expectedStatus);
    }


    /**
     * type deep, delete, page (=deep)
     *
     * @param sourcePaths      the blue print paths
     * @param targetPaths      the live copy paths
     * @param paragraphPaths   the paragraph paths
     * @param type             the type of rollout - deep / shallow
     * @param reset            whether to reset or not the job
     * @param useBackgroundJob if true use a background job, otherwise do not use a
     *                         background job
     * @param expectedStatus   list of expected http status codes
     * @return the response
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse rollout(String[] sourcePaths, String[] targetPaths, String paragraphPaths[], String type, boolean reset,
                                     boolean useBackgroundJob, int... expectedStatus) throws ClientException {
        FormEntityBuilder feb = FormEntityBuilder.create()
                .addParameter("cmd", CMD_ROLLOUT)
                .addParameter("type", type)
                .addParameter("reset", String.valueOf(reset))
                .addParameter("sling:bg", Boolean.valueOf(useBackgroundJob).toString());

        fillParameters(feb, "path", sourcePaths);
        fillParameters(feb, "paras", paragraphPaths);
        fillParameters(feb, "msm:targetPath", targetPaths);
        return executeWCMCommand(CMD_ROLLOUT, feb, expectedStatus);
    }

    /**
     * Creates a new launch copy.
     *
     * @param title                 Title of the new launch section
     * @param srcPath               the root page to be copied from
     * @param liveDate              the date the launch section should go live
     * @param ignoreSubPages        if sub pages should be copied with
     * @param isLiveCopy            if the launch section is connected to the source via live copy
     * @param template              a template if the launch should be created with a different template, null otherwise
     * @param sourceRolloutConfigs  Rollout configs used for the launch live copy
     * @param promoteRolloutConfigs Rollout configs used on launch promotion
     * @param expectedStatus        list of expected http status codes
     * @return the http response
     * @throws ClientException if anything goes wrong
     */
    public SlingHttpResponse createLaunch(String title, String srcPath, Calendar liveDate, boolean ignoreSubPages,
                                          boolean isLiveCopy, String template, String[] sourceRolloutConfigs,
                                          String[] promoteRolloutConfigs, int... expectedStatus)
            throws ClientException {
        ArrayList<String> srcPathList = new ArrayList<>();
        srcPathList.add(srcPath);
        ArrayList<Boolean> ignoreSubPagesList = new ArrayList<>();
        ignoreSubPagesList.add(ignoreSubPages);
        return createLaunch(title, srcPathList, liveDate, ignoreSubPagesList, isLiveCopy, template,
                sourceRolloutConfigs, promoteRolloutConfigs);
    }

    /**
     * Creates a new launch copy.
     *
     * @param title                 Title of the new launch section
     * @param srcPathList           list of the root page to be copied from
     * @param liveDate              the date the launch section should go live
     * @param ignoreSubPagesList    list specifying if sub pages should be copied with
     * @param isLiveCopy            if the launch section is connected to the source via live copy
     * @param template              a template if the launch should be created with a different template, null otherwise
     * @param sourceRolloutConfigs  Rollout configs used for the launch live copy
     * @param promoteRolloutConfigs Rollout configs used on launch promotion
     * @return the http response
     * @throws ClientException if anything goes wrong
     */
    public SlingHttpResponse createLaunch(String title, ArrayList<String> srcPathList, Calendar liveDate,
                                          ArrayList<Boolean> ignoreSubPagesList, boolean isLiveCopy, String template, String[] sourceRolloutConfigs,
                                          String[] promoteRolloutConfigs)
            throws ClientException {

        FormEntityBuilder feb = FormEntityBuilder.create();

        // the command
        feb.addParameter("cmd", CMD_CREATE_LAUNCH);

        // title of new launch section
        feb.addParameter("title", title);

        // where to copy from
        for (String srcPath : srcPathList) {
            feb.addParameter("srcPathList", srcPath);
        }

        // when this launch should go live
        if (liveDate != null) {
            feb.addParameter("liveDate@TypeHint", "Date");
            feb.addParameter("liveDate", TestUtil.ISO_DATETIME_TIME_ZONE_FORMAT.format(liveDate.getTime()));
        }

        // if sub pages should not be copied
        feb.addParameter("shallow@Delete", "true");
        for (Boolean ignoreSubPages : ignoreSubPagesList) {
            feb.addParameter("shallowList", ignoreSubPages.toString());
        }

        // if new launch copy is to be treated as a live copy
        feb.addParameter("isLiveCopy@Delete", "true");
        feb.addParameter("isLiveCopy", Boolean.valueOf(isLiveCopy).toString());

        if (template != null) {
            feb.addParameter("template", template);
        }

        // rollout configs
        fillParameters(feb, "sourceRolloutConfigs", sourceRolloutConfigs);
        fillParameters(feb, "promoteRolloutConfigs", promoteRolloutConfigs);

        return executeWCMCommand(CMD_CREATE_LAUNCH, feb);
    }

    public SlingHttpResponse editLaunch(String launchPath, ArrayList<String> srcPathList,
                                        ArrayList<Boolean> ignoreSubPagesList) throws ClientException {

        FormEntityBuilder feb = FormEntityBuilder.create();

        // the command
        feb.addParameter("cmd", CMD_EDIT_LAUNCH);

        // title of new launch section
        feb.addParameter("path", launchPath);

        // Path which need to be changed
        for (String srcPath : srcPathList) {
            feb.addParameter("srcPathList", srcPath);
        }

        // if sub pages should not be copied
        feb.addParameter("shallow@Delete", "true");
        for (Boolean ignoreSubPages : ignoreSubPagesList) {
            feb.addParameter("shallowList", ignoreSubPages.toString());
        }
        return executeWCMCommand(CMD_EDIT_LAUNCH, feb);
    }

    /**
     * Deletes a launch section
     *
     * @param path           complete path to the launch sections root page handle
     * @param expectedStatus list of expected http status codes
     * @return the sling http response
     * @throws ClientException If anything goes wrong
     */
    public SlingHttpResponse deleteLaunch(String path, int... expectedStatus) throws ClientException {

        FormEntityBuilder feb = FormEntityBuilder.create();

        // the command
        feb.addParameter("cmd", CMD_DELETE_LAUNCH);

        // title of new launch section
        feb.addParameter("path", path);

        return executeWCMCommand(CMD_CREATE_LAUNCH, feb, expectedStatus);
    }


    /**
     * Clones an existing launch section
     *
     * @param orgPath        path to the root page of launch section to copy
     * @param expectedStatus list of expected http status codes
     * @return the sling http response
     * @throws ClientException if anything goes wrong
     */
    public SlingHttpResponse cloneLaunch(String orgPath, int... expectedStatus) throws ClientException {
        FormEntityBuilder feb = FormEntityBuilder.create();

        // the command
        feb.addParameter("cmd", CMD_CLONE_LAUNCH);

        // title of new launch section
        feb.addParameter("path", orgPath);

        return executeWCMCommand(CMD_CLONE_LAUNCH, feb, expectedStatus);
    }

    /**
     * Promotes a launch section
     *
     * @param path            path to the root page of the launch section to promote
     * @param promoteSubPages true if sub pages should be promoted as well
     * @param target          the path to a target launch or null to promote to production
     * @param workflowPackage path to the workflow package or null
     * @param expectedStatus  list of expected http status codes
     * @return the http response
     * @throws ClientException If anything goes wrong
     */
    public SlingHttpResponse promoteLaunch(String path, boolean promoteSubPages, String target, String workflowPackage,
                                           int... expectedStatus)
            throws ClientException {

        FormEntityBuilder feb = FormEntityBuilder.create();

        // the command
        feb.addParameter("cmd", CMD_PROMOTE_LAUNCH);

        // title of new launch section
        feb.addParameter("path", path);

        // check if sub pages need promotion as well
        if (promoteSubPages) {
            feb.addParameter("promotionScope", "deep");
        } else {
            feb.addParameter("promotionScope", "resource");
        }

        if (target != null) {
            feb.addParameter("target", target);
        }

        // if they should be put into a workflow package
        if (workflowPackage != null) {
            feb.addParameter("workflowPackage", workflowPackage);
        }

        return executeWCMCommand(CMD_PROMOTE_LAUNCH, feb, expectedStatus);
    }

    /**
     * Creates a catalog from a blueprint
     *
     * @param source         path to the blueprint
     * @param dest           destination path to the catalog
     * @param label          label of the catalog
     * @param title          title of the catalog
     * @param expectedStatus expected HTTP response status codes
     * @return the http response
     * @throws ClientException if the request could not be executed
     */
    public SlingHttpResponse createCatalog(String source, String dest, String label, String title, int... expectedStatus)
            throws ClientException {

        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter("cmd", CMD_CREATE_CATALOG)
                .addParameter("srcPath", source)
                .addParameter("destPath", dest)
                .addParameter("label", label)
                .addParameter("title", title)
                .addParameter("_charset_", "utf-8");
        return executeWCMCommand(CMD_CREATE_CATALOG, form, expectedStatus);

    }

    /**
     * Rolls out a section
     *
     * @param source         path to blueprint
     * @param dest           destination path to the catalog
     * @param force          whether the rollout should be forced
     * @param expectedStatus expected HTTP response status code
     * @return the http response
     * @throws ClientException if the request could not be executed
     */
    public SlingHttpResponse rolloutSection(String source, String dest, boolean force, int... expectedStatus)
            throws ClientException {

        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter("cmd", CMD_ROLLOUT_SECTION)
                .addParameter("srcPath", source)
                .addParameter("destPath", dest)
                .addParameter("force", force ? "true" : "false")
                .addParameter("_charset_", "utf-8");
        return executeWCMCommand(CMD_ROLLOUT_SECTION, form, expectedStatus);

    }


    private static void fillParameters(FormEntityBuilder formEntityBuilder, String paramName, String... values) {
        if (values != null && values.length > 0) {
            for (String value : values)
                formEntityBuilder.addParameter(paramName, value);
        }
    }

    private SlingHttpResponse executeWCMCommand(String cmd, FormEntityBuilder fb, int... expectedStatus) throws ClientException {
        try {
            // build and execute the request
            return client.doPost("/bin/wcmcommand", fb.build(), expectedStatus);
        } catch (Exception e) {
            // if any exception occurs when sending the request
            throw new ClientException("Sending WCM Command '" + cmd + "' failed!", e);
        }
    }

}
