[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.adobe.cq/cq-testing-clients-64/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.adobe.cq/cq-testing-clients-64)
[![CircleCI](https://circleci.com/gh/adobe/aem-testing-clients/tree/cq-64.svg?style=svg)](https://circleci.com/gh/adobe/aem-testing-clients/tree/cq-64)
[![codecov](https://img.shields.io/codecov/c/github/adobe/aem-testing-clients/cq-64.svg)](https://codecov.io/gh/adobe/aem-testing-clients/branch/cq-64)
[![javadoc](https://javadoc.io/badge2/com.adobe.cq/cq-testing-clients-64/javadoc.svg)](https://javadoc.io/doc/com.adobe.cq/cq-testing-clients-64)

# AEM testing clients
HTTP testing clients and utilities for AEM, based on [sling.testing.clients](https://github.com/apache/sling-org-apache-sling-testing-clients).

## How to use
Add the following dependency to your tests project to start writing integration tests:
```xml
<dependencies>
    <dependency>
        <groupId>com.adobe.cq</groupId>
        <artifactId>cq-testing-clients-64</artifactId>
        <version>0.1.3</version>
    </dependency>
</dependencies>


```

The artifact is available on [Maven Central](https://mvnrepository.com/repos/central) 
and it is discoverable in any default maven installation.

For an example of a test module, check the [aem-test-samples](https://github.com/adobe/aem-test-samples)

## Documentation
* Check the [wiki](https://github.com/adobe/aem-testing-clients/wiki)
* Read the README from [sling.testing.clients](https://github.com/apache/sling-org-apache-sling-testing-clients) and
[sling.testing.rules](https://github.com/apache/sling-org-apache-sling-testing-rules)
* Check the [javadoc](http://adobe.github.io/aem-testing-clients/apidocs/cq-testing-clients-64/0.1.0/index.html)

## Development
For building from sources, clone the repository and use maven:
```bash
mvn clean install
```

PRs and issues are welcome, please reade our [CONTRIBUTING guideline](CONTRIBUTING.md). 
