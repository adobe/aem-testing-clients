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

import java.net.URI;

import static org.apache.sling.testing.Constants.CHARSET_UTF8;
import static org.apache.sling.testing.Constants.PARAMETER_CHARSET;


/**
 * Client to create page templates.
 */
public final class TemplateEditorManagerClient extends CQClient {

    private static final String CREATE_ENDPOINT = "/libs/wcm/core/content/sites/createtemplatewizard/_jcr_content";
    private static final String PATH_PATTERN = "<dt class='foundation-form-response-path'>Path</dt>";
    private static final String DD_START_PATTERN = "<dd>";
    private static final String DD_END_PATTERN = "</dd>";

    public static final String PARAM_HIDDEN = "./hidden";
    public static final String PARAM_STATUS = "./status";

    private static final String PARAM_TEMPLATE_TYPE = "templateType";
    private static final String DEFAULT_HTML5_TYPE = "/libs/settings/wcm/template-types/html5page";
    private static final String POLICIES_RELATIVE_PATH = "%s/settings/wcm/policies";

    private static final String WCM_FOUNDATION_COMPONENTS_RESPONSIVEGRID = "wcm/foundation/components/responsivegrid";

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
     * Creates a HTML5 type page template
     * @param configPath path of config to be used to create a page template
     * @param title title of the template
     * @param description description of the template
     * @return created template path
     * @throws ClientException
     */
    public String createHTML5(final String configPath, final String title, final String description) throws ClientException {
        return create(configPath, DEFAULT_HTML5_TYPE, title, description);
    }

    /**
     * Creates a page template with given template type
     * @param configPath path of config to be used to create a page template
     * @param templateType type of the template
     * @param title title of the template
     * @param description description of the template
     * @return created template path
     * @throws ClientException
     */
    public String create(final String configPath, final String templateType, final String title, final String description) throws ClientException {
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

        String content = doPost(CREATE_ENDPOINT, formEntry.build(), HttpStatus.SC_CREATED).getContent();
        return extractPath(content);
    }

    /**
     * Create a layout container on the provided location of the provided template
     *
     * @param templatePath: path of the template the layout container should be created, i.e. /conf/myConfig/settings/wcm/templates/myTemplate
     * @param location: the location the layout container should be created, i.e. /structure/jcr:content/root/
     * @param nameHint: name hint for the layout container or null to create one
     * @return path of the created layout container
     * @throws ClientException
     */
    public String createResponsiveGrid(String templatePath, String location, String nameHint) throws ClientException {
        if (nameHint == null) nameHint = "responsivegrid";
        FormEntityBuilder formEntry = FormEntityBuilder.create()
                .addParameter("./@CopyFrom", "/libs/" + WCM_FOUNDATION_COMPONENTS_RESPONSIVEGRID + "/cq:template")
                .addParameter(PARAMETER_CHARSET, CHARSET_UTF8)
                .addParameter("./sling:resourceType", WCM_FOUNDATION_COMPONENTS_RESPONSIVEGRID)
                .addParameter("parentResourceType", WCM_FOUNDATION_COMPONENTS_RESPONSIVEGRID)
                .addParameter(":order", "last")
                .addParameter(":nameHint", nameHint);

        location = StringUtils.prependIfMissing(location, "/");
        String parentPath = templatePath + StringUtils.appendIfMissing(location, "/");

        SlingHttpResponse exec = doPost(parentPath, formEntry.build(), HttpStatus.SC_CREATED);
        return exec.getSlingPath();
    }

    /**
     * Create a layout container on structure top level of the provided template
     *
     * @param templatePath: path of the template the layout container should be created, i.e. /conf/myConfig/settings/wcm/templates/myTemplate
     * @param nameHint: name hint for the layout container or null to create one
     * @return path of the created layout container
     * @throws ClientException
     */
    public String createTopLevelResponsiveGrid(String templatePath, String nameHint) throws ClientException {
        return createResponsiveGrid(templatePath, "/structure/jcr:content/root/", nameHint);
    }

    /**
     * Enables a existing template
     * @param templatePath path of the template to be enabled
     * @throws ClientException
     */
    public void enable(final String templatePath) throws ClientException {
        setPageProperty(templatePath, PARAM_STATUS, "enabled", HttpStatus.SC_OK);
    }

    /**
     * Disables an existing template
     * @param templatePath path of the template to be disabled
     * @throws ClientException
     */
    public void disable(final String templatePath) throws ClientException {
        setPageProperty(templatePath, PARAM_STATUS, "disabled", HttpStatus.SC_OK);
    }

    /**
     * Update the policy of an existing CQConfig with the provides policy
     * @param configPath path of the config which needs to be updated
     * @param jsonString policy to be updated in json format
     * @return updated config path
     * @throws ClientException
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
     * @throws ClientException
     */
    public void unlockStructureComponent(final String componentPath) throws ClientException {
        setStructureComponentLock(componentPath, false);
    }

    /**
     * Lock / unlock a component in the structure tree of a template
     *
     * @param componentPath: path of the component to be locked / unlocked
     * @param isLocked: true to lock the component, false to unlock the component
     * @throws ClientException
     */
    public void setStructureComponentLock(String componentPath, boolean isLocked) throws ClientException {
        String resourcePath = "/bin/wcm/template/sync.html" + componentPath;
        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter("lock", "" + isLocked);

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
     * @throws ClientException
     */
    public String setComponentPolicy(String componentPath, String policyPath) throws ClientException {
        String resourcePath = StringUtils.replaceOnce(componentPath, "structure", "policies");
        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter("./jcr:primaryType", "nt:unstructured")  // set primary type for folder node
                .addParameter("./sling:resourceType", getPolicyMappingResourceType(componentPath))
                .addParameter("./cq:policy", policyPath);

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