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

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClientConfig;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.FormEntityBuilder;
import org.codehaus.jackson.JsonNode;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;

/**
 * The base client for all commerce-related tests. It provides a core set of commonly used commerce
 * functions such as getting carts and orders, etc. <br>
 * <br>
 * It extends from {@link com.adobe.cq.testing.client.FormClient} which in turn provides a core set
 * of commonly used website, page and form functionality.
 */
public class CommerceClient extends FormClient {

    public final String PRODUCT_PAGE_RESOURCE_TYPE = "weretail/components/structure/page";
    public final String PROXY_PRODUCT_PAGE_RESOURCE_TYPE = "commerce/components/productpageproxy";

    private String commerceCookieName = "CommercePersistence";

    public CommerceClient(CloseableHttpClient http, SlingClientConfig config) throws ClientException {
        super(http, config);
    }

    public CommerceClient(URI serverUrl, String user, String password) throws ClientException {
        super(serverUrl, user, password);
    }

    /**
     * Execute a commerce-oriented POST request.  Similar to adminAuthor.doPost(), but allows
     * for the setting of a session cookie.
     *
     * @param path request path
     * @param cookieText cookie to be added
     * @param parameters list of parameters
     * @return the http response
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse doCommercePost(String path, String cookieText, Map<String, String> parameters) throws ClientException {
        assert(commerceCookieName != null);
        List<Header> headers = new ArrayList<>(2);
        headers.add(new BasicHeader("Cookie", commerceCookieName + "=" + urlEncode(cookieText)));

        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8"));

        return doPost(path, FormEntityBuilder.create().addAllParameters(parameters).build(), headers);
    }

    public SlingHttpResponse doCommerceGet(String path, String cookieText) throws ClientException {
        assert(commerceCookieName != null);
        List<Header> headers = new ArrayList<>(1);
        headers.add(new BasicHeader("Cookie", commerceCookieName + "=" + urlEncode(cookieText)));
        return doGet(path, null, headers);
    }

    /**
     * Return the user's shopping cart.
     * NOTE: carts are no longer stored in the users' homes.  See getCookieText() to get the cart.
     *
     * @param homePath home path
     * @return the cart as json node
     * @throws ClientException if the request fails
     */
    public JsonNode getCart(String homePath) throws ClientException {
        return doGetJson(homePath + "/commerce/cart", -1);
    }

    /**
     * Retrieves the committed orders.
     *
     * @return the orders as a json node
     * @throws ClientException if the request fails
     */
    public JsonNode getOrders() throws ClientException {
        return doGetJson("/etc/commerce/orders", -1);
    }

    /**
     * Return the cookie values concatenated into a single string.  The cart and order details will
     * be contained in the result.
     *
     * @param resp http response
     * @return the cookie text
     */
    public String getCommerceCookieText(HttpResponse resp) {
        assert(commerceCookieName != null);
        String commerceCookiePreamble = commerceCookieName + "=";
        Header headers[] = resp.getHeaders("Set-Cookie");
        for (Header header : headers) {
            String s = header.getValue();
            if (s.startsWith(commerceCookiePreamble)) {
                try {
                    return urlDecode(s.substring(commerceCookiePreamble.length()));
                } catch (ClientException e) {
                    LOG.error("Failed to decode commerce cookie", e);
                }
            }
        }
        return "";
    }

    /**
     * Clear out the commerce context from the user's home directory.
     *
     * @param homePath home path
     * @throws ClientException if the request fails
     */
    public void clearCommerceContext(String homePath) throws ClientException {
        final String commercePath = homePath + "/commerce";

        if (exists(commercePath)) {
            deletePath(commercePath);
        }
    }

    /**
     * Creates a catalog from a blueprint
     *
     * @param source path to the blueprint
     * @param dest destination path to the catalog
     * @param label label of the catalog
     * @param title title of the catalog
     * @param expectedStatus expected HTTP response status code
     * @return the sling http response
     * @throws ClientException if the request could not be executed
     */
    public SlingHttpResponse createCatalog(String source, String dest, String label, String title, int... expectedStatus)
            throws ClientException {

        return wcmCommands.createCatalog(source, dest, label, title, expectedStatus);

    }

    /**
     * Rolls out a section
     *
     * @param source path to blueprint
     * @param dest destination path to the catalog
     * @param force whether the rollout should be forced
     * @param expectedStatus expected HTTP response status code
     * @return the sling http response
     * @throws ClientException if the request could not be executed
     */
    public SlingHttpResponse rolloutSection(String source, String dest, boolean force, int... expectedStatus)
            throws ClientException {

        return wcmCommands.rolloutSection(source, dest, force, expectedStatus);

    }

    /**
     * Creates a classification
     *
     * @param managementPath path to which the request should be sent
     * @param parentPath path to the parent node
     * @param title name of the classification
     * @param expectedStatus expected HTTP response status code
     * @return the sling http response
     * @throws ClientException if the request could not be executed
     */
    public SlingHttpResponse createClassification(String managementPath, String parentPath, String title, int... expectedStatus)
            throws ClientException {

        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter(":operation", "createClassification")
                .addParameter("parentPath", parentPath)
                .addParameter("title", title)
                .addParameter("_charset_", "utf-8");
        return doPost(managementPath, form.build(), expectedStatus);
    }

    /**
     * Removes a classification
     *
     * @param managementPath path to which the request should be sent
     * @param classificationPath path to the classification
     * @param expectedStatus expected HTTP response status code
     * @return the sling http response
     * @throws ClientException if the request could not be executed
     */
    public SlingHttpResponse removeClassification(String managementPath, String classificationPath, int... expectedStatus)
            throws ClientException {

        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter(":operation", "removeClassification")
                .addParameter("path", classificationPath)
                .addParameter("_charset_", "utf-8");
        return doPost(managementPath, form.build(), expectedStatus);
    }

    /**
     * Adds a root category
     *
     * @param managementPath path to which the request should be sent
     * @param classificationPath path to the classification
     * @param title name of the category
     * @param expectedStatus expected HTTP response status code
     * @return the sling http response
     * @throws ClientException if the request could not be executed
     */
    public SlingHttpResponse addRootCategory(String managementPath, String classificationPath, String title, int... expectedStatus)
            throws ClientException {

        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter(":operation", "createRootCategory")
                .addParameter("path", classificationPath)
                .addParameter("title", title)
                .addParameter("_charset_", "utf-8");
        return doPost(managementPath, form.build(), expectedStatus);
    }

    /**
     * Adds a category
     *
     * @param managementPath path to which the request should be sent
     * @param parentCategoryPath path to the parent category
     * @param title name of the category
     * @param expectedStatus expected HTTP response status code
     * @return the sling http response
     * @throws ClientException if the request could not be executed
     */
    public SlingHttpResponse addCategory(String managementPath, String parentCategoryPath, String title, int... expectedStatus)
            throws ClientException {

        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter(":operation", "createCategory")
                .addParameter("parentPath", parentCategoryPath)
                .addParameter("title", title)
                .addParameter("_charset_", "utf-8");
        return doPost(managementPath, form.build(), expectedStatus);
    }

    /**
     * Removes a category
     *
     * @param managementPath path to which the request should be sent
     * @param categoryPath path to the category
     * @param expectedStatus expected HTTP response status code
     * @return the sling http response
     * @throws ClientException if the request could not be executed
     */
    public SlingHttpResponse removeCategory(String managementPath, String categoryPath, int... expectedStatus)
            throws ClientException {

        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter(":operation", "removeCategory")
                .addParameter("path", categoryPath)
                .addParameter("_charset_", "utf-8");
        return doPost(managementPath, form.build(), expectedStatus);
    }

    /**
     * Update the attributes of a category
     *
     * @param managementPath path to which the request should be sent
     * @param categoryPath path to the category
     * @param title name of the category
     * @param expectedStatus expected HTTP response status code
     * @return the sling http response
     * @throws ClientException if the request could not be executed
     */
    public SlingHttpResponse updateCategory(String managementPath, String categoryPath, String title, int... expectedStatus)
            throws ClientException {

        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter(":operation", "updateCategory")
                .addParameter("path", categoryPath)
                .addParameter("title", title)
                .addParameter("_charset_", "utf-8");
        return doPost(managementPath, form.build(), expectedStatus);
    }

    /**
     * Adds an attribute
     *
     * @param managementPath path to which the request should be sent
     * @param categoryPath path to the category
     * @param title attribute title
     * @param attrName attribute name
     * @param attrUnit attribute unit
     * @param expectedStatus expected HTTP response status code
     * @return the sling http response
     * @throws ClientException if the request could not be executed
     */
    public SlingHttpResponse addAttribute(String managementPath, String categoryPath, String title, String attrName, String attrUnit, int... expectedStatus)
            throws ClientException {

        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter(":operation", "addAttribute")
                .addParameter("parentPath", categoryPath)
                .addParameter("title", title)
                .addParameter("attributeName", attrName)
                .addParameter("attributeUnit", attrUnit)
                .addParameter("_charset_", "utf-8");
        return doPost(managementPath, form.build(), expectedStatus);
    }

    /**
     * Creates a collection
     *
     * @param managementPath path to which the request should be sent
     * @param parentPath path to the parent node
     * @param title name of the collection
     * @param type type of the collection
     * @param search search
     * @param searchType search type of the collection
     * @param referencePaths reference paths
     * @param expectedStatus expected HTTP response status code
     * @return the sling http response
     * @throws ClientException if the request could not be executed
     */
    public SlingHttpResponse createCollection(String managementPath, String parentPath, String title, String type, String search, String searchType, String[] referencePaths, int... expectedStatus)
            throws ClientException {

        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter(":operation", "createCollection")
                .addParameter("collectionParentPath", parentPath)
                .addParameter("collectionTitle", title)
                .addParameter("_charset_", "utf-8");

        if(search != null) {
            form.addParameter("search", search);
        }

        if(searchType != null) {
            form.addParameter("searchType", searchType);
        }

        if(type != null) {
            form.addParameter("collectionType", type);
        }

        if(referencePaths != null) {
            for (String ref : referencePaths) {
                form.addParameter("referencePaths", ref);
            }
        }

        return doPost(managementPath, form.build(), expectedStatus);
    }

    /**
     * Creates a collection
     *
     * @param managementPath path to which the request should be sent
     * @param parentPath path to the parent node
     * @param title title of the collection
     * @param type type of the collection
     * @param referencePaths reference paths
     * @param expectedStatus expected HTTP response status code
     * @return the sling http response
     * @throws ClientException if the request could not be executed
     */
    public SlingHttpResponse createCollection(String managementPath, String parentPath, String title, String type, String[] referencePaths, int... expectedStatus)
            throws ClientException {
        return this.createCollection(managementPath, parentPath, title, type, null, null, referencePaths, expectedStatus);
    }

    /**
     * Updates the properties of a collection
     *
     * @param managementPath path to which the request should be sent
     * @param collectionPath path to the collection
     * @param title name of the collection
     * @param referencePaths reference paths
     * @param expectedStatus expected HTTP response status code
     * @return the sling http response
     * @throws ClientException if the request could not be executed
     */
    public SlingHttpResponse updateCollection(String managementPath, String collectionPath, String title, String[] referencePaths, int... expectedStatus)
            throws ClientException {

        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter(":operation", "updateCollection")
                .addParameter("collectionPath", collectionPath)
                .addParameter("collectionTitle", title)
                .addParameter("_charset_", "utf-8");

        if(referencePaths != null) {
            for (String ref : referencePaths) {
                form.addParameter("referencePaths", ref);
            }
        }

        return doPost(managementPath, form.build(), expectedStatus);
    }

    /**
     * Removes a collection
     *
     * @param managementPath path to which the request should be sent
     * @param collectionPath path to the collection
     * @param expectedStatus expected HTTP response status code
     * @return the sling http response
     * @throws ClientException if the request could not be executed
     */
    public SlingHttpResponse removeCollection(String managementPath, String collectionPath, int... expectedStatus)
            throws ClientException {

        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter(":operation", "removeCollection")
                .addParameter("collectionPath", collectionPath)
                .addParameter("_charset_", "utf-8");
        return doPost(managementPath, form.build(), expectedStatus);
    }

    /**
     * Adds one or multiple references to a collection
     *
     * @param managementPath path to which the request should be sent
     * @param collectionPath path to the collection
     * @param referencePaths list of HTTP headers
     * @param expectedStatus expected HTTP response status code
     * @return the sling http response
     * @throws ClientException if the request could not be executed
     */
    public SlingHttpResponse addReferences(String managementPath, String collectionPath, String[] referencePaths, int... expectedStatus)
            throws ClientException {

        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter(":operation", "addReferences")
                .addParameter("collectionPath", collectionPath)
                .addParameter("_charset_", "utf-8");

        if(referencePaths != null) {
            for (String ref : referencePaths) {
                form.addParameter("referencePaths", ref);
            }
        }

        return doPost(managementPath, form.build(), expectedStatus);
    }

    /**
     * Removes one or multiple references from a collection
     *
     * @param managementPath path to which the request should be sent
     * @param collectionPath path tp the collection
     * @param referencePaths list of HTTP headers
     * @param expectedStatus expected HTTP response status code
     * @return the sling http response
     * @throws ClientException if the request could not be executed
     */
    public SlingHttpResponse removeReferences(String managementPath, String collectionPath, String[] referencePaths, int... expectedStatus)
            throws ClientException {

        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter(":operation", "removeReferences")
                .addParameter("collectionPath", collectionPath)
                .addParameter("_charset_", "utf-8");

        if(referencePaths != null) {
            for (String ref : referencePaths) {
                form.addParameter("referencePaths", ref);
            }
        }

        return doPost(managementPath, form.build(), expectedStatus);

    }

    /**
     * Creates a smartlist
     *
     * @param managementPath path to which the request should be sent
     * @param title name of the smartlist
     * @param defaultList should the created list be a default list?
     * @param redirect redirection path
     * @param headers list of HTTP headers
     * @param expectedStatus expected HTTP response status code
     * @return the sling http response
     * @throws ClientException if the request could not be executed
     */
    public SlingHttpResponse createSmartList(String managementPath, String title, boolean defaultList, String redirect, List<Header> headers, int... expectedStatus)
            throws ClientException {

        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter(":operation", "createSmartList")
                .addParameter("title", title)
                .addParameter("default", defaultList ? "on" : "off")
                .addParameter("redirect", redirect)
                .addParameter("_charset_", "utf-8");
        return doPost(managementPath, form.build(), headers, expectedStatus);
    }

    /**
     * Delete one or multiple smartlists
     *
     * @param managementPath path to which the request should be sent
     * @param title name of the smartlist
     * @param delete one or multiple smartlists to delete
     * @param redirect redirection path
     * @param headers list of HTTP headers
     * @param expectedStatus expected HTTP response status code
     * @return the sling http response
     * @throws ClientException if the request could not be executed
     */
    public SlingHttpResponse manageSmartListsDelete(String managementPath, String title, String[] delete, String redirect, List<Header> headers, int... expectedStatus)
            throws ClientException {

        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter(":operation", "manageSmartLists")
                .addParameter("title", title)
                .addParameter("redirect", redirect)
                .addParameter("_charset_", "utf-8");

        if(delete != null) {
            for (String del : delete) {
                form.addParameter("delete", del);
            }
        }

        return doPost(managementPath, form.build(), headers, expectedStatus);
    }

    /**
     * Manage the default setting of a smartlist
     *
     * @param managementPath path to which the request should be sent
     * @param title name of the smartlist
     * @param defaultList one or multiple smartlists that should be a default list
     * @param redirect redirection path
     * @param headers list of HTTP headers
     * @param expectedStatus expected HTTP response status code
     * @return the sling http response
     * @throws ClientException if the request could not be executed
     */
    public SlingHttpResponse manageSmartListsDefault(String managementPath, String title, String[] defaultList, String redirect, List<Header> headers, int... expectedStatus)
            throws ClientException {

        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter(":operation", "manageSmartLists")
                .addParameter("title", title)
                .addParameter("redirect", redirect)
                .addParameter("_charset_", "utf-8");

        if(defaultList != null) {
            for (String def : defaultList) {
                form.addParameter("default", def);
            }
        }

        return doPost(managementPath, form.build(), headers, expectedStatus);
    }

    /**
     * Edits the properties of a smartlist
     *
     * @param managementPath path to which the request should be sent
     * @param listPath path to the smartlist
     * @param redirect redirection path
     * @param title name of the smartlist
     * @param description description of the smartlist
     * @param headers list of HTTP headers
     * @param expectedStatus expected HTTP response status code
     * @return the sling http response
     * @throws ClientException if the request could not be executed
     */
    public SlingHttpResponse editSmartList(String managementPath, String listPath, String redirect, String title, String description, List<Header> headers, int... expectedStatus)
            throws ClientException {

        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter(":operation", "editSmartList")
                .addParameter("smartlist-path", listPath)
                .addParameter("redirect", redirect)
                .addParameter("title", title)
                .addParameter("description", description)
                .addParameter("_charset_", "utf-8");
        return doPost(managementPath, form.build(), headers, expectedStatus);
    }

    /**
     * Adds an entry to a smartlist.
     *
     * @param managementPath path to which the request should be sent
     * @param productPath path to the product
     * @param listName name of the smartlist
     * @param redirect redirection path
     * @param headers list of HTTP headers
     * @param expectedStatus expected HTTP response status code
     * @return the sling http response
     * @throws ClientException if the request could not be executed
     */
    public SlingHttpResponse addToSmartList(String managementPath, String productPath, String listName, String redirect, List<Header> headers, int... expectedStatus)
            throws ClientException {

        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter(":operation", "addToSmartList")
                .addParameter("product-path", productPath)
                .addParameter("redirect", redirect)
                .addParameter("_charset_", "utf-8");

        if (listName != null) {
            form.addParameter("smartlist-path", listName);
        }

        if(headers == null || headers.size() <= 0) {
            return doPost(managementPath, form.build(), expectedStatus);
        }
        return doPost(managementPath, form.build(), headers, expectedStatus);

    }

    /**
     * Deletes an entry form a smartlist
     *
     * @param managementPath path to which the request should be sent
     * @param productPath path to the product
     * @param listName name of the smartlist
     * @param redirect redirection path
     * @param headers list of HTTP headers
     * @param expectedStatus expected HTTP response status code
     * @return the sling http response
     * @throws ClientException if the request could not be executed
     */
    public SlingHttpResponse deleteSmartListEntry(String managementPath, String productPath, String listName, String redirect, List<Header> headers, int... expectedStatus)
            throws ClientException {

        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter(":operation", "deleteSmartListEntry")
                .addParameter("product-path", productPath)
                .addParameter("smartlist-path", listName)
                .addParameter("redirect", redirect)
                .addParameter("_charset_", "utf-8");
        return doPost(managementPath, form.build(), headers, expectedStatus);

    }

    /**
     * Modifies the quantity of an entry on a smartlist
     *
     * @param managementPath path to which the request should be sent
     * @param productPath path to the product
     * @param listPath path to the smartlist
     * @param redirect redirection path
     * @param quantity new quantity
     * @param headers list of HTTP headers
     * @param expectedStatus expected HTTP response status code
     * @return the sling http response
     * @throws ClientException if the request could not be executed
     */
    public SlingHttpResponse modifyQuantitySmartListEntry(String managementPath, String productPath, String listPath, String redirect, String quantity, List<Header> headers, int... expectedStatus)
            throws ClientException {

        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter(":operation", "modifyQuantitySmartListEntry")
                .addParameter("product-path", productPath)
                .addParameter("smartlist-path", listPath)
                .addParameter("redirect", redirect)
                .addParameter("quantity", quantity)
                .addParameter("_charset_", "utf-8");
        return doPost(managementPath, form.build(), headers, expectedStatus);

    }

    /**
     * Add a product to the cart
     *
     * @param managementPath path to which the request should be sent
     * @param productPath path to the product
     * @param listPath path to the smartlist
     * @param delete should the product be deleted from its smartlist?
     * @param redirect redirection path
     * @param headers list of HTTP headers
     * @param expectedStatus expected HTTP response status code
     * @return the sling http response
     * @throws ClientException if the request could not be executed
     */
    public SlingHttpResponse addToCart(String managementPath, String productPath, String listPath, boolean delete, String redirect, List<Header> headers, int... expectedStatus)
            throws ClientException {

        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter(":operation", "addToCart")
                .addParameter("smartlist-path", listPath)
                .addParameter("delete", delete ? "on" : "off")
                .addParameter("redirect", redirect)
                .addParameter("_charset_", "utf-8");

        if(productPath != null) {
            form.addParameter("product-path", productPath);
        }

        return doPost(managementPath, form.build(), headers, expectedStatus);

    }

    /**
     * Update the product component on a page to point to the given product and generate the product hierarchy.
     *
     * @param productPagePath Path to product page
     * @param productPath Path to the product
     * @return the http response
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse configureProductComponent(String productPagePath, String productPath) throws ClientException {

        // Update to product component on the sample page to show our product
        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter("_charset_", "UTF-8")
                .addParameter("./sling:resourceType", "weretail/components/structure/product")
                .addParameter("./image/isDecorative@Delete", "true")
                .addParameter("./productData", productPath);
        String productComponent = productPagePath + "/_jcr_content/root/product";
        this.doPost(productComponent, form.build(), SC_OK);

        // Trigger the creation of the product subcomponents (image etc.)
        form = FormEntityBuilder.create()
                .addParameter("_charset_", "UTF-8")
                .addParameter(":operation", "checkVariantHierarchy")
                .addParameter("catalogPath", productPagePath + "/jcr:content/root/product");
        return this.doPost("/libs/commerce/products", form.build(), SC_OK);

    }

    /**
     * Create a new product.
     *
     * @param parent Parent folder
     * @param title Name
     * @param price Price
     * @return SlingHttpResponse object
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse addProduct(String parent, String title, double price)
            throws ClientException {

        return this.addProduct(parent, title, price, null, null, null, null);

    }

    /**
     * Create a new product.
     *
     * @param parent Parent folder
     * @param title Name
     * @param price Price
     * @param description Description
     * @param sku SKU identifier
     * @param color Color
     * @param size Size
     * @return SlingHttpResponse object
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse addProduct(String parent, String title, double price, String description, String sku, String color, String size)
            throws ClientException {

        // Prepare product create request
        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter("_charset_",                  "UTF-8")
                .addParameter("./jcr:primaryType",          "nt:unstructured")
                .addParameter("parentPath",                 parent)
                .addParameter("./jcr:title",                title)
                .addParameter("./price@TypeHint",           "Decimal")
                .addParameter("./cq:commerceType",          "product")
                .addParameter("./sling:resourceType",       "commerce/components/product")
                .addParameter("./jcr:mixinTypes",           "cq:Taggable")
                .addParameter("./jcr:mixinTypes@TypeHint",  "String[]")
                .addParameter("./price",                    String.valueOf(price))
                .addParameter("./jcr:lastModified",             "")
                .addParameter("./jcr:lastModified@TypeHint",    "Date");

        if (description != null) {
            form.addParameter("./jcr:description", description);
        }

        if (sku != null) {
            form.addParameter("./identifier",               sku);
        }

        if (color != null) {
            form.addParameter("./color",                    color);
        }

        if (size != null) {
            form.addParameter("./size",                     size);
        }

        // Send request
        return this.doPost(parent + "/*", form.build(), SC_CREATED);

    }

    /**
     * Assign a tag to a product.
     *
     * @param tagPath Path to the tag.
     * @param productPath Path to the product.
     * @return SlingHttpResponse object
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse addTagToProduct(String tagPath, String productPath) throws ClientException {

        // Prepare update request
        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter("_charset_", "UTF-8")
                .addParameter("./cq:tags@TypeHint",  "String[]")
                .addParameter("./cq:tags", tagPath);

        // Send request
        return this.doPost(productPath, form.build(), SC_OK);

    }

    /**
     * Add an asset to a product.
     *
     * @param product Path to product
     * @param asset Path to asset
     * @return SlingHttpResponse object
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse addProductAsset(String product, String asset) throws ClientException {

        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter("_charset_", "UTF-8")
                .addParameter("productPath", product)
                .addParameter("assetReference", asset);

        return this.doPost("/apps/weretail/content/we-retail/product/jcr:content/cq:dialog/content/items/columns/items/column1/items/images/image.create.html", form.build(), SC_OK);

    }

    /**
     * Update an existing asset of a product.
     *
     * @param productAssetPath Path to the product's asset (e.g. ProductPath + "/assets/asset0")
     * @param newAsset Path to new asset
     * @return SlingHttpResponse object
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse updateProductAsset(String productAssetPath, String newAsset) throws ClientException {
        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter("_charset_", "UTF-8")
                .addParameter("productAssetPath", productAssetPath)
                .addParameter("assetReference", newAsset);

        return this.doPost("/apps/weretail/content/we-retail/product/jcr:content/cq:dialog/content/items/columns/items/column1/items/images/image.update.html", form.build(), SC_OK);

    }

    /**
     * Deletes an asset from a product.
     *
     * @param productAssetPath Path to the product's asset (e.g. ProductPath + "/assets/asset0")
     * @return SlingHttpResponse object
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse deleteProductAsset(String productAssetPath) throws ClientException {

        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter("_charset_", "UTF-8")
                .addParameter("productAssetPath", productAssetPath);

        return this.doPost("/apps/weretail/content/we-retail/product/jcr:content/cq:dialog/content/items/columns/items/column1/items/images/image.remove.html", form.build(), SC_OK);

    }

    /**
     * Add a product variant.
     *
     * @param parentProduct Path of parent product
     * @param title Title
     * @param price Price
     * @param description Description
     * @param sku SKU identifier
     * @param color Color
     * @param size Size
     * @return SlingHttpResponse object
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse addProductVariant(String parentProduct, String title, double price, String description, String sku, String color, String size)
            throws ClientException {

        // Create a new product variant
        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter("_charset_",                      "UTF-8")
                .addParameter("parentPath",                     parentProduct)
                .addParameter("./jcr:title",                    title)
                .addParameter("./price",                        String.valueOf(price))
                .addParameter("./price@TypeHint",               "Decimal")
                .addParameter("./jcr:primaryType",              "nt:unstructured")
                .addParameter("./cq:commerceType",              "variant")
                .addParameter("./sling:resourceType",           "commerce/components/product")
                .addParameter("./jcr:mixinTypes",               "cq:Taggable")
                .addParameter("./jcr:mixinTypes@TypeHint",      "String[]")
                .addParameter("./jcr:lastModified",             "")
                .addParameter("./jcr:lastModified@TypeHint",    "Date");

        if (description != null) {
            form.addParameter("./jcr:description", description);
        }

        if (sku != null) {
            form.addParameter("./identifier",               sku);
        }

        if (color != null) {
            form.addParameter("./color",                    color);
        }

        if (size != null) {
            form.addParameter("./size",                     size);
        }

        return this.doPost(parentProduct + "/*", form.build(), SC_CREATED);

    }

    /**
     * Create a new blueprint.
     *
     * @param title Title
     * @param description Description
     * @param parentPath Path under which the blueprint should be created
     * @param catalogTemplate Path to catalog page template
     * @param sectionTemplate Path to section page template
     * @param productTemplate Path to product page template
     * @param proxyPages Whether proxy pages should be generated
     * @return SlingHttpResponse object
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse createBlueprint(String title, String description, String parentPath, String catalogTemplate, String sectionTemplate, String productTemplate, boolean proxyPages)
            throws ClientException {

        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter("_charset_", "UTF-8")
                .addParameter("./jcr:content/templates/catalog", catalogTemplate)
                .addParameter("./jcr:content/templates/section", sectionTemplate)
                .addParameter("./jcr:content/templates/product", productTemplate)
                .addParameter("./jcr:content/templates/proxyProductPages", proxyPages ? "true" : "false") // No type hint boolean?
                .addParameter("./jcr:content/filter/searchType", "simple")
                .addParameter("./jcr:content/jcr:title", title)
                .addParameter("./jcr:content/jcr:description", description)
                .addParameter("./jcr:content/jcr:primaryType", "cq:PageContent")
                .addParameter("./jcr:content/sling:resourceType", "commerce/components/catalog")
                .addParameter("./jcr:content/cq:template", "/libs/commerce/templates/catalog")
                .addParameter("./jcr:content/cq:catalogVersion", "561")
                .addParameter("./jcr:primaryType", "cq:Page")
                .addParameter("parentPath", parentPath)
                .addParameter(":nameHint", title);

        return this.doPost(parentPath + "/*", form.build(), SC_CREATED);

    }


    /**
     * Create a new blueprint section.
     *
     * @param name Name
     * @param description Description
     * @param parentPath Path of the parent blueprint or blueprint section
     * @param proxyPages Whether proxy pages should be generated
     * @return SlingHttpResponse object
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse createBlueprintSection(String name, String description, String parentPath, boolean proxyPages) throws ClientException {

        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter("./jcr:content/jcr:title", name)
                .addParameter("./jcr:content/jcr:description", description)
                .addParameter("./jcr:content/templates/proxyProductPages", proxyPages ? "true" : "false")
                .addParameter("./jcr:content/filter/searchType", "simple")
                .addParameter("./jcr:content/jcr:primaryType", "cq:PageContent")
                .addParameter("./jcr:content/sling:resourceType", "commerce/components/section")
                .addParameter("./jcr:content/cq:template", "/libs/commerce/templates/section")
                .addParameter("./jcr:content/target/jcr_title", name)
                .addParameter("_charset_", "UTF-8")
                .addParameter("parentPath", parentPath)
                .addParameter("./jcr:primaryType", "cq:Page")
                .addParameter(":nameHint", name);

        return this.doPost(parentPath + "/*", form.build(), SC_CREATED);

    }

    /**
     * Check if a product page is a regular product page based on its resource type.
     *
     * @param productPage Path to product page
     * @return true if the page is a product page
     * @throws ClientException if the request fails
     */
    public boolean isProductPage(String productPage) throws ClientException {

        JsonNode product = this.doGetJson(productPage, -1, SC_OK);
        String resourceType = product.get("jcr:content").get("sling:resourceType").getTextValue();

        return PRODUCT_PAGE_RESOURCE_TYPE.equals(resourceType);

    }

    /**
     * Check if a product page is a proxy product page based on its resource type.
     *
     * @param productPage Path to product page
     * @return true if the page is a proxy
     * @throws ClientException if the request fails
     */
    public boolean isProxyPage(String productPage) throws ClientException {

        JsonNode product = this.doGetJson(productPage, -1, SC_OK);
        String resourceType = product.get("jcr:content").get("sling:resourceType").getTextValue();

        return PROXY_PRODUCT_PAGE_RESOURCE_TYPE.equals(resourceType);

    }

    /**
     * Set the product filter of a blueprint or blueprint section using a product base path and a tag.
     *
     * @param blueprintPath Blueprint or blueprint section path
     * @param productFolder Product Folder
     * @param tag Tag
     * @return SlingHttpResponse object
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse addTagFilter(String blueprintPath, String productFolder, String tag) throws ClientException {

        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter("_charset_", "UTF-8")
                .addParameter("./filter/basePath", productFolder)
                .addParameter("./filter/matchTags", tag)
                .addParameter("./filter/matchTags@TypeHint", "String[]")
                .addParameter("./filter/matchTags@Delete", "");

        return this.doPost(blueprintPath + "/_jcr_content", form.build(), SC_OK);

    }

    /**
     * Set the product filter of a blueprint or blueprint section using a JCR-SQL2 query.
     *
     * @param blueprintPath Blueprint or blueprint section path
     * @param query JCQ-SQL2 query
     * @return SlingHttpResponse object
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse addSQL2QueryFilter(String blueprintPath, String query) throws ClientException {

        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter("_charset_", "UTF-8")
                .addParameter("./filter/searchType", "JCR-SQL2")
                .addParameter("./filter/search", query);

        return this.doPost(blueprintPath + "/_jcr_content", form.build(), SC_OK);

    }

    /**
     * Set the product filter of a blueprint or blueprint section using an XPath query.
     *
     * @param blueprintPath Blueprint or blueprint section path
     * @param query XPath query
     * @return SlingHttpResponse object
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse addXPathQueryFilter(String blueprintPath, String query) throws ClientException {

        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter("_charset_", "UTF-8")
                .addParameter("./filter/searchType", "xpath")
                .addParameter("./filter/search", query);

        return this.doPost(blueprintPath + "/_jcr_content", form.build(), SC_OK);

    }

    /**
     * Clears the shopping cart using the ContextHub. Works only on the client side.
     *
     * @param page Content page that can be used to call the ContextHub
     * @return SlingHttpResponse object
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse clearShoppingCart(String page) throws ClientException {

        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter("cart", "{\"entries\":[],\"promotions\":[],\"vouchers\":[]}");

        return this.doPost(page + "/jcr:content/contexthub.commerce.cart.json", form.build(), SC_OK);

    }

    /**
     * Adds or updates the cq:commerceProvider property to a JCR node at a given url.
     *
     * @param url URL of the node
     * @param commerceProvider commerce provider identifier
     * @return SlingHttpResponse object
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse setCommerceProvider(String url, String commerceProvider) throws ClientException {

        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter("cq:commerceProvider", commerceProvider);

        return this.doPost(url, form.build(), SC_OK);

    }

    public final static class Builder extends InternalBuilder<CommerceClient> {

        private Builder(URI url, String user, String password) {
            super(url, user, password);
        }

        public static Builder create(URI url, String user, String password) {
            return new Builder(url, user, password);
        }

        @Override public CommerceClient build() throws ClientException {
            return new CommerceClient(buildHttpClient(), buildSlingClientConfig());
        }

    }

    private String urlEncode(final String text) throws ClientException {
        try {
            return URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ClientException("Failed to encode '" + text + "'", e);
        }
    }

    private String urlDecode(final String text) throws ClientException {
        try {
            return URLDecoder.decode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ClientException("Failed to decode '" + text + "'", e);
        }
    }

}
