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
package com.adobe.cq.testing.polling;

import com.adobe.cq.testing.client.WorkflowClient;
import com.adobe.cq.testing.client.workflow.HistoryItem;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.util.poller.Polling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

/**
 * A Utility Poller to wait until the worflow history contains any matching
 * condition to be present For example you can check if the workflow history
 * contains the process called "End" and that the status is "Completed"
 * 
 * As usually as a poller it need that you provide the polling interval and max
 * number of attempts before it abort the polling
 *
 */
public class WorkflowInstanceHistoryPoller extends Polling {

    private Logger LOG = LoggerFactory.getLogger(WorkflowInstanceHistoryPoller.class);

    private String wfInstancePath;

    private WorkflowClient client;

    private String expectedProcess;

    private String expectedStatus;

    public WorkflowInstanceHistoryPoller(WorkflowClient client, String workflowInstancePath) {
        super();
        this.wfInstancePath = workflowInstancePath;
        this.client = client;
    }

    @Override
    public Boolean call() throws ClientException {
        boolean result;
        if (expectedProcess != null) {
            List<HistoryItem> historyItems = client.getWorkflowInstanceHistory(wfInstancePath);
            return hasState(historyItems);
        } else {
            LOG.warn("No value set for the nodeId to lookup for, will skip");
            return true;
        }
    }

    /**
     * Set the condition that will define the polling
     * 
     * @param expectedProcess
     *            the process title that is expected to be found in the history
     *            items
     * @param expectedStatus
     *            the status of that history item (i.e completed, active etc..)
     */
    public void setCondition(String expectedProcess, String expectedStatus) {
        this.expectedProcess = expectedProcess;
        this.expectedStatus = expectedStatus;
    }

    /**
     * A method to check that the history items contains the matching condition
     * defined in this poller
     * 
     * @param historyItems
     *            history items as a list
     * @return true if the expected status and expected process are present in
     *         the history items
     */
    private boolean hasState(List<HistoryItem> historyItems) {
        boolean found = false;
        Iterator<HistoryItem> historyEntryIterator = historyItems.iterator();
        while (!found && historyEntryIterator.hasNext()) {
            // read the history and map
            HistoryItem historyItem = historyEntryIterator.next();
            String historyProcess = historyItem.getProcess();
            String status = historyItem.getStatus();
            found = historyProcess.equals(expectedProcess) && status.equals(expectedStatus);
        }
        return found;
    }
}
