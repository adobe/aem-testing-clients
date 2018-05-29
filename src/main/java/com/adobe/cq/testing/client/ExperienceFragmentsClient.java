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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClientConfig;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.FormEntityBuilder;
import org.apache.sling.testing.clients.util.HttpUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.*;

/**
 * CQ Testing client for Experience Fragments
 */
public class ExperienceFragmentsClient extends CQClient {
    private static final Gson GSON = new Gson();
    private static final Type LIST_STRING_TYPE = new TypeToken<List<String>>() {}.getType();

    private static final String WEB_TEMPLATE_PATH = "/libs/settings/experience-fragments/templates/experience-fragment-template";
    private static final String FACEBOOK_TEMPLATE_PATH = "/libs/settings/experience-fragments/templates/experience-fragment-template-facebook";
    private static final String PINTEREST_TEMPLATE_PATH = "/libs/settings/experience-fragments/templates/experience-fragment-template-pinterest";
    //private static final String POS_TEMPLATE_PATH = "/libs/settings/experience-fragments/templates/experience-fragment-template-pos";

    private static final String EXPERIENCE_FRAGMENTS_TAG = "experience-fragments:";
    private static final String VARIATION_TAG = EXPERIENCE_FRAGMENTS_TAG + "variation";
    private static final String WEB_TAG_ID = VARIATION_TAG + "/web";
    private static final String FACEBOOK_TAG_ID = VARIATION_TAG + "/facebook";
    private static final String PINTEREST_TAG_ID = VARIATION_TAG + "/pinterest";
    //private static final String POS_TAG_ID = VARIATION_TAG + "/pos";

    private static final String WEB_XF_VARIANT_TYPE = "web";
    private static final String FACEBOOK_XF_VARIANT_TYPE = "facebook";
    private static final String PINTEREST_XF_VARIANT_TYPE = "pinterest";
    //private static final String POS_XF_VARIANT_TYPE = "pos";
    private static final String CUSTOM_XF_VARIANT_TYPE = "custom";

    private static final String PROPERTIES_PATH_PREFIX = "/mnt/overlay/wcm/core/content/sites/properties.html?item=";

    public static final String DEFAULT_XF_PARENT_PATH = "/content/experience-fragments";

    private static final String REFERENCES_SERVLET = "/libs/cq/experience-fragments/content/commons/reference.json";
    private static final String CONFIGURATOR = "/libs/cq/experience-fragments/content/dialogs/configdialog/configurator.html";

    /**
     * Interface with methods that all predefined XF templates from the XF_TEMPLATE must implement
     */
    public interface XFTemplate {
        /**
         * Path to the template
         * @return the path to the template
         */
        String path();

        /**
         * A {@link VariantComponents} subclass object that can be used to configure the components defined in that template
         * @param xfClient a {@link ExperienceFragmentsClient} instance
         * @param variantPath the path to the experience fragment variant defined with this template
         * @param <T> The subclass of {@link VariantComponents} to which the result should be casted to
         * @return A {@link VariantComponents} subclass object that can be used to configure the components defined in that template
         */
        <T extends VariantComponents> T getComponents(ExperienceFragmentsClient xfClient, String variantPath);

        /**
         * This template creates a social variant
         * @return {@code true} if the template creates a social variant
         */
        boolean isSocialTemplate();

        /**
         * Template tags
         *
         * @return the list of tags
         */
        List<String> tags();

        /**
         * Template's cq:xfVariantType value
         *
         * @return the variant type
         */
        String variantType();
    }

    /**
     * Interface with methods that all predefined XF tags from the XF_TAG must implement
     */
    public interface XFTag {
        String tagID();
    }

    /**
     * Base class for classes mapped to predefined templates that enable consumers to configure variant components from that template
     */
    public static class VariantComponents {
        protected String variantPath;
        protected ExperienceFragmentsClient xfClient;

        public VariantComponents(ExperienceFragmentsClient xfClient, String variantPath) {
            this.xfClient = xfClient;
            this.variantPath = variantPath;
        }
    }

    public interface TextVariantComponents {
        TextComponent getTextComponent();
    }

    public interface ImageVariantComponents {
        ImageComponent getImageComponent();
    }

    public interface ContentFragmentVariantCompoents {
        ContentFragmentComponent getContentFragment();
    }

    /**
     * Class that enable consumers to configure variant components from the POS template
     */
    public static class POSVariantComponents extends VariantComponents
            implements TextVariantComponents, ContentFragmentVariantCompoents {
        private ContentFragmentComponent contentFragmentComponent;
        private TextComponent textComponent;

        public POSVariantComponents(ExperienceFragmentsClient xfClient, String variantPath) {
            super(xfClient, variantPath);

            this.contentFragmentComponent = new ContentFragmentComponent(xfClient, variantPath);
            this.textComponent = new TextComponent(xfClient, variantPath);
        }

        @Override
        public ContentFragmentComponent getContentFragment() {
            return contentFragmentComponent;
        }

        @Override
        public TextComponent getTextComponent() {
            return textComponent;
        }
    }

    /**
     * Class that enable consumers to configure variant components from the Pinterest template
     */
    public static class PinterestVariantComponents extends VariantComponents
            implements TextVariantComponents, ImageVariantComponents, ContentFragmentVariantCompoents {
        private ImageComponent imageComponent;
        private TextComponent textComponent;
        private ContentFragmentComponent contentFragmentComponent;

        public PinterestVariantComponents(ExperienceFragmentsClient xfClient, String variantPath) {
            super(xfClient, variantPath);

            this.imageComponent = new ImageComponent(xfClient, variantPath);
            this.textComponent = new TextComponent(xfClient, variantPath);
            this.contentFragmentComponent = new ContentFragmentComponent(xfClient, variantPath);
        }

        @Override
        public ContentFragmentComponent getContentFragment() {
            return contentFragmentComponent;
        }

        @Override
        public ImageComponent getImageComponent() {
            return imageComponent;
        }

        @Override
        public TextComponent getTextComponent() {
            return textComponent;
        }
    }

    /**
     * Class that enable consumers to configure variant components from the Facebook template
     */
    public static class FacebookVariantComponents extends VariantComponents
            implements TextVariantComponents, ImageVariantComponents, ContentFragmentVariantCompoents {
        private ImageComponent imageComponent;
        private TextComponent textComponent;
        private ContentFragmentComponent contentFragmentComponent;

        public FacebookVariantComponents(ExperienceFragmentsClient xfClient, String variantPath) {
            super(xfClient, variantPath);

            this.imageComponent = new ImageComponent(xfClient, variantPath);
            this.textComponent = new TextComponent(xfClient, variantPath);
            this.contentFragmentComponent = new ContentFragmentComponent(xfClient, variantPath);
        }

        @Override
        public ContentFragmentComponent getContentFragment() {
            return contentFragmentComponent;
        }

        @Override
        public ImageComponent getImageComponent() {
            return imageComponent;
        }

        @Override
        public TextComponent getTextComponent() {
            return textComponent;
        }
    }

    /**
     * Experience Fragments predefined tags
     */
    public enum XF_TAG implements XFTag {
        EXPERIENCE_FRAGMENTS {
            @Override
            public String tagID() {
                return EXPERIENCE_FRAGMENTS_TAG;
            }
        },
        VARIATION {
            @Override
            public String tagID() {
                return VARIATION_TAG;
            }
        },
        WEB {
            @Override
            public String tagID() {
                return WEB_TAG_ID;
            }
        },
        FACEBOOK {
            @Override
            public String tagID() {
                return FACEBOOK_TAG_ID;
            }
        },
        PINTEREST {
            @Override
            public String tagID() {
                return PINTEREST_TAG_ID;
            }
        }/*,
        POS {
            @Override
            public String tagID() {
                return POS_TAG_ID;
            }
        }  */
    }



    /**
     * Experience Fragments predefined templates
     */
    public enum XF_TEMPLATE implements XFTemplate {
        WEB {
            public String path() {
                return WEB_TEMPLATE_PATH;
            }

            @Override
            public <T extends VariantComponents> T getComponents(ExperienceFragmentsClient xfClient, String variantPath) {
                throw new UnsupportedOperationException("Cannot determine components for " + WEB);
            }

            @Override
            public boolean isSocialTemplate() {
                return false;
            }

            @Override
            public List<String> tags() {
                return Collections.singletonList(XF_TAG.WEB.tagID());
            }

            @Override
            public String variantType() {
                return WEB_XF_VARIANT_TYPE;
            }
        },
        FACEBOOK {
            public String path() {
                return FACEBOOK_TEMPLATE_PATH;
            }

            @Override
            public <T extends VariantComponents> T getComponents(ExperienceFragmentsClient xfClient, String variantPath) {
                //noinspection unchecked
                return (T) new FacebookVariantComponents(xfClient, variantPath);
            }

            @Override
            public boolean isSocialTemplate() {
                return true;
            }

            @Override
            public List<String> tags() {
                return Collections.singletonList(XF_TAG.FACEBOOK.tagID());
            }

            @Override
            public String variantType() {
                return FACEBOOK_XF_VARIANT_TYPE;
            }
        },
        PINTEREST {
            public String path() {
                return PINTEREST_TEMPLATE_PATH;
            }

            @Override
            public <T extends VariantComponents> T getComponents(ExperienceFragmentsClient xfClient, String variantPath) {
                //noinspection unchecked
                return (T) new PinterestVariantComponents(xfClient, variantPath);
            }

            @Override
            public boolean isSocialTemplate() {
                return true;
            }

            @Override
            public List<String> tags() {
                return Collections.singletonList(XF_TAG.PINTEREST.tagID());
            }

            @Override
            public String variantType() {
                return PINTEREST_XF_VARIANT_TYPE;
            }
        }/*,
        POS {
            public String path() {
                return POS_TEMPLATE_PATH;
            }

            @Override
            public <T extends VariantComponents> T getComponents(ExperienceFragmentsClient xfClient, String variantPath) {
                return (T) new POSVariantComponents(xfClient, variantPath);
            }

            @Override
            public boolean isSocialTemplate() {
                return false;
            }

            @Override
            public List<String> tags() {
                return Arrays.asList(XF_TAG.POS.tagID());
            }

            @Override
            public String variantType() {
                return POS_XF_VARIANT_TYPE;
            }
        }*/,
        CUSTOM {
            @Override
            public String path() {
                throw new UnsupportedOperationException("Cannot determine path for custom templates through Enum");
            }

            @Override
            public <T extends VariantComponents> T getComponents(ExperienceFragmentsClient xfClient, String variantPath) {
                throw new UnsupportedOperationException("Cannot determine components for custom templates");
            }

            @Override
            public boolean isSocialTemplate() {
                return false;
            }

            @Override
            public List<String> tags() {
                throw new UnsupportedOperationException("Cannot determine tags for custom templates");
            }

            @Override
            public String variantType() {
                return CUSTOM_XF_VARIANT_TYPE;
            }
        }
    }

    private static final String XF_CREATE_WIZARD = "/libs/cq/experience-fragments/content/v2/experience-fragments/createxfwizard/_jcr_content";
    private static final String XF_VARIANT_CREATE_WIZARD = "/libs/cq/experience-fragments/content/dialogs/createvariation/creator";

    private static final String CONVERT_TO_XF = "/libs/cq/experience-fragments/content/v2/conversion/converter";

    public ExperienceFragmentsClient(CloseableHttpClient http, SlingClientConfig config) throws ClientException {
        super(http, config);
    }

    public ExperienceFragmentsClient(URI serverUrl, String user, String password) throws ClientException {
        super(serverUrl, user, password);
    }

    /**
     * Create an {@link ExperienceFragmentBuilder}
     * @param xfTitle title for the experience fragment
     * @param variantTitle title for the experience fragment variant
     * @param variantTemplate template for the experience fragment variant
     * @return a new {@link ExperienceFragmentBuilder}
     */
    public ExperienceFragmentBuilder experienceFragmentBuilder(String xfTitle, String variantTitle, String variantTemplate) {
        return new ExperienceFragmentBuilder(this, xfTitle, variantTitle, variantTemplate);
    }

    /**
     * Create an {@link ExperienceFragmentBuilder}
     * @param xfTitle title for the experience fragment
     * @param variantTitle title for the experience fragment variant
     * @param variantTemplate template for the experience fragment variant
     * @return a new {@link ExperienceFragmentBuilder}
     */
    public ExperienceFragmentBuilder experienceFragmentBuilder(String xfTitle, String variantTitle, XF_TEMPLATE variantTemplate) {
        return experienceFragmentBuilder(xfTitle, variantTitle, variantTemplate.path());
    }

    /**
     * Create an Experience Fragment
     * @param xfTitle title for the experience fragment
     * @param variantTitle title for the experience fragment variant
     * @param variantTemplate template for the experience fragment variant
     * @param expectedStatus Http status expected after creation of the experience fragment
     * @return The full {@link SlingHttpResponse} for the experience fragment create request
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse createExperienceFragment(String xfTitle, String variantTitle, String variantTemplate, int... expectedStatus) throws ClientException {
        return new ExperienceFragmentBuilder(this, xfTitle, variantTitle, variantTemplate).create(expectedStatus);
    }

    /**
     * Create an Experience Fragment
     * @param xfTitle title for the experience fragment
     * @param variantTitle title for the experience fragment variant
     * @param variantTemplate template for the experience fragment variant
     * @param expectedStatus http status expected after creation of the experience fragment
     * @return The full {@link SlingHttpResponse} for the experience fragment create request
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse createExperienceFragment(String xfTitle, String variantTitle, XF_TEMPLATE variantTemplate, int... expectedStatus) throws ClientException {
        return createExperienceFragment(xfTitle, variantTitle, variantTemplate.path(), expectedStatus);
    }

    /**
     * Create an {@link ExperienceFragmentVariantBuilder}
     * @param parentPath the parent experience fragment path
     * @param template the variant template
     * @param title the variant title
     * @return a new {@link ExperienceFragmentVariantBuilder}
     */
    public ExperienceFragmentVariantBuilder xfVariantBuilder(String parentPath, String template, String title) {
        return new ExperienceFragmentVariantBuilder(this, parentPath, template, title);
    }

    /**
     * Create an {@link ExperienceFragmentVariantBuilder}
     * @param parentPath the parent experience fragment path
     * @param template the variant template
     * @param title the variant title
     * @return a new {@link ExperienceFragmentVariantBuilder}
     */
    public ExperienceFragmentVariantBuilder xfVariantBuilder(String parentPath, XF_TEMPLATE template, String title) {
        return xfVariantBuilder(parentPath, template.path(), title);
    }

    /**
     * Create an Experience Fragment Variant
     * @param parentPath the parent experience fragment path
     * @param template the variant template
     * @param title the variant title
     * @param expectedStatus http status expected after creation of the experience fragment variant
     * @return The full {@link SlingHttpResponse} for the experience fragment variant create request
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse createXfVariant(String parentPath, String template, String title, int... expectedStatus) throws ClientException {
        return new ExperienceFragmentVariantBuilder(this, parentPath, template, title).create(expectedStatus);
    }

    /**
     * Create an Experience Fragment Variant
     * @param parentPath the parent experience fragment path
     * @param template the variant template
     * @param title the variant title
     * @param expectedStatus http status expected after creation of the experience fragment variant
     * @return The full {@link SlingHttpResponse} for the experience fragment variant create request
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse createXfVariant(String parentPath, XF_TEMPLATE template, String title, int... expectedStatus) throws ClientException {
        return createXfVariant(parentPath, template.path(), title, expectedStatus);
    }

    /**
     * Create a Live Copy of an Experience Fragment Variant
     * The new variant will be created under the same Experience Fragment
     * @param sourceVariantPath the path of the source experience fragment variant
     * @param title the live copy variant's title
     * @param name the live copy variant's name
     * @param rolloutConfigs the rollout configurations for the live copy
     * @param expectedStatus http status expected after creation of the experience fragment variant live copy
     * @return The full {@link SlingHttpResponse} for the experience fragment variant live copy create request
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse createVariationAsLiveCopy(String sourceVariantPath, String title,
                                                       String name, String[] rolloutConfigs, int... expectedStatus) throws ClientException {
        return wcmCommands.createLiveCopy(name, title, getParentXFPath(sourceVariantPath),
                sourceVariantPath, true, rolloutConfigs, null, true, expectedStatus);
    }

    /**
     * Force delete an Experience Fragment
     * @param path the path of the Experience Fragment
     * @param expectedStatus http status expected after deleting the experience fragment
     * @return The full {@link SlingHttpResponse} for the experience fragment delete request
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse deleteExperienceFragment(String path, int... expectedStatus) throws ClientException {
        return deleteExperienceFragment(path, true, expectedStatus);
    }

    /**
     * Delete an Experience Fragment
     * @param path the path of the Experience Fragment
     * @param force force delete
     * @param expectedStatus http status expected after deleting the experience fragment
     * @return The full {@link SlingHttpResponse} for the experience fragment delete request
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse deleteExperienceFragment(String path, boolean force, int... expectedStatus) throws ClientException {
        HttpEntity entity = FormEntityBuilder.create()
                .addParameter("_charset_", "UTF-8")
                .addParameter("cmd","deletePage")
                .addParameter("path", path)
                .addParameter("force", String.valueOf(force))
                .addParameter("checkChildren", String.valueOf(true))
                .build();

        return doPost("/bin/wcmcommand", entity, expectedStatus);
    }

    /**
     * Delete an Experience Fragment Variant
     * @param variantPath the path of the Experience Fragment Variant
     * @param expectedStatus http status expected after deleting the experience fragment variant
     * @return The full {@link SlingHttpResponse} for the experience fragment variant delete request
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse deleteXfVariant(String variantPath, int... expectedStatus) throws ClientException {
        HttpEntity entity = FormEntityBuilder.create()
                .addParameter("variationPath", variantPath)
                .build();

        return doPost(getParentXFPath(variantPath) + "/jcr:content.deletevariation.json", entity,HttpUtils.getExpectedStatus(HttpStatus.SC_OK, expectedStatus));
    }

    /**
     * Get the Experience Fragment as an object of {@link ExperienceFragment} class
     * @param xfPath the path of the Experience Fragment
     * @return an object of {@link ExperienceFragment} class
     * @throws ClientException if the request fails
     */
    public ExperienceFragment getExperienceFragment(String xfPath) throws ClientException {
        return new ExperienceFragment(this, xfPath);
    }

    /**
     * Get the Experience Fragment Variant as an object of {@link ExperienceFragmentVariant} class
     * @param variantPath the path of the Experience Fragment Variant
     * @return an object of {@link ExperienceFragmentVariant} class
     * @throws ClientException if the request fails
     */
    public ExperienceFragmentVariant getXFVariant(String variantPath) throws ClientException {
        return new ExperienceFragmentVariant(this, variantPath);
    }

    /**
     * Call the Plain Html Processor for an Experience Fragment Variant
     * @param variantPath the path for the of Experience Fragment Variant
     * @param expectedStatus http status expected after calling the the Plain Html Processor for the variant
     * @return The full {@link SlingHttpResponse} for the experience fragment variant plain html processor request.
     *  The HTML rendition can be obtained by calling {@code SlingHttpResponse.getContent()} method
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse getPlainHtmlRendering(String variantPath, int... expectedStatus) throws ClientException {
        return doGet(variantPath + ".plain.html",  HttpUtils.getExpectedStatus(HttpStatus.SC_OK, expectedStatus));
    }

    /**
     * Get the Social Urls for the a Social Experience Fragment Variant
     * @param variantPath the path for the Social Experience Fragment Variant
     * @param expectedStatus http status expected after requesting the social urls for the social variant
     * @return The full {@link SlingHttpResponse} after requesting the social urls for the social variant
     *  The Social Urls are in JSON format and can be obtained by calling {@code SlingHttpResponse.getContent()} method
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse getSocialUrls(String variantPath, int... expectedStatus) throws ClientException {
        return doGet(variantPath + ".socialurls.json", HttpUtils.getExpectedStatus(HttpStatus.SC_OK, expectedStatus));
    }

    /**
     * Initiate a Convert to Experience Fragment
     * @return a new instance of {@link ConvertToXFAction}
     */
    public ConvertToXFAction convertToXF() {
        return new ConvertToXFAction(this);
    }

    public SlingHttpResponse getXFPropertiesPageHTHML(String xfPath, int... expectedStatus) throws ClientException {
        return doGet(PROPERTIES_PATH_PREFIX + xfPath, HttpUtils.getExpectedStatus(HttpStatus.SC_OK, expectedStatus));
    }

    public SlingHttpResponse getVariantProperitesPageHTML(String variantPath, int... expectedStatus) throws ClientException {
        return doGet(PROPERTIES_PATH_PREFIX + variantPath, HttpUtils.getExpectedStatus(HttpStatus.SC_OK, expectedStatus));
    }

    /**
     * Get the current Configuration for Experience Fragments
     * @return the current configuration
     * @throws ClientException if the configuration cannot be retrieved
     */
    public ExperienceFragmentsConfiguration getCurrentConfiguration() throws ClientException {
        return new ExperienceFragmentsConfiguration(this);
    }

    /**
     * Create a new Experience Fragments configuration builder
     * @return the builder
     * @throws ClientException if the builder cannot be created
     */
    public ExperienceFragementsConfigurationBuilder configurationBuilder() throws ClientException {
        return new ExperienceFragementsConfigurationBuilder(this);
    }

    private List<String> getPageReferences(String pagePath) throws ClientException {

        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter("_charset_", "UTF-8")
                .addParameter("path", pagePath);
        SlingHttpResponse content = this.doPost(REFERENCES_SERVLET, form.build(), HttpStatus.SC_OK);

        List<String> paths = new ArrayList<>();

        JsonArray references = GSON.fromJson(content.getContent(), JsonObject.class)
                .get("assets")
                .getAsJsonArray();

        for(JsonElement reference : references) {
            paths.add(((JsonObject)reference).get("path").getAsString());
        }

        return paths;

    }

    private SlingHttpResponse replicate(String cmd, String pagePath, int... expectedStatus) throws ClientException {
        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter("_charset_", "UTF-8")
                .addParameter("cmd", cmd)
                .addParameter("path", pagePath);

        List<String> references = this.getPageReferences(pagePath);
        for(String reference : references) {
            form.addParameter("path", reference);
        }

        return this.doPost("/bin/replicate", form.build(), HttpUtils.getExpectedStatus(HttpStatus.SC_OK, expectedStatus));
    }


    /**
     * Publish a page and all the paths that are referenced to a publish instance.
     *
     * @param pagePath the page to be published
     * @return SlingHttpResponse object
     */
    private SlingHttpResponse publish(String pagePath, int... expectedStatus) throws ClientException {
        return replicate("Activate", pagePath);
    }

    public SlingHttpResponse unpublish(String pagePath, int... expectedStatus) throws ClientException {
        return replicate("Deactivate", pagePath);
    }

    public SlingHttpResponse publishXFVariant(String variantPath, int... expectedStatus) throws ClientException {
        return publish(variantPath, expectedStatus);
    }

    public SlingHttpResponse publishXF(String xfPath, int... expectedStatus) throws ClientException {
        return publish(xfPath, expectedStatus);
    }

    public SlingHttpResponse unpublishXFVariant(String variantPath, int ... expectedStatus) throws ClientException {
        return unpublish(variantPath, expectedStatus);
    }

    public SlingHttpResponse unblishXF(String xfPath, int... expectedStatus) throws ClientException {
        return unpublish(xfPath, expectedStatus);
    }

    /**
     * Get the path of the parent Experience Fragment, given the path of the Experience Fragment Variant
     * @param variantPath the path of the Experience Fragment Variant
     * @return The path of the parent Experience Fragment
     */
    public static String getParentXFPath(String variantPath) {
        return variantPath.substring(0, variantPath.lastIndexOf("/"));
    }

    /**
     * A {@link VariantComponents} subclass object that can be used to configure the variant components defined in the template
     * @param variantPath the path of the Experience Fragment Variant
     * @param template the template of the Experience Fragment Variant
     * @param <T> The subclass of {@link VariantComponents} to which the result should be casted to
     * @return A {@link VariantComponents} subclass object that can be used to configure the variant components defined in the template
     * @throws Exception if the components can not be retrieved
     */
    public <T extends VariantComponents > T getComponents(String variantPath, XF_TEMPLATE template) throws Exception {
        return template.getComponents(this, variantPath);
    }

    /**
     * An Experience Fragment builder
     */
    public class ExperienceFragmentBuilder {
        private ExperienceFragmentsClient xfClient;

        private String variantTemplate;
        private String parentPath = DEFAULT_XF_PARENT_PATH;
        private String xfName;
        private String xfTitle;
        private String xfDescription;
        private List<String> xfTags = new ArrayList<>();
        private String variantName;
        private String variantTitle;
        private List<String> variantTags = new ArrayList<>();

        /**
         * Constructor
         * @param client the Experience Fragment client
         * @param xfTitle the title for the Experience Fragment
         * @param variantTitle the title for the Experience Fragment Variant
         * @param variantTemplate the template for the Experience Fragment Variant
         */
        public ExperienceFragmentBuilder(ExperienceFragmentsClient client, String xfTitle, String variantTitle, String variantTemplate) {
            this.xfTitle = xfTitle;
            this.variantTitle = variantTitle;
            this.variantTemplate = variantTemplate;
            this.xfClient = client;
        }

        /**
         * Set the parent path
         * @param parentPath parent path
         * @return this
         */
        public ExperienceFragmentBuilder withParentPath(String parentPath) {
            this.parentPath = parentPath;
            return this;
        }

        /**
         * Set the Experience Fragment name
         * @param xfName name
         * @return this
         */
        public ExperienceFragmentBuilder withXFName(String xfName) {
            this.xfName = xfName;
            return this;
        }

        /**
         * Set the Experience Fragment description
         * @param description experience fragment description
         * @return this
         */
        public ExperienceFragmentBuilder withXFDescription(String description) {
            this.xfDescription = description;
            return this;
        }

        /**
         * Add a tag to the Experience Fragment
         * @param xfTag tag to be added
         * @return this
         */
        public ExperienceFragmentBuilder addXFTag(String xfTag) {
            this.xfTags.add(xfTag);
            return this;
        }

        /**
         * Add tags to the Experience Fragment
         * @param xfTags list of tags to be added
         * @return this
         */
        public ExperienceFragmentBuilder addXFTags(List<String> xfTags) {
            this.xfTags.addAll(xfTags);
            return this;
        }

        /**
         * Set the Experience Fragment Variant name
         * @param variantName variant name
         * @return this
         */
        public ExperienceFragmentBuilder withVariantName(String variantName) {
            this.variantName = variantName;
            return this;
        }

        /**
         * Add a tag to the Experience Fragment Variant
         * @param variantTag variant tag
         * @return this
         */
        public ExperienceFragmentBuilder addVariantTag(String variantTag) {
            this.variantTags.add(variantTag);
            return this;
        }

        /**
         * Add tags to the Experience Fragment Variant
         * @param variantTags list of variant tags
         * @return this
         */
        public ExperienceFragmentBuilder addVariantTags(List<String> variantTags) {
            this.variantTags.addAll(variantTags);
            return this;
        }

        /**
         * Create the Experience Fragment with the information configured in this builder object
         * @param expectedStatus http status expected after sending the Experience Fragment create request
         * @return The full {@link SlingHttpResponse} for the experience fragment create request
         * @throws ClientException if the request fails
         */
        public SlingHttpResponse create(int... expectedStatus) throws ClientException {
            FormEntityBuilder entityBuilder = FormEntityBuilder.create()
                    .addParameter("parentPath", parentPath)
                    .addParameter("pageTitle", xfTitle)
                    .addParameter("pageName", xfName)
                    .addParameter("./jcr:description", xfDescription)
                    .addParameter("variantTitle", variantTitle)
                    .addParameter("variantName", variantName)

                    .addParameter("variantTemplate", variantTemplate);

            for (String xfTag : xfTags) {
                entityBuilder.addParameter("./cq:tags", xfTag);
            }

            for (String variantTag : variantTags) {
                entityBuilder.addParameter("variantTags", variantTag);
            }

            return xfClient.doPost(XF_CREATE_WIZARD, entityBuilder.build(), HttpUtils.getExpectedStatus(HttpStatus.SC_CREATED, expectedStatus));
        }
    }

    /**
     * An Experience Fragment Variant builder
     */
    public static class ExperienceFragmentVariantBuilder {
        private ExperienceFragmentsClient client;

        private String parentPath;
        private String template;
        private String title;
        private String name;
        private List<String> tags = new ArrayList<>();
        private String description;

        /**
         * Constructor
         * @param client Experience Fragment client
         * @param parentPath parent Experience Fragment path
         * @param template variant template
         * @param title variant title
         */
        public ExperienceFragmentVariantBuilder(ExperienceFragmentsClient client, String parentPath, String template, String title) {
            this.client = client;
            this.parentPath = parentPath;
            this.template = template;
            this.title = title;
        }

        /**
         * Set variant name
         * @param name variant name
         * @return this
         */
        public ExperienceFragmentVariantBuilder withName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Set variant description
         * @param description variant description
         * @return this
         */
        public ExperienceFragmentVariantBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        /**
         * Add tag to the variant
         * @param tag variant tag
         * @return this
         */
        public ExperienceFragmentVariantBuilder addTag(String tag) {
            this.tags.add(tag);
            return this;
        }

        /**
         * Add tags to the variants
         * @param tags list of variant tags
         * @return this
         */
        public ExperienceFragmentVariantBuilder addTags(List<String> tags) {
            this.tags.addAll(tags);
            return this;
        }

        /**
         * Create the Experience Fragment Variant with the information configured in this builder object
         * @param expectedStatus http status expected after sending the Experience Fragment Variant create request
         * @return The full {@link SlingHttpResponse} for the experience fragment variant create request
         * @throws ClientException if the request fails
         */
        public SlingHttpResponse create(int... expectedStatus) throws ClientException {
            FormEntityBuilder entityBuilder = FormEntityBuilder.create()
                    .addParameter("parentPath", parentPath)
                    .addParameter("template", template)
                    .addParameter("pageName", name)
                    .addParameter("./jcr:title", title)
                    .addParameter("./jcr:description", description);

            for(String tag : tags) {
                entityBuilder.addParameter("./cq:tags", tag);
            }

            return client.doPost(XF_VARIANT_CREATE_WIZARD, entityBuilder.build(), HttpUtils.getExpectedStatus(HttpStatus.SC_CREATED, expectedStatus));
        }
    }

    /**
     * Convert a component to an Experience Fragment
     */
    public static class ConvertToXFAction {
        ExperienceFragmentsClient client;

        public ConvertToXFAction(ExperienceFragmentsClient client) {
            this.client = client;
        }

        /**
         * Convert and create a new Experience Fragment
         *
         * @param componentPath component path
         * @param parentPath Experience Fragment parent path
         * @param xfTitle Experience Fragment title
         * @param xfTags Experience Fragment tags
         * @param variantTitle Experience Fragment Variant title
         * @param variantTemplate Experience Fragment Variant template
         * @param variantTags Experience Fragment Variant tags
         * @param expectedStatus http status expected after sending the convert request
         * @return The full {@link SlingHttpResponse} for the convert request
         * @throws ClientException if the request fails
         */
        public SlingHttpResponse createNewExperienceFragment(String componentPath,
                                                             @Nullable String parentPath,
                                                             String xfTitle,
                                                             @Nullable List<String> xfTags,
                                                             String variantTitle,
                                                             String variantTemplate,
                                                             @Nullable List<String> variantTags,
                                                             int... expectedStatus) throws ClientException {
            FormEntityBuilder entityBuilder = FormEntityBuilder.create()
                    .addParameter("action", "createXf")
                    .addParameter("componentPath", componentPath)
                    .addParameter("xfParentPath", parentPath != null ? parentPath :  DEFAULT_XF_PARENT_PATH)
                    .addParameter("pageTitle", xfTitle)
                    .addParameter("variantTemplate", variantTemplate)
                    .addParameter("variantTitle", variantTitle);

            if(xfTags != null) {
                for (String xfTag : xfTags) {
                    entityBuilder.addParameter("cq:tags", xfTag);
                }
            }

            if (variantTags != null) {
                for (String variantTag : variantTags) {
                    entityBuilder.addParameter("variantTags", variantTag);
                }
            }

            return client.doPost(CONVERT_TO_XF, entityBuilder.build(), HttpUtils.getExpectedStatus(HttpStatus.SC_CREATED, expectedStatus));
        }

        /**
         * Convert by creating a new Experience Fragment
         * @param componentPath Experience Fragment component path
         * @param parentPath Experience Fragment parent path
         * @param xfTitle Experience Fragment title
         * @param xfTags Experience Fragment tags
         * @param variantTitle Experience Fragment Variant title
         * @param variantTemplate Experience Fragment Variant template
         * @param variantTags Experience Fragment Variant tags
         * @param expectedStatus http status expected after sending the convert request
         * @return The full {@link SlingHttpResponse} for the convert request
         * @throws ClientException if the request fails
         */
        public SlingHttpResponse createNewExperienceFragment(String componentPath, String parentPath, String xfTitle, List<String> xfTags, String variantTitle,
                                                             XF_TEMPLATE variantTemplate, List<String> variantTags, int... expectedStatus) throws ClientException {
            return createNewExperienceFragment(componentPath, parentPath, xfTitle, xfTags, variantTitle, variantTemplate.path(), variantTags, expectedStatus);
        }

        /**
         * Convert and creating a new Experience Fragment Variant
         * @param componentPath path to the component that will converted to a variant
         * @param parentXFPath parent Experience Fragment path
         * @param variantTitle Experience Fragment Variant title
         * @param variantTemplate Experience Fragment Variant template
         * @param variantTags Experience Fragment Variant tags
         * @param expectedStatus http status expected after sending the convert request
         * @return The full {@link SlingHttpResponse} for the convert request
         * @throws ClientException if the request fails
         */
        public SlingHttpResponse addToAnExistingXF(String componentPath,
                                                   String parentXFPath,
                                                   String variantTitle,
                                                   String variantTemplate,
                                                   @Nullable List<String> variantTags,
                                                   int... expectedStatus) throws ClientException {
            FormEntityBuilder entityBuilder = FormEntityBuilder.create()
                    .addParameter("action", "createVariation")
                    .addParameter("componentPath", componentPath)
                    .addParameter("variantTemplate", variantTemplate)
                    .addParameter("variationParentPath", parentXFPath)
                    .addParameter("variantTitle", variantTitle);

            if(variantTags != null) {
                for (String tag : variantTags) {
                    entityBuilder.addParameter("variantTags", tag);
                }
            }

            return client.doPost(CONVERT_TO_XF, entityBuilder.build(), HttpUtils.getExpectedStatus(HttpStatus.SC_CREATED, expectedStatus));
        }

        /**
         * Convert and creating a new Experience Fragment Variant
         * @param componentPath path to the component that will converted to a variant
         * @param parentXFPath parent Experience Fragment path
         * @param variantTitle Experience Fragment Variant title
         * @param variantTemplate Experience Fragment Variant template
         * @param variantTags Experience Fragment Variant tags
         * @param expectedStatus http status expected after sending the convert request
         * @return The full {@link SlingHttpResponse} for the convert request
         * @throws ClientException if the request fails
         */
        public SlingHttpResponse addToAnExistingXF(String componentPath,
                                                   String parentXFPath,
                                                   String variantTitle,
                                                   XF_TEMPLATE variantTemplate,
                                                   @Nullable List<String> variantTags,
                                                   int... expectedStatus) throws ClientException {
            return addToAnExistingXF(componentPath, parentXFPath, variantTitle, variantTemplate.path(), variantTags, expectedStatus);
        }
    }

    /**
     * Base class for components
     */
    //TODO use the foundation it client?
    private static abstract class Component {
        protected ExperienceFragmentsClient client;
        protected String variantPath;

        public Component(ExperienceFragmentsClient client, String variantPath) {
            this.client = client;
            this.variantPath = variantPath;
        }

        public abstract String getPath();
    }

    /**
     * Image component
     */
    public static class ImageComponent extends Component {
        private static final String IMAGE_COMPONENT_PATH_SUFIX = "/jcr:content/root/image";

        private final String imageComponentPath;

        public ImageComponent(ExperienceFragmentsClient client, String variantPath) {
            super(client, variantPath);

            this.imageComponentPath = variantPath + IMAGE_COMPONENT_PATH_SUFIX;
        }

        public SlingHttpResponse setImageReference(String imagePath, int... expectedStatus) throws ClientException {
            FormEntityBuilder form = FormEntityBuilder.create()
                    .addParameter("_charset_", "UTF-8")
                    .addParameter("./fileReference", imagePath);

            return client.doPost(imageComponentPath, form.build(), HttpUtils.getExpectedStatus(HttpStatus.SC_OK, expectedStatus));
        }

        public String getImageReference() throws ClientException {
            SlingHttpResponse response = client.doGet(imageComponentPath + ".json", HttpStatus.SC_OK);
            JsonObject jsonResponse = GSON.fromJson(response.getContent(), JsonObject.class);
            return jsonResponse.get("fileReference").getAsString();
        }

        public String getPath() {
            return this.imageComponentPath;
        }
    }

    /**
     * Test component
     */
    public static class TextComponent extends Component {
        private static final String TEXT_COMPONENT_PATH_SUFIX = "/jcr:content/root/text";

        private final String textComponentPath;

        public TextComponent(ExperienceFragmentsClient client, String variantPath) {
            super(client, variantPath);

            this.textComponentPath = variantPath + TEXT_COMPONENT_PATH_SUFIX;
        }

        public SlingHttpResponse setText(String text, int... expectedStatus) throws ClientException {
            FormEntityBuilder form = FormEntityBuilder.create()
                    .addParameter("_charset_", "UTF-8")
                    .addParameter("./text", text)
                    .addParameter("./textIsRich", "true");

            return client.doPost(textComponentPath, form.build(), HttpUtils.getExpectedStatus(HttpStatus.SC_OK, expectedStatus));
        }

        public String getText() throws ClientException {
            SlingHttpResponse response = client.doGet(textComponentPath + ".json", HttpStatus.SC_OK);
            JsonObject jsonResponse = GSON.fromJson(response.getContent(), JsonObject.class);
            return jsonResponse.get("text").getAsString();
        }

        public boolean isRich() throws ClientException {
            SlingHttpResponse response = client.doGet(textComponentPath + ".json", HttpStatus.SC_OK);
            JsonObject jsonResponse = GSON.fromJson(response.getContent(), JsonObject.class);
            return jsonResponse.get("textIsRich").getAsBoolean();
        }

        public String getPath() {
            return textComponentPath;
        }
    }

    /**
     * Content Fragment component
     */
    public static class ContentFragmentComponent extends Component {
        private static final String CONTENT_FRAGMENT_PATH_SUFIX = "/jcr:content/root/contentfragment";

        private final String contentFragmentComponentPath;

        public ContentFragmentComponent(ExperienceFragmentsClient client, String variantPath) {
            super(client, variantPath);

            contentFragmentComponentPath = variantPath + CONTENT_FRAGMENT_PATH_SUFIX;
        }

        @Override
        public String getPath() {
            return contentFragmentComponentPath;
        }

        //TODO
    }

    public static final String TITLE_PROP = "jcr:title";
    private static final String TAGS_PROP = "cq:tags";

    private static final String FACEBOOK_SPACE_PROP = "facebookspace";
    private static final String PINTEREST_SPACE_PROP = "pinterestspace";

    /**
     * Representation of the Experience Fragment
     * NOTE: All properties are cached. If any change is made in the repository you need to call
     * {@code ExperienceFragment.update()}.
     */
    public static class ExperienceFragment {
        public static final String DESCRIPTION_PROP = "jcr:description";

        private ExperienceFragmentsClient client;
        private JsonObject xfProperties;
        private String xfPath;
        private String title;
        private String name;
        private List<String> tags;
        private String description;
        private List<ExperienceFragmentVariant> variants;

        private String facebookSpace;
        private String pinterestSpace;

        public ExperienceFragment(ExperienceFragmentsClient client, String xfPath) throws ClientException {
            this.client = client;
            this.xfPath = StringUtils.stripEnd(xfPath, "/");
            buildXF();
        }

        private void buildXF() throws ClientException {
            JsonObject experienceFragment = GSON.fromJson(client.doGet(xfPath + ".1.json", HttpStatus.SC_OK).getContent(), JsonObject.class);
            xfProperties = experienceFragment.getAsJsonObject("jcr:content");
            title = xfProperties.get(TITLE_PROP).getAsString();
            description = xfProperties.get(DESCRIPTION_PROP) != null ? xfProperties.get(DESCRIPTION_PROP).getAsString() : null;
            //noinspection unchecked
            tags = xfProperties.get(TAGS_PROP) != null ?
                    (List<String>) GSON.fromJson(xfProperties.get(TAGS_PROP).getAsJsonArray().toString(), LIST_STRING_TYPE)
                    : null;
            facebookSpace = xfProperties.get(FACEBOOK_SPACE_PROP) != null ? xfProperties.get(FACEBOOK_SPACE_PROP).getAsString() : null;
            pinterestSpace = xfProperties.get(PINTEREST_SPACE_PROP) != null ? xfProperties.get(PINTEREST_SPACE_PROP).getAsString() : null;

            String[] pathComponents = xfPath.split("/");
            name = pathComponents[pathComponents.length - 1];

            variants = new ArrayList<>();

            for(Map.Entry<String, JsonElement> property : experienceFragment.entrySet()) {
                if(property.getValue().isJsonObject() && !property.getKey().equals("jcr:content")) {
                    variants.add(new ExperienceFragmentVariant(client, xfPath + "/" + property.getKey()));
                }
            }
        }

        public void update() throws ClientException {
            buildXF();
        }

        public String getTitle() {
            return title;
        }

        public String getName() {
            return name;
        }

        public String getPath() { return xfPath; }

        public @Nullable List<String> getTags() {
            return tags;
        }

        public @Nullable String getDescription() {
            return description;
        }

        public List<ExperienceFragmentVariant> getVariants() {
            return variants;
        }

        public @Nullable String getFacebookSpace() {
            return facebookSpace;
        }

        public @Nullable String getPinterestSpace() {
            return pinterestSpace;
        }

        public JsonElement getProperty(String property) {
            return xfProperties.get(property);
        }

        public void setFacebookSpace(String facebookSpace) throws ClientException {
            HttpEntity entity = FormEntityBuilder.create()
                    .addParameter("./" + FACEBOOK_SPACE_PROP, facebookSpace)
                    .build();

            client.doPost(xfPath + "/jcr:content", entity, HttpStatus.SC_OK);
            update();
        }

        public void setPinterestSpace(String pinterestSpace) throws ClientException {
            HttpEntity entity = FormEntityBuilder.create()
                    .addParameter("./" + PINTEREST_SPACE_PROP, pinterestSpace)
                    .build();

            client.doPost(xfPath + "/jcr:content", entity, HttpStatus.SC_OK);
            update();
        }

        public SlingHttpResponse publish(int... expectedStatus) throws ClientException {
            return client.publishXF(xfPath, expectedStatus);
        }

        public SlingHttpResponse unpublish(int... expectedStatus) throws ClientException {
            return client.unblishXF(xfPath, expectedStatus);
        }
    }


    /**
     * Representation of the Experience Fragment Variant
     * NOTE: All properties are cached. If any change is made in the repository you need to call
     * {@code ExperienceFragmentVariant.update()}.
     */
    public static class ExperienceFragmentVariant {
        public static final String DESCRIPTION_PROP = "jcr:description";
        public static final String MASTER_VARIATION_PROP = "cq:xfMasterVariation";
        public static final String SOCIAL_VARIATION_PROP = "cq:xfSocialVariation";
        public static final String SHOW_IN_EDITOR_PROP = "showInEditor";
        public static final String TEMPLATE_PROP = "cq:template";
        public static final String RESOURCE_TYPE_PROP = "sling:resourceType";
        private static final String MIXIN_TYPES_PROP = "jcr:mixinTypes";
        private static final String LIVE_RELATIONSHIP = "cq:LiveRelationship";
        private static final String LIVE_SYNC = "cq:LiveSync";
        private static final String XF_VARIANT_TYPE = "cq:xfVariantType";

        private ExperienceFragmentsClient client;
        private JsonObject variantProperties;
        private boolean masterVariant;
        private boolean socialVariant;
        private boolean showInEditor;
        private boolean liveCopy;
        private String variantType;
        private String variantPath;
        private String templatePath;
        private XF_TEMPLATE templateType;
        private String resourceType;
        private String title;
        private String name;
        private String description;
        private List<String> tags;

        private String facebookSpace;
        private String pinterestSpace;

        public ExperienceFragmentVariant(ExperienceFragmentsClient xfClient, String variantPath) throws ClientException {
            this.client = xfClient;
            this.variantPath = StringUtils.stripEnd(variantPath, "/");
            buildVariant();
        }

        private void buildVariant() throws ClientException {
            SlingHttpResponse response = client.doGet(variantPath + "/jcr:content.json", HttpStatus.SC_OK);
            variantProperties = GSON.fromJson(response.getContent(), JsonObject.class);

            variantType   = variantProperties.get(XF_VARIANT_TYPE) != null ? variantProperties.get(XF_VARIANT_TYPE).getAsString() : CUSTOM_XF_VARIANT_TYPE;
            masterVariant = variantProperties.get(MASTER_VARIATION_PROP) != null && variantProperties.get(MASTER_VARIATION_PROP).getAsBoolean();
            socialVariant = variantProperties.get(SOCIAL_VARIATION_PROP) != null && variantProperties.get(SOCIAL_VARIATION_PROP).getAsBoolean();
            showInEditor  = variantProperties.get(SHOW_IN_EDITOR_PROP) != null && variantProperties.get(SHOW_IN_EDITOR_PROP).getAsBoolean();
            templatePath  = variantProperties.get(TEMPLATE_PROP).getAsString();
            resourceType  = variantProperties.get(RESOURCE_TYPE_PROP).getAsString();
            title         = variantProperties.get(TITLE_PROP).getAsString();
            description   = variantProperties.get(DESCRIPTION_PROP) != null ? variantProperties.get(DESCRIPTION_PROP).getAsString() : null;
            //noinspection unchecked
            tags = variantProperties.get(TAGS_PROP) != null ?
                    (List<String>) GSON.fromJson(variantProperties.get(TAGS_PROP).getAsJsonArray().toString(), LIST_STRING_TYPE) :
                    null;
            facebookSpace = variantProperties.get(FACEBOOK_SPACE_PROP) != null ? variantProperties.get(FACEBOOK_SPACE_PROP).getAsString() : null;
            pinterestSpace = variantProperties.get(PINTEREST_SPACE_PROP) != null ? variantProperties.get(PINTEREST_SPACE_PROP).getAsString() : null;

            //noinspection unchecked
            List<String> mixins = variantProperties.get(MIXIN_TYPES_PROP) != null ?
                    (List<String>) GSON.fromJson(variantProperties.get(MIXIN_TYPES_PROP).getAsJsonArray(), LIST_STRING_TYPE) :
                    new ArrayList<String>();
            liveCopy = mixins.contains(LIVE_RELATIONSHIP) && mixins.contains(LIVE_SYNC);

            String[] pathComponents = variantPath.split("/");
            name = pathComponents[pathComponents.length - 1];

            templateType = XF_TEMPLATE.CUSTOM;
            for(XF_TEMPLATE template : XF_TEMPLATE.values()) {
                if(template != XF_TEMPLATE.CUSTOM && template.path().equals(templatePath)) {
                    templateType = template;
                    break;
                }
            }
        }

        public boolean isMasterVariant() {
            return masterVariant;
        }

        public boolean isSocialVariant() {
            return socialVariant;
        }

        public boolean isLiveCopy() {
            return liveCopy;
        }

        public XF_TEMPLATE getTemplateType() {
            return templateType;
        }

        public String getTemplatePath() {
            return templateType != XF_TEMPLATE.CUSTOM ? templateType.path() : templatePath;
        }

        public String getVariantType() {
            return variantType;
        }

        public String getPath() {
            return variantPath;
        }

        public String getResourceType() {
            return resourceType;
        }

        public boolean showInEditor() {
            return showInEditor;
        }

        public String getParentXFPath() {
            return ExperienceFragmentsClient.getParentXFPath(variantPath);
        }

        public String getTitle() {
            return title;
        }

        public String getName() {
            return name;
        }

        public @Nullable String getDescription() {
            return description;
        }

        public @Nullable List<String> getTags() {
            return tags;
        }

        public @Nullable String getFacebookSpace() { return  facebookSpace; }
        public @Nullable String getPinterestSpace() { return pinterestSpace; }

        public JsonElement getProperty(String propertyName) {
            return variantProperties.get(propertyName);
        }

        public <T extends VariantComponents> T getComponents() {
            return templateType.getComponents(client, variantPath);
        }

        public void update() throws ClientException {
            buildVariant();
        }

        public void setFacebookSpace(String facebookSpace) throws ClientException {
            if(templateType != XF_TEMPLATE.FACEBOOK)
                throw new UnsupportedOperationException("Cannot set the Facebook Space for a non Facebook variant");

            HttpEntity entity = FormEntityBuilder.create()
                    .addParameter("./" + FACEBOOK_SPACE_PROP, facebookSpace)
                    .build();

            client.doPost(variantPath + "/jcr:content", entity, HttpStatus.SC_OK);
            update();
        }

        public void setPinterestSpace(String pinterestSpace) throws ClientException {
            if(templateType != XF_TEMPLATE.PINTEREST)
                throw new UnsupportedOperationException("Cannot set the Facebook Space for a non Pinterest variant");

            HttpEntity entity = FormEntityBuilder.create()
                    .addParameter("./" + PINTEREST_SPACE_PROP, pinterestSpace)
                    .build();

            client.doPost(variantPath + "/jcr:content", entity, HttpStatus.SC_OK);
            update();
        }

        public SlingHttpResponse publish(int... expectedStatus) throws ClientException {
            return client.publishXFVariant(variantPath, expectedStatus);
        }

        public SlingHttpResponse unpublish(int... expectedStatus) throws ClientException {
            return client.unpublishXFVariant(variantPath, expectedStatus);
        }
    }


    /**
     * Representation the configuration of the Experience Fragments Feature, as it can be done from the UI
     * NOTE: All properties are cached. If any change is made in the repository you need to call
     * {@code ExperienceFragmentsConfiguration.update()}.
     */
    public static class ExperienceFragmentsConfiguration {
        private static final String ALLOWED_TEMPLATES_PROP = "cq:allowedTemplates";
        private ExperienceFragmentsClient client;
        private List<String> allowedTemplates;

        public ExperienceFragmentsConfiguration(ExperienceFragmentsClient client) throws ClientException {
            this.client = client;
            buildConfiguration();
        }

        private void buildConfiguration() throws ClientException {
            SlingHttpResponse response = client.doGet(ExperienceFragmentsClient.DEFAULT_XF_PARENT_PATH + ".json");
            JsonObject folderProperties = GSON.fromJson(response.getContent(), JsonObject.class);
            JsonArray allowedTemplatesJson = folderProperties.get(ALLOWED_TEMPLATES_PROP).getAsJsonArray();
            allowedTemplates = new ArrayList<>();
            for(JsonElement allowedTemplateJson : allowedTemplatesJson) {
                allowedTemplates.add(allowedTemplateJson.getAsString());
            }
        }

        public List<String> getAllowedTemplates() {
            return allowedTemplates;
        }

        public void update() throws ClientException {
            buildConfiguration();
        }
    }

    /**
     * Experience Fragments configuration builder as it can be done from the UI
     */
    public static class ExperienceFragementsConfigurationBuilder {
        private static final String ALLOWED_TEMPLATES_PARAM = "cq:allowedTemplates";
        private ExperienceFragmentsClient client;
        private ExperienceFragmentsConfiguration configuration;

        private List<String> allowedTemplates;

        /**
         * Constructor
         * @param client client to be used
         * @throws ClientException if the builder cannot be created
         */
        public ExperienceFragementsConfigurationBuilder(ExperienceFragmentsClient client) throws ClientException {
            this.client = client;
            configuration = new ExperienceFragmentsConfiguration(client);
        }

        /**
         * Set the allowed templates
         * @param allowedTemplates the complete list of allowed templates.
         * @return this
         */
        public ExperienceFragementsConfigurationBuilder withAllowedTemplates(List<String> allowedTemplates) {
            this.allowedTemplates = allowedTemplates;
            return this;
        }

        /**
         * Do the configuration
         * @return The new configuration
         * @throws ClientException if the configuration can not be created
         */
        public ExperienceFragmentsConfiguration configure() throws ClientException {
            FormEntityBuilder feb = FormEntityBuilder.create();
            /*
             * The UI contains all the values already configured. If someone doesn't configure something in the builder,
             * we need to keep the existing ones otherwise those entries are deleted.
             */
            List<String> allowedTemplatesResolved = allowedTemplates != null ? allowedTemplates : configuration.getAllowedTemplates();

            for(String allowedTemplate : allowedTemplatesResolved) {
                feb.addParameter(ALLOWED_TEMPLATES_PARAM, allowedTemplate);
            }

            client.doPost(CONFIGURATOR, feb.build(), HttpStatus.SC_OK);
            return new ExperienceFragmentsConfiguration(client);
        }
    }
}
