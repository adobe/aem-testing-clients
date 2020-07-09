[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.adobe.cq/cq-testing-clients-65/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.adobe.cq/cq-testing-clients-65)
[![CircleCI](https://circleci.com/gh/adobe/aem-testing-clients/tree/cq-65.svg?style=svg)](https://circleci.com/gh/adobe/aem-testing-clients/tree/cq-65)
[![codecov](https://img.shields.io/codecov/c/github/adobe/aem-testing-clients/cq-65.svg)](https://codecov.io/gh/adobe/aem-testing-clients/branch/cq-65)
[![javadoc](https://javadoc.io/badge2/com.adobe.cq/cq-testing-clients-65/javadoc.svg)](https://javadoc.io/doc/com.adobe.cq/cq-testing-clients-65)

# AEM testing clients
HTTP testing clients and utilities for AEM, based on [sling.testing.clients](https://github.com/apache/sling-org-apache-sling-testing-clients).

## How to use
Add the following dependency to your tests project to start writing integration tests:
```xml
<dependencies>
    <dependency>
        <groupId>com.adobe.cq</groupId>
        <artifactId>cq-testing-clients-65</artifactId>
        <version>1.1.1</version>
    </dependency>
</dependencies>


```

For an example of a test module, check the [aem-test-samples](https://github.com/adobe/aem-test-samples)

## Documentation
* Check the [wiki](https://github.com/adobe/aem-testing-clients/wiki)
* Read the README from [sling.testing.clients](https://github.com/apache/sling-org-apache-sling-testing-clients) and
[sling.testing.rules](https://github.com/apache/sling-org-apache-sling-testing-rules)

## Development
For building from sources, clone the repository and use maven:
```bash
mvn clean install
```

PRs and issues are welcome, please read our [CONTRIBUTING guideline](CONTRIBUTING.md). 
