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

package com.adobe.cq.testing.client.tartan;

import com.adobe.cq.testing.client.tartan.util.Team;
import com.adobe.cq.testing.client.tartan.util.TeamMember;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.Constants;
import org.apache.sling.testing.clients.SlingClientConfig;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.FormEntityBuilder;
import org.apache.sling.testing.clients.util.HttpUtils;
import org.apache.sling.testing.clients.util.ResourceUtil;
import org.codehaus.jackson.JsonNode;
import org.jsoup.nodes.Document;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.apache.http.HttpStatus.SC_OK;

public class BoardClient extends TartanClient {

    public static final String PIN_ACTION_DELETE = "delete";
    public static final String PIN_ACTION_UNPIN = "unpin";
    public static final String PIN_ACTION_PIN = "pin";

    private static final String DELETE_BOARD_PATH_SUFFIX = "/_jcr_content.delete.json";
    private static final String SAVE_BOARD_PATH_SUFFIX = "/_jcr_content.save.json";
    private static final String UPLOAD_ASSET_TO_BOARD_PATH_SUFFIX = "/jcr:content.createStaticCard.json";
    private static final String BOARD_PIN_ACTIONS_PATH_SUFFIX = "/_jcr_content.pin.json";
    private static final String BOARD_EDIT_CONTENT_PATH_SUFFIX = ".edit.html";

    private String boardsPath;
    private String createBoardPath;

    private List<String> cardExcludeFields = Arrays.asList("sling:resourceType", "jcr:primaryType");

    public BoardClient(CloseableHttpClient http, SlingClientConfig config) throws ClientException {
        super(http, config);

        homePath += "/home/users/" + getUser();
        boardsPath = homePath + "/boards";
        createBoardPath = boardsPath + SAVE_BOARD_PATH_SUFFIX;
    }

    public BoardClient(URI serverUrl, String user, String password) throws ClientException {
        super(serverUrl, user, password);

        homePath += "/home/users/" + getUser();
        boardsPath = homePath + "/boards";
        createBoardPath = boardsPath + SAVE_BOARD_PATH_SUFFIX;
    }

    /**
     * Edit an existing board
     *
     * @param boardPath
     *            URI Path of the board to modify
     * @param title
     *            Title of the Board
     * @param team
     *            Id of the user/owner of the Board
     * @param expectedStatus
     *            list of allowed HTTP Status to be returned. If not set, http
     *            status 201 (CREATED) is assumed.
     * @return Sling response
     * @throws ClientException
     *             If something fails during request/response cycle
     */
    public SlingHttpResponse editBoard(String boardPath, String title, Team team,
                                       int... expectedStatus) throws ClientException {
        String editBoardPath = boardPath + SAVE_BOARD_PATH_SUFFIX;

        // build the form to submit
        FormEntityBuilder feb = FormEntityBuilder.create();
        feb.addParameter(Constants.PARAMETER_CHARSET, Constants.CHARSET_UTF8);
        feb.addParameter("title", title);

        if (team == null) {
            team = new Team();
            team.addTeamMember(this.getUser(), TeamMember.PARAM_ROLE_OWNER, null);
        }

        for (TeamMember member : team.getMembers()) {
            feb.addParameter(TeamMember.PARAM_USER_ID, member.getUserId());
            feb.addParameter(TeamMember.PARAM_ROLE_ID, member.getRoleId());
            feb.addParameter(TeamMember.PARAM_MESSAGE, member.getMessage());
        }

        // ??? Unknown parameters for now, force them to null
        feb.addParameter("undefined.edit", null);

        // execute the request
        return doPost(editBoardPath, feb.build(), HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Create a new board for the current user
     *
     * @param title
     *            Title of the Board
     * @param team
     *            Id of the user/owner of the Board
     * @param expectedStatus
     *            list of allowed HTTP Status to be returned. If not set, http
     *            status 201 (CREATED) is assumed.
     * @return Sling response
     * @throws ClientException
     *             If something fails during request/response cycle
     */
    public SlingHttpResponse createBoard(String title, Team team, int... expectedStatus) throws ClientException {
        if (team == null) {
            team = new Team();
            team.addTeamMember(this.getUser(), TeamMember.PARAM_ROLE_OWNER, null);
        }

        return editBoard(createBoardPath, title, team, expectedStatus);
    }

    public SlingHttpResponse deleteBoard(String boardPath, int... expectedStatus) throws ClientException {
        // execute the request
        return doPost(boardPath + DELETE_BOARD_PATH_SUFFIX, null, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Uploads an <b>File</b> to the repository. Same as using {@code New File...} in the Site Admin outside of the
     * {@code Digital Assets} folder.<br>
     * <br>
     * This will create a folder with the file name and upload the file below it in a node typed {@code nt:file}.<br>
     * To upload a file that is to be handled as an Asset use {@link #uploadAsset}  instead.<br>
     * To upload a file directly using sling use
     * {@link #upload(java.io.File, String, String, boolean,int...)
     * upload}.
     *
     * @param fileName       file name. The file name will become part of the URL to the file.
     * @param resourcePath   defines the path to the resource
     * @param mimeType       MIME type of the image getting uploaded
     * @param boardPath      path of the board where the asset is uploaded
     * @param expectedStatus list of expected HTTP Status to be returned, if not set, 201 is assumed.
     * @return Sling response
     * @throws ClientException if something fails during the request/response cycle
     */
    public SlingHttpResponse uploadAssetToBoard(String fileName, String resourcePath, String mimeType,
                                                String boardPath, int... expectedStatus) throws ClientException {
        // create the request content
        HttpEntity entity = MultipartEntityBuilder.create()
                .addBinaryBody("file", ResourceUtil.getResourceAsStream(resourcePath), ContentType.create(mimeType), fileName)
                .addTextBody(Constants.PARAMETER_CHARSET, Constants.CHARSET_UTF8)
                .addTextBody("fileName", fileName)
                .build();

        // send the request with the multipart entity as content directly to the new location
        return doPost(boardPath + UPLOAD_ASSET_TO_BOARD_PATH_SUFFIX, entity, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Delete an uploaded asset
     *
     * @param boardPath
     *            URI Path of the board to modify
     * @param cardPath
     *            Path of the card in the Board
     * @param expectedStatus
     *            list of allowed HTTP Status to be returned. If not set, http
     * @return Sling response
     * @throws ClientException
     *             If something fails during request/response cycle
     */
    public SlingHttpResponse deleteAssetFromBoard(String boardPath, String cardPath, int... expectedStatus)
            throws ClientException {
        return doPinActionOnBoard(boardPath, PIN_ACTION_DELETE, cardPath, expectedStatus);
    }

    /**
     * Pin a card to a Board
     *
     * @param boardPath
     *            URI Path of the board to modify
     * @param cardPath
     *            Path of the card in the Board
     * @param expectedStatus
     *            list of allowed HTTP Status to be returned. If not set, http
     * @return Sling response
     * @throws ClientException
     *             If something fails during request/response cycle
     */
    public SlingHttpResponse pinCardToBoard(String boardPath, String cardPath, int... expectedStatus)
            throws ClientException {
        return doPinActionOnBoard(boardPath, PIN_ACTION_PIN, cardPath, expectedStatus);
    }

    /**
     * Unpin a referenced card
     *
     * @param boardPath
     *            URI Path of the board to modify
     * @param cardPath
     *            Path of the card in the Board
     * @param expectedStatus
     *            list of allowed HTTP Status to be returned. If not set, http
     * @return Sling response
     * @throws ClientException
     *             If something fails during request/response cycle
     */
    public SlingHttpResponse unpinCardFromBoard(String boardPath, String cardPath, int... expectedStatus)
            throws ClientException {
        return doPinActionOnBoard(boardPath, PIN_ACTION_UNPIN, cardPath, expectedStatus);
    }

    /**
     * Do a Pin action on a board
     *
     * @param boardPath
     *            URI Path of the board to modify
     * @param action
     *            Pin action to do (delete / unpin)
     * @param cardPath
     *            Path of the card in the Board
     * @param expectedStatus
     *            list of allowed HTTP Status to be returned. If not set, http
     * @return Sling response
     * @throws ClientException
     *             If something fails during request/response cycle
     */
    public SlingHttpResponse doPinActionOnBoard(String boardPath, String action, String cardPath,
                                                int... expectedStatus) throws ClientException {
        String editBoardPath = boardPath + BOARD_PIN_ACTIONS_PATH_SUFFIX;

        // build the form to submit
        FormEntityBuilder feb = FormEntityBuilder.create();
        feb.addParameter(Constants.PARAMETER_CHARSET, Constants.CHARSET_UTF8);
        feb.addParameter("cardPath", cardPath);
        feb.addParameter("action", action);

        // execute the request
        return doPost(editBoardPath, feb.build(), HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    public List<JsonNode> getCards(String boardPath) throws ClientException {
        List<JsonNode> cards = new ArrayList<>();

        JsonNode cardsJson = this.doGetJson(boardPath, -1).get("jcr:content").get("cards");
        Iterator<String> cardFields = cardsJson.getFieldNames();

        while (cardFields.hasNext()) {
            String fieldName = cardFields.next();

            if (!cardExcludeFields.contains(fieldName)) {
                cards.add(cardsJson.get(fieldName));
            }
        }

        return cards;
    }

    public Document getEditPageOfBoard(String boardPath) {
        return getPageContent(boardPath + BOARD_EDIT_CONTENT_PATH_SUFFIX);
    }

    public String getBoardsPath() {
        return boardsPath;
    }
}
