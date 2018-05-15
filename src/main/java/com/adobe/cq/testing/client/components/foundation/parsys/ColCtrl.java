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

package com.adobe.cq.testing.client.components.foundation.parsys;

import com.adobe.cq.testing.client.ComponentClient;
import com.adobe.cq.testing.client.components.AbstractComponent;
import com.adobe.cq.testing.client.components.foundation.AbstractFoundationComponent;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingHttpResponse;

/**
 * Created with IntelliJ IDEA.
 * User: catalan
 * Date: 5/29/12
 * Time: 10:13 AM
 */
public class ColCtrl extends AbstractFoundationComponent {

    public static final String RESOURCE_TYPE = "foundation/components/parsys/colctrl";

    public static final String PROP_RESOURCE_TYPE = "sling:resourceType";
    public static final String PROP_CONTROL_TYPE = "controlType";
    public static final String PROP_LAYOUT = "layout";
    public static final String PROP_ORDER = ":order";

    public static final String COL_BREAK_CONTROL_TYPE = "break";
    public static final String COL_END_CONTROL_TYPE = "end";

    public static final String LAYOUT_2_COLS = "2;colctrl-2c";
    public static final String LAYOUT_3_COLS = "3;colctrl-3c";

    private AbstractFoundationComponent endColComponent;
    private AbstractFoundationComponent[] colBreaks;

    /**
     * The constructor stores all the component path information like parentPage, name etc.
     *
     * @param client   The {@link com.adobe.cq.testing.client.FoundationClient FoundationClient} that's
     *                 creating this
     *                 instance.
     * @param pagePath path to the page that will contain the component.
     * @param location relative location to the parent node inside the page that will contain the component node.
     * @param nameHint name to be used for the component node. Might get altered by the server if a naming conflict
     *                 occurs. The {@link #getName()} method will return the correct name after {@link #create
     *                 (String,int...)} has been called.
     */
    public ColCtrl(ComponentClient client, String pagePath, String location, String nameHint) {
        super(client, pagePath, location, nameHint);
    }

    /**
     * We override the default create call, so we can capture the reference to the end component.
     * The end component is created right after the colctrl component.
     *
     * @param order          Defines where the component should be added in relation to its siblings. Possible values
     *                       are {@code first}, {@code last}, {@code before [nodeName]}, {@code after [nodeName]}.
     * @param expectedStatus list of expected HTTP Status to be returned, if not set, 201 is assumed.
     * @return the response
     * @throws ClientException if the request fails
     */
    @Override
    public SlingHttpResponse create(String order, int... expectedStatus) throws ClientException, InterruptedException {
        SlingHttpResponse res = super.create(order, expectedStatus);
        endColComponent = getNext();
        return res;
    }

    @Override
    public String getResourceType() {
        return RESOURCE_TYPE;
    }

    public <T extends AbstractComponent> T addComponent(Class<T> componentClass, int colNum) throws Exception {
        String idx = getOrderPropertyForCol(colNum);
        return client.addComponent(componentClass, pagePath, idx);
    }

    public String getOrderPropertyForCol(int colNum) throws Exception {
        checkColNumExists(colNum);

        int lColNum = getLayoutColNum();

        // Add component to the last column
        if (colNum == lColNum) {
            return "before " + endColComponent.getName();
        }
        // Get the corresponding column break node
        else {
            AbstractFoundationComponent colBreak = colBreaks[colNum - 1];
            return "before " + colBreak.getName();
        }
    }

    public void setLayout(String layout) throws ClientException, InterruptedException {
        setProperty(PROP_LAYOUT, layout);
        save();

        int lColNum = getLayoutColNum();

        colBreaks = new AbstractFoundationComponent[lColNum];

        int idx = 0;
        AbstractFoundationComponent node = getNext();
        while (node != null
                && node.getResourceType().equals(RESOURCE_TYPE)
                && !node.getPropertyAsString(PROP_CONTROL_TYPE).equals(COL_END_CONTROL_TYPE)) {
            if (node.getResourceType().equals(RESOURCE_TYPE)
                    && node.getPropertyAsString(PROP_CONTROL_TYPE).equals(COL_BREAK_CONTROL_TYPE)) {
                colBreaks[idx] = node;
                idx++;
            }
            node = node.getNext();
        }
    }

    /**
     * Returns the end ColCtrl component that belongs to this start component.
     *
     * @return The end component for this start component
     * @throws ClientException
     *          if something fails during request/response
     */
    public AbstractFoundationComponent getColCtrlEndComponent() throws ClientException {
        return endColComponent;
    }

    /**
     * Returns the break nodes of the ColCtrl component.
     *
     * @return The array of colctrl break nodes
     */
    public AbstractFoundationComponent[] getColCtrlBreaks() {
        return colBreaks;
    }

    private void checkColNumExists(int colNum) throws Exception {
        int lColNum = getLayoutColNum();

        if (colNum < 1 || colNum > lColNum) {
            throw new Exception("Column index out of range (must be between 1 AND LAYOUT_COL_NUMBER)");
        }
    }

    private int getLayoutColNum() {
        String layout = this.getPropertyAsString(PROP_LAYOUT);

        if (layout == null) {
            return 1;
        } else {
            return Integer.parseInt(layout.substring(0, 1));
        }
    }
}
