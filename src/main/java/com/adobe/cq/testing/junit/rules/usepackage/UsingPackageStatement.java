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
package com.adobe.cq.testing.junit.rules.usepackage;

import com.adobe.cq.testing.client.CQClient;
import com.adobe.cq.testing.client.PackageManagerClient;
import com.adobe.cq.testing.util.FileVaultPackageUtil;
import org.apache.commons.io.IOUtils;
import org.junit.runners.model.Statement;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class UsingPackageStatement extends Statement {

    private Statement base;

    private UsePackageRule rule;

    public UsingPackageStatement(UsePackageRule rule, Statement base) {
        this.base = base;
        this.rule = rule;
    }

    @Override
    public void evaluate() throws Throwable {
        PackageManagerClient.Package uploadedPackage = null;
        try {
            //Before:
            File packFile = FileVaultPackageUtil.getInstance().generatePackage(rule.getSrcPath());
            CQClient adminAuthor = rule.getInstance().getAdminClient(CQClient.class);
            PackageManagerClient packClient =  adminAuthor.adaptTo(PackageManagerClient.class);
            uploadedPackage = packClient.uploadPackage(new FileInputStream(packFile), packFile.getName());
            uploadedPackage.install();

            //Test:
            base.evaluate();
        } finally {
            //After:
            if (uploadedPackage != null) {
                uploadedPackage.unInstall();
                uploadedPackage.delete();
            }
        }
    }

}
