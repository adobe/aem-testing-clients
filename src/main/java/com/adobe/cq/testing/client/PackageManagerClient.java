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

import org.apache.commons.lang3.NotImplementedException;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClientConfig;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.FormEntityBuilder;
import org.apache.sling.testing.clients.util.HttpUtils;
import org.apache.sling.testing.clients.util.ResourceUtil;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.http.HttpStatus.SC_OK;

/**
 * <p>The PackageManagerClient encapsulates operations on content packages just like
 * those available in the {@code /crx/packmgr/index.jsp} application. It
 * also uses the same API to invoke them (HTTP requests).</p>
 * 
 * <p>Provides methods to create a new empty package or upload one from the local disk</p>
 *
 * To manipulate an existing package, use {@link PackageManagerClient.Package}, which can be obtained
 * using {@link #getPackage(String path)}.
 */
public class PackageManagerClient extends CQClient {

    public static class Package {

        PackageManagerClient pm;
        String path;
        String name;
        String version;
        boolean versionUpdated;
        String group;
        String description;
        String filter;
        Date created;
        Date lastModified;
        Date lastWrapped;
        String lastWrappedBy;
        Date lastUnwrapped;
        String lastUnwrappedBy;
        Date lastUnpacked;
        String lastUnpackedBy;
        Boolean requiresRestart;
        Boolean requiresRoot;
        Integer buildCount;
        String builtWith;

        protected Package(PackageManagerClient pm, String name, String version, String group) {
            init(name, version, group, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        }

        protected Package(PackageManagerClient pm, String path) throws ClientException {
            this.pm = pm;
            initAll(path);
        }
        /**
         * Build factory from a definition dump. This is intended for testing purposes only.
         * @param definitionJson the string with the definition json
         * @return a new Package object
         */
        public static Package build(final String definitionJson) throws ClientException {
            Package p = new Package(null, "dummyName", "dummyVersion", "dummyGroup");
            p.initFromJson(definitionJson);
            return p;
        }

        private void initAll(String path) throws ClientException {
            checkValidPackagePath(path);
            initFromJson(getDefinition(path));
        }

        private void checkValidPackagePath(final String path) {
            String regexp = "/etc/packages/([^/]+)/([^\\-.]+)-?(.*).zip";
            Matcher re = Pattern.compile(regexp).matcher(path);
            if (!re.matches()) {
                throw new IllegalArgumentException("The path supplied does not look like a path to a package. It is expected to match " + regexp);
            }
        }

        private void initFromJson(String json) throws ClientException {
            JsonNode node;
            try {
                ObjectMapper mapper = new ObjectMapper();
                node = mapper.readTree(json);
            } catch (JsonProcessingException e) {
                throw new ClientException("Unable to parse package properties json: " + json, e);
            } catch (IOException e) {
                throw new ClientException("Unable to read package properties.", e);
            }

            init(
                    getJsonStringSafely(node, "name"),
                    getJsonStringSafely(node, "version"),
                    getJsonStringSafely(node, "group"),
                    getJsonStringSafely(node, "jcr:description"),
                    getJsonStringSafely(node, "filter"),
                    getJsonDateSafely(node, "jcr:created"),
                    getJsonDateSafely(node, "jcr:lastModified"),
                    getJsonDateSafely(node, "lastWrapped"),
                    getJsonStringSafely(node, "lastWrappedBy"),
                    getJsonDateSafely(node, "lastUnwrapped"),
                    getJsonStringSafely(node, "lastUnwrappedBy"),
                    getJsonDateSafely(node, "lastUnpacked"),
                    getJsonStringSafely(node, "lastUnpackedBy"),
                    getJsonBooleanSafely(node, "requiresRestart"),
                    getJsonBooleanSafely(node, "requiresRoot"),
                    getJsonIntegerSafely(node, "buildCount"),
                    getJsonStringSafely(node, "builtWith"));
        }

        private void init(
                String name,
                String version,
                String group,
                String description,
                String filter,
                Date created,
                Date lastModified,
                Date lastWrapped,
                String lastWrappedBy,
                Date lastUnwrapped,
                String lastUnwrappedBy,
                Date lastUnpacked,
                String lastUnpackedBy,
                Boolean requiresRestart,
                Boolean requiresRoot,
                Integer buildCount,
                String builtWith) {
            this.name = name;
            this.version = version;
            this.group = group;
            this.description = description;
            this.filter = filter;
            this.path = buildPath();
            this.created = created;
            this.lastModified = lastModified;
            this.lastWrapped = lastWrapped;
            this.lastWrappedBy = lastWrappedBy;
            this.lastUnwrapped = lastUnwrapped;
            this.lastUnwrappedBy = lastUnwrappedBy;
            this.lastUnpacked = lastUnpacked;
            this.lastUnpackedBy = lastUnpackedBy;
            this.requiresRestart = requiresRestart;
            this.requiresRoot = requiresRoot;
            this.buildCount = buildCount;
            this.builtWith = builtWith;
            this.versionUpdated = false;
        }



        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Package aPackage = (Package) o;

            if (path != null ? !path.equals(aPackage.path) : aPackage.path != null) return false;
            if (name != null ? !name.equals(aPackage.name) : aPackage.name != null) return false;
            if (version != null ? !version.equals(aPackage.version) : aPackage.version != null) return false;
            if (group != null ? !group.equals(aPackage.group) : aPackage.group != null) return false;
            if (description != null ? !description.equals(aPackage.description) : aPackage.description != null) return false;
            if (filter != null ? !filter.equals(aPackage.filter) : aPackage.filter != null) return false;
            if (requiresRestart != null ? !requiresRestart.equals(aPackage.requiresRestart) : aPackage.requiresRestart != null)
                return false;
            if (requiresRoot != null ? !requiresRoot.equals(aPackage.requiresRoot) : aPackage.requiresRoot != null) return false;
            //noinspection SimplifiableIfStatement
            if (buildCount != null ? !buildCount.equals(aPackage.buildCount) : aPackage.buildCount != null) return false;
            return !(builtWith != null ? !builtWith.equals(aPackage.builtWith) : aPackage.builtWith != null);
        }

        @Override
        public int hashCode() {
            int result = path != null ? path.hashCode() : 0;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + (version != null ? version.hashCode() : 0);
            result = 31 * result + (group != null ? group.hashCode() : 0);
            result = 31 * result + (description != null ? description.hashCode() : 0);
            result = 31 * result + (filter != null ? filter.hashCode() : 0);
            result = 31 * result + (requiresRestart != null ? requiresRestart.hashCode() : 0);
            result = 31 * result + (requiresRoot != null ? requiresRoot.hashCode() : 0);
            result = 31 * result + (buildCount != null ? buildCount.hashCode() : 0);
            result = 31 * result + (builtWith != null ? builtWith.hashCode() : 0);
            return result;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getFilter() {
            return filter;
        }

        public void setFilter(String filter) {
            this.filter = filter;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
            versionUpdated = true;
        }

        public String getPath() {
            return path;
        }

        public Integer getBuildCount() {
            return buildCount;
        }

        public String getBuiltWith() {
            return builtWith;
        }

        public Date getCreated() {
            return created;
        }

        public Date getLastModified() {
            return lastModified;
        }

        public Date getLastUnpacked() {
            return lastUnpacked;
        }

        public String getLastUnpackedBy() {
            return lastUnpackedBy;
        }

        public Date getLastUnwrapped() {
            return lastUnwrapped;
        }

        public String getLastUnwrappedBy() {
            return lastUnwrappedBy;
        }

        public Date getLastWrapped() {
            return lastWrapped;
        }

        public String getLastWrappedBy() {
            return lastWrappedBy;
        }

        public Boolean getRequiresRestart() {
            return requiresRestart;
        }

        public Boolean getRequiresRoot() {
            return requiresRoot;
        }

        public String buildPath() {
            if (getName() == null || "".equals(getName())) {
                throw new NotImplementedException("Package name is not set.");
            }
            if (getGroup() == null || "".equals(getGroup())) {
                throw new NotImplementedException("Package group is not set.");
            }
            if (getVersion() == null || "".equals(getVersion())) {
                return String.format("/etc/packages/%s/%s.zip", getGroup(), getName());
            }
            return String.format("/etc/packages/%s/%s-%s.zip", getGroup(), getName(), getVersion());
        }

        public String getDefinition(final String path) throws ClientException {
            return pm.doGet(path + "/jcr:content/vlt:definition.9.json", SC_OK).getContent();
        }

        /**
         * Commit all property changes to the server.
         * @return The HTML response.
         * @throws ClientException if the request failed
         */
        public String update() throws ClientException {
            MultipartEntityBuilder meb = MultipartEntityBuilder.create();
            meb.addTextBody("path", getPath());
            meb.addTextBody("packageName", getName());
            meb.addTextBody("groupName", getGroup());
            if (getVersion() != null) {
                meb.addTextBody("version", getVersion());
            }
            if (getDescription() != null) {
                meb.addTextBody("description", getDescription());
            }
            if (getFilter() != null) {
                meb.addTextBody("filter", getFilter());
            }
            meb.addTextBody("_charset_", "UTF-8");
            SlingHttpResponse exec = pm.doPost("/crx/packmgr/update.jsp", meb.build(), 200);
            if (versionUpdated) {
                this.path = buildPath(); // the path changes with the version number
                versionUpdated = false;
            }
            return exec.getContent();
        }

        /**
         * Build the package.
         * @return The HTML response.
         * @throws ClientException if the request failed
         */
        public String build() throws ClientException {
            String content = doScriptCmd("build").getContent();
            initAll(getPath());
            return content;
        }

        /**
         * Install the package.
         * @return The HTML response.
         * @throws ClientException if the request failed
         */
        public String install() throws ClientException {
            FormEntityBuilder feb = FormEntityBuilder.create();
            feb.addParameter("cmd", "install");
            feb.addParameter("autosave", "1024");
            feb.addParameter("recursive", "true");
            feb.addParameter("acHandling", "");
            SlingHttpResponse exec = pm.doPost("/crx/packmgr/service/script.html" + getPath(), feb.build(), 200);
            String content = exec.getContent();
            initAll(getPath());
            return content;
        }

        /**
         * Replicate the package.
         * @return The HTML response.
         * @throws ClientException if the request failed
         */
        public String replicate() throws ClientException {
            return doScriptCmd("replicate").getContent();
        }

        /**
         * Simulate package installation. No changes are made to the repository.
         * @return The HTML response.
         * @throws ClientException if the request failed
         */
        public String testInstall() throws ClientException {
            return doScriptCmd("dryrun").getContent();
        }

        /**
         * Uninstall the package.
         * @return The HTML response.
         * @throws ClientException if the request failed
         */
        public String unInstall() throws ClientException {
            String content = doScriptCmd("uninstall").getContent();
            initAll(getPath());
            return content;
        }

        /**
         * Rewrap the package.
         * @return The HTML response.
         * @throws ClientException if the request failed
         */
        public String rewrap() throws ClientException {
            String content = doScriptCmd("rewrap").getContent();
            initAll(getPath());
            return content;
        }

        /**
         * Get a description of the package contents.
         * @return The package contents list as an HTML response.
         * @throws ClientException if the request failed
         */
        public String getPackageContentsAsHtml() throws ClientException {
            return checkStatus(doScriptCmd("contents")).getContent();
        }

        private SlingHttpResponse checkStatus(SlingHttpResponse exec) throws ClientException {
            String content = exec.getContent();
            String statusMessage = content.replaceFirst("^.*\\(\\{\"success\":[^,]*,\"msg\":\"[^\"]*\"}\\).*$", "\\1");
            if (!statusMessage.contains("\"success\":true"))
                throw new ClientException("The get contents request returned an error:\n" + statusMessage);
            return exec;
        }

        private SlingHttpResponse doScriptCmd(String cmd) throws ClientException {
            FormEntityBuilder feb = FormEntityBuilder.create();
            feb.addParameter("cmd", cmd);
            return pm.doPost("/crx/packmgr/service/script.html" + getPath(), feb.build(), SC_OK);
        }

        /**
         * Get a description of the package coverage.
         * @return The package coverage as an HTML response.
         * @throws ClientException if the request failed
         */
        public String getPackageCoverageAsHtml() throws ClientException {
            return checkStatus(doScriptCmd("preview")).getContent();
        }

        /**
         * Delete the package. This package instance becomes invalid and should not be used anymore after completion of the request.
         * @return The HTML response.
         * @throws ClientException if the request failed
         */
        public String delete() throws ClientException {
            return doScriptCmd("delete").getContent();
        }

        private static String getJsonStringSafely(JsonNode node, String attr) {
            try {
                return node.get(attr).getValueAsText();
            } catch (Exception e) {
                return null;
            }
        }

        private static Date getJsonDateSafely(JsonNode node, String attr) {
            try {
                String dateAsString = node.get(attr).getTextValue();
                return new SimpleDateFormat("E MMM dd yyyy HH:mm:ss 'GMT'z").parse(dateAsString);
            } catch (Exception e) {
                return null;
            }
        }

        private static Integer getJsonIntegerSafely(JsonNode node, String attr) {
            try {
                return Integer.parseInt(node.get(attr).getValueAsText());
            } catch (Exception e) {
                return null;
            }
        }

        private static Boolean getJsonBooleanSafely(JsonNode node, String attr) {
            try {
                return Boolean.parseBoolean(node.get(attr).getValueAsText());
            } catch (Exception e) {
                return null;
            }
        }
    }

    public PackageManagerClient(CloseableHttpClient http, SlingClientConfig config) throws ClientException {
        super(http, config);
    }

    public PackageManagerClient(URI serverUrl, String user, String password) throws ClientException {
        super(serverUrl, user, password);
    }

    public Package createPackage(String name, String version, String group) throws ClientException {
        FormEntityBuilder feb = FormEntityBuilder.create();
        feb.addParameter("cmd", "create");
        feb.addParameter("_charset_", "utf-8");
        feb.addParameter("groupName", group);
        feb.addParameter("packageName", name);
        if (version != null) {
            feb.addParameter("packageVersion", version);
        }
        doPost("/crx/packmgr/service/exec.json", feb.build(), SC_OK);
        return new Package(this, name, version, group);
    }

    public Package getPackage(String path) throws ClientException {
        return new Package(this, path);
    }

    public Package uploadPackage(InputStream is, String fileName) throws ClientException {
        HttpEntity mpe = MultipartEntityBuilder.create()
                .addPart("package", new InputStreamBody(is, fileName))
                .addTextBody("_charset_", "UTF-8")
                .build();

        SlingHttpResponse exec = doPost("/crx/packmgr/service/exec.json?cmd=upload&jsonInTextarea=true", mpe, SC_OK);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root;
        try {
            root = mapper.readTree(exec.getContent().replaceAll("</?textarea>", ""));
        } catch (Exception ex) {
            throw new ClientException("Unable to parse JSON response to upload request.", ex);
        }
        if (!root.get("success").getBooleanValue()) {
            throw new ClientException(root.get("msg").getTextValue());
        }
        return new Package(this, root.get("path").getTextValue());
    }

    //Bellow is another set of methods for that are used to manage packages
    /**
     * Creates a new package.
     *
     * @param packageName name of the package.
     * @param packageVersion version of the package.
     * @param groupName name of the group where the package will be created.
     * @param expectedStatus list of accepted statuses
     * @return the response of post
     * @throws ClientException if the request failed
     */
    public SlingHttpResponse createPackage(String packageName, String packageVersion, String groupName, int... expectedStatus)
            throws ClientException {
        FormEntityBuilder feb = FormEntityBuilder.create()
                .addParameter("_charset_", "utf-8")
                .addParameter("packageName", packageName)
                .addParameter("packageVersion", packageVersion)
                .addParameter("groupName", groupName);

        return doPost("/crx/packmgr/service/exec.json?cmd=create", feb.build(), HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Deletes a package.
     *
     * @param packageName Name of the package.
     * @param packageVersion Package version.
     * @param groupName Package group name.
     * @param expectedStatus list of accepted statuses
     * @return the response
     * @throws ClientException if the request failed
     */
    public SlingHttpResponse deletePackage(String packageName, String packageVersion, String groupName, int... expectedStatus)
            throws ClientException {
        String url = "/crx/packmgr/service/script.html/etc/packages/" + groupName + "/" + packageName + "-" + packageVersion + ".zip";
        MultipartEntityBuilder multiPartEntity = MultipartEntityBuilder.create()
                .addTextBody("cmd", "delete")
                .addTextBody("callback", "window.parent.Ext.Ajax.Stream.callback");

        return doPost(url, multiPartEntity.build(), HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Renames a package.
     *
     * @param oldName The old package name.
     * @param oldVersion The old package version.
     * @param oldGroup The old package group.
     * @param newName The The new name for the package.
     * @param newVersion The new version for the package.
     * @param newGroup The new group for the package
     * @param expectedStatus list of accepted statuses
     * @return the response
     * @throws ClientException if the request failed
     */
    public SlingHttpResponse renamePackages(String oldName, String oldVersion, String oldGroup, String newName, String newVersion,
                                            String newGroup, int... expectedStatus) throws ClientException {
        String oldPath = "/etc/packages/" + oldGroup + "/" + oldName + "-" + oldVersion + ".zip";
        HttpEntity multiPartEntity = MultipartEntityBuilder.create()
                .addTextBody("path", oldPath)
                .addTextBody("packageName", newName)
                .addTextBody("groupName", newGroup)
                .addTextBody("version", newVersion)
                .addTextBody("_charset_", "UTF-8")
                .build();

        return doPost("/crx/packmgr/update.jsp", multiPartEntity, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Checks if the package is created.
     *
     * @param packageName Name of the package.
     * @param packageVersion Name of the version.
     * @param groupName Name of the group.
     * @return True if the package is created or false otherwise.
     * @throws ClientException if the request failed
     */
    public boolean isPackageCreated(String packageName, String packageVersion, String groupName) throws ClientException {
        return exists("/etc/packages/" + groupName + "/" + packageName + "-" + packageVersion + ".zip");

    }

    /**
     * Builds the specific package.
     *
     * @param packageName Name of the package.
     * @param packageVersion Version for the package.
     * @param groupName Group name for the package.
     * @param expectedStatus list of accepted statuses
     * @return the response
     * @throws ClientException if the request failed
     */
    public SlingHttpResponse buildPackage(String packageName, String packageVersion, String groupName, int... expectedStatus)
            throws ClientException {
        String postURL = "/crx/packmgr/service/script.html/etc/packages/" + groupName + "/" + packageName + "-" + packageVersion + ".zip";

        HttpEntity multiPartEntity = MultipartEntityBuilder.create()
                .addTextBody("cmd", "build")
                .addTextBody("callback", "window.parent.Ext.Ajax.Stream.callback")
                .build();
        return doPost(postURL, multiPartEntity, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Re-wraps the specific package.
     *
     * @param packageName Name of the package.
     * @param packageVersion Version for the package.
     * @param groupName Group name for the package.
     * @param expectedStatus List of expected statuses
     * @return The response
     * @throws ClientException if the request failed
     */
    public SlingHttpResponse rewrapPackage(String packageName, String packageVersion, String groupName, int... expectedStatus)
            throws ClientException {
        String postURL = "/crx/packmgr/service/script.html/etc/packages/" + groupName + "/" + packageName + "-" + packageVersion + ".zip";

        HttpEntity multiPartEntity = MultipartEntityBuilder.create()
                .addTextBody("cmd", "rewrap")
                .addTextBody("callback", "window.parent.Ext.Ajax.Stream.callback")
                .build();
        return doPost(postURL, multiPartEntity, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Installs the specific package.
     *
     * @param packageName Name of the package.
     * @param packageVersion Version for the package.
     * @param groupName Group name for the package.
     * @param expectedStatus List of expected statuses
     * @return the response
     * @throws ClientException if the request failed
     */
    public SlingHttpResponse installPackage(String packageName, String packageVersion, String groupName, int... expectedStatus)
            throws ClientException {
        String postURL = "/crx/packmgr/service/script.html/etc/packages/" + groupName + "/" + packageName + "-" + packageVersion + ".zip";
        HttpEntity multiPartEntity = MultipartEntityBuilder.create()
                .addTextBody("cmd", "install")
                .addTextBody("callback", "window.parent.Ext.Ajax.Stream.callback")
                .addTextBody("autosave", "1024")
                .addTextBody("recursive", "true")
                .build();
        return doPost(postURL, multiPartEntity, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Uninstall the specific package.
     *
     * @param packageName Name of the package.
     * @param packageVersion Version for the package.
     * @param groupName Group name for the package.
     * @param expectedStatus list of expected status
     * @return the response
     * @throws ClientException if the request failed
     */
    public SlingHttpResponse uninstallPackage(String packageName, String packageVersion, String groupName, int... expectedStatus)
            throws ClientException {
        String postURL = "/crx/packmgr/service/script.html/etc/packages/" + groupName + "/" + packageName + "-" + packageVersion + ".zip";
        HttpEntity multiPartEntity = MultipartEntityBuilder.create()
                .addTextBody("cmd", "uninstall")
                .addTextBody("callback", "window.parent.Ext.Ajax.Stream.callback")
                .build();
        return doPost(postURL, multiPartEntity, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Test installation for the specific package.
     *
     * @param packageName Name of the package.
     * @param packageVersion Version for the package.
     * @param groupName Group name for the package.
     * @param expectedStatus list of expected statuses
     * @return the response
     * @throws ClientException if the request failed
     */
    public SlingHttpResponse testInstallPackage(String packageName, String packageVersion, String groupName, int... expectedStatus)
            throws ClientException {
        String postURL = "/crx/packmgr/service/script.html/etc/packages/" + groupName + "/" + packageName + "-" + packageVersion + ".zip";
        HttpEntity multiPartEntity = MultipartEntityBuilder.create()
                .addTextBody("cmd", "dryrun")
                .addTextBody("callback", "window.parent.Ext.Ajax.Stream.callback")
                .build();
        return doPost(postURL, multiPartEntity, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Uploads a package.
     *
     * @param resourcePath File's path.
     * @param fileName Name of the file.
     * @param expectedStatus list of expected statuses
     * @return the response
     * @throws ClientException if the request failed
     */
    public SlingHttpResponse uploadPackage(String resourcePath, String fileName, int... expectedStatus)
            throws ClientException {
        String postURL = "/crx/packmgr/service/exec.json?cmd=upload";
        HttpEntity multiPartEntity = MultipartEntityBuilder.create()
                .addBinaryBody("package", ResourceUtil.getResourceAsStream(resourcePath),
                        ContentType.create("application/x-zip-compressed"), fileName)
                .addTextBody("force", "true")
                .addTextBody("_charset_", "UTF-8")
                .build();
        return doPost(postURL, multiPartEntity, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Returns the HTTP response for the package content request.
     *
     * @param packageName name of the package
     * @param packageVersion version of the package
     * @param groupName group of the package
     *
     * @return the response
     * @throws ClientException if the request failed
     */
    public String getPackageContentResponse(String packageName, String packageVersion, String groupName)
            throws ClientException {
        String postURL = "/crx/packmgr/service/script.html/etc/packages/" + groupName + "/" + packageName + "-" + packageVersion + ".zip";
        HttpEntity multiPartEntity = MultipartEntityBuilder.create()
                .addTextBody("cmd", "contents")
                .addTextBody("callback", "window.parent.Ext.Ajax.Stream.callback")
                .build();
        return doPost(postURL, multiPartEntity).getContent();
    }

    /**
     * Returns the HTTP response for the package content request.
     *
     * @param packageName name of the package
     * @param packageVersion version of the package
     * @param groupName group of the package
     *
     * @return the response
     * @throws ClientException if the request failed
     */
    public String getPackageCoverageResponse(String packageName, String packageVersion, String groupName)
            throws ClientException {
        String postURL = "/crx/packmgr/service/script.html/etc/packages/" + groupName + "/" + packageName + "-" + packageVersion + ".zip";
        HttpEntity multiPartEntity = MultipartEntityBuilder.create()
                .addTextBody("cmd", "preview")
                .addTextBody("callback", "window.parent.Ext.Ajax.Stream.callback")
                .build();
        return doPost(postURL, multiPartEntity).getContent();

    }

    /**
     * Updates the thumbnail for a specific package.
     *
     * @param packageName Name of the package
     * @param packageVersion Version of the package
     * @param groupName Name of the group
     * @param resourcePath Path to the thumbnail
     * @param fileName Name of the thumbnail
     * @param expectedStatus list of expected statuses
     * @return the response
     * @throws ClientException if the request failed
     */
    public SlingHttpResponse updateThumbnail(String packageName, String packageVersion, String groupName, String resourcePath,
                                             String fileName, int... expectedStatus) throws ClientException {
        String postURL = "/crx/packmgr/update.jsp";
        HttpEntity multiPartEntity = MultipartEntityBuilder.create()
                .addTextBody("path", "/etc/packages/" + groupName + "/" + packageName + "-" + packageVersion + ".zip")
                .addTextBody("packageName", packageName)
                .addTextBody("groupName", groupName)
                .addTextBody("version", packageVersion)
                .addTextBody("_charset_", "UTF-8")
                .addBinaryBody("thumbnail", ResourceUtil.getResourceAsStream(resourcePath), ContentType.create("image/jpeg"), fileName)
                .build();
        return doPost(postURL, multiPartEntity, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Uploads a screenshot for a package.
     *
     * @param packageName name of the package
     * @param packageVersion version of the package
     * @param groupName group of the package
     * @param resourcePath path to the thumbnail
     * @param fileName name of the thumbnail
     * @param expectedStatus list of expected statuses
     *
     * @return the response
     * @throws ClientException if the request failed
     */
    public SlingHttpResponse uploadPictureAsScreenshot(String packageName, String packageVersion, String groupName, String resourcePath,
                                                       String fileName, int... expectedStatus)
            throws ClientException {
        String postURL = "/crx/packmgr/update.jsp";
        HttpEntity multiPartEntity = MultipartEntityBuilder.create()
                .addTextBody("path", "/etc/packages/" + groupName + "/" + packageName + "-" + packageVersion + ".zip")
                .addTextBody("packageName", packageName)
                .addTextBody("groupName", groupName)
                .addTextBody("version", packageVersion)
                .addTextBody("_charset_", "UTF-8")
                .addBinaryBody("screenshot", ResourceUtil.getResourceAsStream(resourcePath), ContentType.create("image/jpeg"), fileName)
                .addTextBody("screenshotConfig", "[{\"upload\":true}])")
                .build();
        return doPost(postURL, multiPartEntity, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

}
