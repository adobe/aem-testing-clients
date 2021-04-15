/*
 * Copyright 2021 Adobe Systems Incorporated
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

import com.adobe.cq.testing.util.CSRFUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClientConfig;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.FormEntityBuilder;
import org.apache.sling.testing.clients.util.JsonUtils;

import java.io.IOException;
import java.net.URI;

import static org.apache.sling.testing.Constants.CHARSET_UTF8;
import static org.apache.sling.testing.Constants.PARAMETER_CHARSET;


/**
 * Client to create and update the page template.
 * To create a default template use {@link #createDefaultTemplate(String configPath, String title, String Description)}
 * To create a template with give type use {@link #createTemplate(String configPath, String templateType, String title, String Description)}
 * To create Default Container on top level of a template use {@link #createTopLevelDefaultContainer(String templatePath, String name)}
 * To create a Default container in a template use {@link #createDefaultContainer(String templatePath, String location, String name)}
 */
public final class TemplateEditorManagerClient extends CQClient {

    private static final String TEMPLATE_CREATE_ENDPOINT = "/libs/wcm/core/content/sites/createtemplatewizard/_jcr_content";
    private static final String PATH_PATTERN = "<dt class='foundation-form-response-path'>Path</dt>";
    private static final String DD_START_PATTERN = "<dd>";
    private static final String DD_END_PATTERN = "</dd>";

    private static final String PARAM_HIDDEN = "./hidden";
    private static final String PARAM_STATUS = "./status";

    private static final String PARAM_TEMPLATE_TYPE = "templateType";
    private static final String PARAM_COPY_FROM = "./@CopyFrom";
    private static final String PARAM_RESOURCE_TYPE = "./sling:resourceType";
    private static final String PARAM_PARENT_RESOURCE_TYPE = "parentResourceType";
    private static final String PARAM_NAME_HINT = ":nameHint";
    private static final String PARAM_ORDER = ":order";
    private static final String PARAM_LOCK = "lock";
    private static final String PARAM_JCR_PRIMARY_TYPE = "./jcr:primaryType";
    private static final String PARAM_POLICY = "./cq:policy";


    private static final String DEFAULT_TEMPLATE_TYPE = "/libs/settings/wcm/template-types/html5page";
    private static final String POLICIES_RELATIVE_PATH = "%s/settings/wcm/policies";

    private static final String DEFAULT_CONTAINER_RESOURCE_TYPE = "wcm/foundation/components/responsivegrid";

    private static final String PROP_JCR_TITLE = "./jcr:title";
    private static final String PARAM_DESCRIPTION = "description";
    private static final String PARAM_PARENT_PATH = "_parentPath_";

    public TemplateEditorManagerClient(final CloseableHttpClient http, final SlingClientConfig config) throws ClientException {
        super(http, config);
    }

    public TemplateEditorManagerClient(final URI serverUrl, final String user, final String password) throws ClientException {
        super(serverUrl, user, password);
    }

    /**
     * Creates a default page template of type HTML5
     * @param configPath path of config to be used to create a page template
     * @param title title of the template
     * @param description description of the template
     * @return created template path
     * @throws ClientException if the request fails
     * @throws IOException if json parsing fails
     */
    public String createDefaultTemplate(final String configPath, final String title, final String description) throws ClientException,IOException {
        return createTemplate(configPath, DEFAULT_TEMPLATE_TYPE, title, description);
    }

    /**
     * Creates a page template with given template type
     * @param configPath path of config to be used to create a page template
     * @param templateType type of the template
     * @param title title of the template
     * @param description description of the template
     * @return created template path
     * @throws ClientException if the request fails
     * @throws IOException if json parsing fails
     */
    public String createTemplate(final String configPath, final String templateType, final String title, final String description) throws ClientException, IOException {
        FormEntityBuilder formEntry = FormEntityBuilder.create()
                .addParameter(PARAMETER_CHARSET, CHARSET_UTF8)
                .addParameter(CSRFUtils.PARAM_CSRF_TOKEN, CSRFUtils.createCSRFToken(this))
                .addParameter(PROP_JCR_TITLE, title)
                .addParameter(PARAM_DESCRIPTION, description)
                .addParameter(PARAM_HIDDEN, "true")
                .addParameter(PARAM_STATUS, "draft")
                .addParameter(PARAM_PARENT_PATH, configPath)
                .addParameter(PARAM_TEMPLATE_TYPE, templateType)
                .addParameter(PARAM_TEMPLATE_TYPE + "@Delete", "");

        String content = doPost(TEMPLATE_CREATE_ENDPOINT, formEntry.build(), HttpStatus.SC_CREATED).getContent();
        return extractPath(content);
    }

    /**
     * Creates default layout container (responsivegrid) on the provided location of the provided template
     *
     * @param templatePath: path of the template the layout container should be created, i.e. /conf/myConfig/settings/wcm/templates/myTemplate
     * @param location: the location the layout container should be created, i.e. /structure/jcr:content/root/
     * @param nameHint: name hint for the layout container or null to create one
     * @return path of the created layout container
     * @throws ClientException if the request fails
     */
    public String createDefaultContainer(String templatePath, String location, String nameHint) throws ClientException {
        if (nameHint == null) nameHint = "responsivegrid";
        FormEntityBuilder formEntry = FormEntityBuilder.create()
                .addParameter(PARAM_COPY_FROM, "/libs/" + DEFAULT_CONTAINER_RESOURCE_TYPE + "/cq:template")
                .addParameter(PARAMETER_CHARSET, CHARSET_UTF8)
                .addParameter(PARAM_RESOURCE_TYPE, DEFAULT_CONTAINER_RESOURCE_TYPE)
                .addParameter(PARAM_PARENT_RESOURCE_TYPE, DEFAULT_CONTAINER_RESOURCE_TYPE)
                .addParameter(PARAM_ORDER, "last")
                .addParameter(PARAM_NAME_HINT, nameHint);

        location = StringUtils.prependIfMissing(location, "/");
        String parentPath = templatePath + StringUtils.appendIfMissing(location, "/");

        SlingHttpResponse exec = doPost(parentPath, formEntry.build(), HttpStatus.SC_CREATED);
        return exec.getSlingPath();
    }


    /**
     * Creates a default layout container (responsivegrid) on structure top level of the provided template
     *
     * @param templatePath: path of the template the layout container should be created, i.e. /conf/myConfig/settings/wcm/templates/myTemplate
     * @param nameHint: name hint for the layout container or null to create one
     * @return path of the created layout container
     * @throws ClientException if the request fails
     */
    public String createTopLevelDefaultContainer(String templatePath, String nameHint) throws ClientException {
        return createDefaultContainer(templatePath, "/structure/jcr:content/root/", nameHint);
    }

    /**
     * Enables a existing template
     * @param templatePath path of the template to be enabled
     * @throws ClientException if the request fails
     */
    public void enable(final String templatePath) throws ClientException {
        setPageProperty(templatePath, PARAM_STATUS, "enabled", HttpStatus.SC_OK);
    }

    /**
     * Disables an existing template
     * @param templatePath path of the template to be disabled
     * @throws ClientException if the request fails
     */
    public void disable(final String templatePath) throws ClientException {
        setPageProperty(templatePath, PARAM_STATUS, "disabled", HttpStatus.SC_OK);
    }

    /**
     * Update the policy of an existing CQConfig with the provides policy
     * @param configPath path of the config which needs to be updated
     * @param jsonString policy to be updated in json format
     * @return updated config path
     * @throws ClientException if the request fails
     */
    public String importPolicy(final String configPath, final String jsonString) throws ClientException {
        return importJson(
                String.format(POLICIES_RELATIVE_PATH, configPath),
                JsonUtils.getJsonNodeFromString(jsonString),
                HttpStatus.SC_CREATED
        ).getSlingPath();
    }

    /**
     * Unlock a component in the structure tree of a template
     *
     * @param componentPath: path of the component to be unlocked
     * @throws ClientException if the request fails
     */
    public void unlockStructureComponent(final String componentPath) throws ClientException {
        setStructureComponentLock(componentPath, false);
    }

    /**
     * Lock / unlock a component in the structure tree of a template
     *
     * @param componentPath: path of the component to be locked / unlocked
     * @param isLocked: true to lock the component, false to unlock the component
     * @throws ClientException if the request fails
     */
    public void setStructureComponentLock(String componentPath, boolean isLocked) throws ClientException {
        String resourcePath = "/bin/wcm/template/sync.html" + componentPath;
        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter(PARAM_LOCK, "" + isLocked);

        this.doPost(resourcePath, form.build(), HttpStatus.SC_OK, HttpStatus.SC_CREATED).getContent();
    }

    /**
     * Set the policy mapping for a component
     * Note: The componentPath is the path to the component in the template structure tree except for components in a
     * unlocked layout container, here the component path is the path to the layout container component in the template
     * structure tree plus the component resource type, i.e. for a foundation text component in unlocked main layout container:
     * /conf/myConfig/settings/wcm/templates/myTemplate/structure/jcr:content/root/responsivegrid/wcm/foundation/components/text
     *
     * @param componentPath: the path of the component for which the policy should be set
     * @param policyPath: the path of the policy to be set for the component, i.e. wcm/foundation/components/responsivegrid/default
     * @return path of the created/updated component policy
     * @throws ClientException if the request fails
     */
    public String setComponentPolicy(String componentPath, String policyPath) throws ClientException {
        String resourcePath = StringUtils.replaceOnce(componentPath, "structure", "policies");
        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter(PARAM_JCR_PRIMARY_TYPE, "nt:unstructured")  // set primary type for folder node
                .addParameter(PARAM_RESOURCE_TYPE, getPolicyMappingResourceType(componentPath))
                .addParameter(PARAM_POLICY, policyPath);

        SlingHttpResponse exec = this.doPost(resourcePath, form.build(), HttpStatus.SC_OK, HttpStatus.SC_CREATED);
        return exec.getSlingPath();
    }

    /**
     * Get the resource type of the policy mapping node depending if the component is a page component or not (note: page
     * components use different resource type in policy mapping than all other components)
     *
     * @param componentPath: the path of the component for which the policy should be set
     * @return resource type of the policy mapping
     */
    private String getPolicyMappingResourceType(String componentPath) {
        return componentPath.endsWith("/jcr:content") ? "wcm/core/components/policies/mappings" : "wcm/core/components/policies/mapping";
    }

    private String extractPath(final String content) {
        int indexPath = content.indexOf(PATH_PATTERN);
        assert indexPath > 0;
        int indexDDStart = content.indexOf(DD_START_PATTERN, indexPath);
        assert indexDDStart > 0;
        int indexDDEnd = content.indexOf(DD_END_PATTERN, indexDDStart);
        assert indexDDEnd > 0;
        return content.substring(indexDDStart + DD_START_PATTERN.length(), indexDDEnd);
    }

}