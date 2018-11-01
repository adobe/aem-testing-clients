[![CircleCI](https://circleci.com/gh/adobe/aem-testing-clients/tree/cq-64.svg?style=svg)](https://circleci.com/gh/adobe/aem-testing-clients/tree/cq-64)

# AEM testing clients
HTTP testing clients and utilities for AEM, based on [sling.testing.clients](https://github.com/apache/sling-org-apache-sling-testing-clients).

## How to use
Add the following dependency to your tests project to start writing integration tests:
```xml
<dependencies>
    <dependency>
        <groupId>com.adobe.cq</groupId>
        <artifactId>cq-testing-clients-65</artifactId>
        <version>0.1.0</version>
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

PRs and issues are welcome, please reade our [CONTRIBUTING guideline](CONTRIBUTING.md). 
