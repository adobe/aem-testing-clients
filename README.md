# AEM testing clients
HTTP testing clients and utilities for AEM, based on [sling.testing.clients](https://github.com/apache/sling-org-apache-sling-testing-clients).

## How to build
Clone the repository and run:
```bash
mvn clean install
```

## How to use
Add the following dependency to your tests project to start writing integration tests:
```xml
<dependency>
    <groupId>com.adobe.cq</groupId>
    <artifactId>cq-testing-clients-64</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```
