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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.util.JsonUtils;
import org.apache.sling.testing.clients.util.poller.Polling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.concurrent.TimeoutException;

import static org.apache.http.HttpStatus.SC_OK;

/**
 * A poller that checks the occurrence of a given OSGi event.
 *
 * Can either be used via the checkEventOccurrence() class method or by
 * instantiating an eventPoller and calling the callUntilCondition() method.
 */
public class EventPoller extends Polling {

    private static final Logger LOG = LoggerFactory.getLogger(EventPoller.class);

    private static final String EVENT_URL = "/system/console/events.json";

    private String topic;
    private JsonNode events;
    private String path;
    private SlingClient client;
    private long since;

    /**
     * Instantiate a new EventPoller
     *
     * @param client required to read from the event log
     * @param topic the topic that should be looked for
     * @param path the path the event should occur
     * @param since the time from which on events are considered
     */
    public EventPoller(SlingClient client, String topic, String path, long since) {
        super();
        this.topic = topic;
        this.path = path;
        this.since = since;
        this.client = client;
    }

    @Override
    public Boolean call() throws ClientException {
        this.events = getEventsSince(this.client, this.since);
        return containsEvent(this.events, this.topic, this.path);
    }

    /**
     * Returns a timestamp of the latest recorded OSGi event.
     *
     * @param client required to read from the event log
     * @return a timestamp of the latest event
     * @throws ClientException if request to OSGi event log could not be made
     */
    public static long getLatestEventTimestamp(SlingClient client) throws ClientException {

        JsonNode eventData = JsonUtils.getJsonNodeFromString(client.doGet(EVENT_URL, SC_OK).getContent());
        JsonNode allEvents = eventData.get("data");

        long mostRecent = 0;
        for (Iterator<JsonNode> it = allEvents.elements(); it.hasNext(); ) {
            JsonNode event = it.next();
            long received = event.get("received").longValue();

            if (received > mostRecent) {
                mostRecent = received;
            }
        }

        return mostRecent;
    }

    /**
     * Returns all OSGi event since a given time.
     *
     * @param client required to read from the event log
     * @param timestamp the time from which on events are considered
     * @return an array of events as JSONArray
     * @throws ClientException if request to OSGi event log could not be made
     */
    public static JsonNode getEventsSince(SlingClient client, long timestamp) throws ClientException {

        JsonNode eventData = JsonUtils.getJsonNodeFromString(client.doGet(EVENT_URL, SC_OK).getContent());
        JsonNode allEvents = eventData.get("data");
        ArrayNode events  = new ObjectMapper().createArrayNode();

        for (Iterator<JsonNode> it = allEvents.elements(); it.hasNext(); ) {
            JsonNode event = it.next();

            if (event.get("received").longValue() > timestamp) {
                events.add(event);
            }
        }

        return events;
    }

    /**
     * Checks if a given array of events contains an event with a given topic at a given path
     *
     * @param events array of events that should be searched
     * @param topic the topic that should be looked for
     * @param path the path the event should occur
     * @return true if the event was found in the given array
     */
    public static boolean containsEvent(JsonNode events, String topic, String path) {
        for (Iterator<JsonNode> it = events.elements(); it.hasNext(); ) {
            JsonNode event = it.next();


            if (event.get("topic").textValue().equals(topic)) {
                JsonNode properties = event.get("properties");
                JsonNode eventPath = properties.get("path");

                if (eventPath != null && eventPath.textValue().equals(path)) {
                    return true;
                }

                JsonNode paths = properties.get("paths");
                if (paths != null) {
                    for (Iterator<JsonNode> it1 = paths.elements(); it1.hasNext(); ) {
                        JsonNode otherPath = it1.next();
                        if (otherPath != null && otherPath.textValue().equals(path)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Asynchronously checks for the occurrence of a given event in the OSGi
     * event log.
     *
     * @param client required to read from the event log
     * @param topic the topic that should be looked for
     * @param path the path the event should occur
     * @param since the time from which on events are considered
     * @return true if the event occurred.
     * @throws InterruptedException to mark this method as waiting
     */
    public static boolean checkEventOccurrence(SlingClient client, String topic, String path, long since)
            throws InterruptedException {

        EventPoller poller = new EventPoller(client, topic, path, since);
        try {
            poller.poll(5000, 100);
            return true;
        } catch (TimeoutException e) {
            LOG.warn("Interrupted while polling for event " + topic + " at " + path + ".", e);
            return false;
        }
    }

}
