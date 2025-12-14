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
package com.adobe.cq.testing.client.util;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/** helper methods for JsonNode operations */
public class JsonNodeUtils {

  private JsonNodeUtils() {
    // utility class
  }

  /**
   * Gets text values from JsonNode array.
   *
   * @param node the JsonNode containing array elements
   * @return set of text values
   */
  public static Set<String> getTxtElements(JsonNode node) {
    // node stream to set
    Stream<JsonNode> elemStream = getElements(node);
    Stream<String> txtStream = elemStream.map(JsonNode::textValue);
    Set<String> result = txtStream.collect(Collectors.toSet());
    return result;
  }

  /**
   * to obtain stream of elements from JsonNode.
   *
   * @param node JsonNode to convert
   * @return stream of JsonNode elements
   */
  public static Stream<JsonNode> getElements(JsonNode node) {
    // Check if node is null
    if (node == null) {
      return Stream.empty();
    }

    // for stream conversion
    Iterator<JsonNode> elementsIt = node.elements();
    Spliterator<JsonNode> spliterator =
        Spliterators.spliteratorUnknownSize(elementsIt, Spliterator.ORDERED);
    Stream<JsonNode> elemStream = StreamSupport.stream(spliterator, false);
    return elemStream;
  }
}
