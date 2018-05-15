<%--
  Copyright 2017 Adobe Systems Incorporated

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
--%><%
%><%@ page session="false" import="java.util.Map" %><%
%><%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.0"%><sling:defineObjects /><%
boolean ready = true;
boolean indexing = false;

Map<Thread, StackTraceElement[]> all = Thread.getAllStackTraces();
for (Thread t : all.keySet()) {
   try {
        String name = t.getName();
        if (name.startsWith("Thread-") || name.startsWith("pool-") || name.startsWith("CM Event Dispatcher")) {
            log.debug("instanceReadyPoller: found Thread " + name);
            StackTraceElement[] traces = t.getStackTrace();
            for (StackTraceElement ste : traces) {
                if (ste.getClassName().startsWith("org.apache.jackrabbit.oak.plugins.index.AsyncIndexUpdate")) {
                  indexing = true;
                }
              if (ste.getClassName().startsWith("org.apache.felix.scr.impl.manager.ServiceTracker")) {
                ready = false;
                break;
              }
            }
       }
       if (!ready) {
           break; // stop further thread checks
       }
   } catch (Exception e) {
      log.error(e.getMessage(), e);
   }
}
%>{"ready":<%= ready %>,"indexing":<%= indexing %>}