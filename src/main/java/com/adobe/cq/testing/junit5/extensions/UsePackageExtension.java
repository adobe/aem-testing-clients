/*
 * Copyright 2019 Adobe Systems Incorporated
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
package com.adobe.cq.testing.junit5.extensions;

import com.adobe.cq.testing.client.CQClient;
import com.adobe.cq.testing.client.PackageManagerClient;
import com.adobe.cq.testing.junit5.annotations.SlingClientContext;
import com.adobe.cq.testing.junit5.annotations.UsePackage;
import com.adobe.cq.testing.util.AnnotationHelper;
import com.adobe.cq.testing.util.FileVaultPackageUtil;
import org.apache.sling.testing.clients.ClientException;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;

public class UsePackageExtension implements BeforeAllCallback, BeforeEachCallback, AfterAllCallback, AfterEachCallback, ExecutionCondition {

    private final Logger LOGGER = LoggerFactory.getLogger(UsePackageExtension.class);

    private final SlingClientExtension.Store clientExtensionStore = SlingClientExtension.Store.getInstance();

    private String srcPath;
    private boolean once;
    private String uploadedPackagePath;

    /**
     * Constructor used along with @RegisterExtension annotation Field level
     * @param srcPath Define the location relative to resources or absolute
     */
    public UsePackageExtension(String srcPath) {
        this.srcPath = srcPath;
    }

    /**
     * Constructor used by @UsePackage annotation at Class level
     */
    public UsePackageExtension() {
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        if (once) {
            after(extensionContext);
        }
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        if (!once) {
            after(extensionContext);
        }
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        if (once) {
            before(extensionContext);
        }
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        if (!once) {
            before(extensionContext);
        }
    }

    private void before(ExtensionContext extensionContext) throws ClientException, IOException, URISyntaxException {
        if (srcPath == null) {
            srcPath = getSrcPath(extensionContext);
        }
        assert !StringUtils.isBlank(srcPath);
        File packFile = FileVaultPackageUtil.getInstance().generatePackage(srcPath);
        PackageManagerClient packClient = getPackageManagerClient(extensionContext);
        PackageManagerClient.Package uploadedPackage = packClient.uploadPackage(new FileInputStream(packFile), packFile.getName());
        uploadedPackage.install();
        uploadedPackagePath = uploadedPackage.getPath();
        LOGGER.info(() -> String.format("Package %s installed", srcPath));
    }

    private boolean getRunOnce(ExtensionContext extensionContext) {
        return AnnotationHelper.findOptionalAnnotation(extensionContext, UsePackage.class).map(usePackage -> usePackage.runOnce()).get();
    }

    private String getSrcPath(ExtensionContext extensionContext) {
        return AnnotationHelper.findOptionalAnnotation(extensionContext, UsePackage.class).map(usePackage -> usePackage.srcPath()).get();
    }

    private String getRunMode(ExtensionContext extensionContext) {
        return AnnotationHelper.findOptionalAnnotation(extensionContext, UsePackage.class).map(usePackage -> usePackage.runMode()).orElse(Constants.RUNMODE_AUTHOR);
    }

    private void after(ExtensionContext extensionContext) throws ClientException {
        if (uploadedPackagePath != null) {
            PackageManagerClient packClient = getPackageManagerClient(extensionContext);
            PackageManagerClient.Package uploadedPackage = packClient.getPackage(uploadedPackagePath);
            uploadedPackage.unInstall();
            uploadedPackage.delete();
            LOGGER.info(() -> String.format("Package %s uninstalled and deleted", srcPath));
        }
    }

    private PackageManagerClient getPackageManagerClient(ExtensionContext extensionContext) throws ClientException {
        String runMode = getRunMode(extensionContext);
        CQClient adminAuthor = clientExtensionStore.getOrCompute(extensionContext, runMode, false).adaptTo(CQClient.class);
        return adminAuthor.adaptTo(PackageManagerClient.class);
    }

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        if (!AnnotationHelper.findOptionalAnnotation(context, SlingClientContext.class).isPresent()) {
            throw new ExtensionConfigurationException("@SlingClientContext not found");
        }
        // init once at the same time
        once = getRunOnce(context);
        return ConditionEvaluationResult.enabled("@SlingClientContext found");
    }
}
