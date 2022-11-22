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
            File packFile = generatePackage(rule.getSrcPath());
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


    private File generatePackage(final String resourceFolder) throws IOException, URISyntaxException {
        File generatedPackage = Files.createTempFile("temp-package-", ".zip").toFile();
        generatedPackage.deleteOnExit();

        addResourcesToPackage(resourceFolder, initPackage(generatedPackage));

        return generatedPackage;
    }

    private JarOutputStream initPackage(File generatedPackage) throws IOException {
        Manifest man = new Manifest();
        Attributes atts = man.getMainAttributes();
        atts.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        atts.putValue("Build-Jdk", ManagementFactory.getRuntimeMXBean().getVmVersion());

        return new JarOutputStream(new FileOutputStream(generatedPackage), man);
    }

    private void addResourcesToPackage(final String resourceFolder, final JarOutputStream outJar) throws URISyntaxException, IOException {
        URI uri = getClass().getResource(resourceFolder).toURI();
        URL urlRoot = getClass().getResource("/");
        final String rootPath = urlRoot != null ? urlRoot.getPath() : "/";

        // Map jar scheme into new FileSystem in order for Paths.get(uri) to resolve it as for local filesystem.
        try (FileSystem fileSystem = (uri.getScheme().equals("jar") ? FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap()) : null)) {
            final Path myPath = Paths.get(uri);
            Files.walkFileTree(myPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (!attrs.isDirectory() && attrs.isRegularFile()) {
                        String rootRelatedPath = file.toString().substring(rootPath.length() - 1);
                        JarEntry je = new JarEntry(rootRelatedPath.substring(resourceFolder.length() + 1));
                        je.setTime(attrs.lastModifiedTime().toMillis());
                        je.setSize(attrs.size());
                        outJar.putNextEntry(je);
                        IOUtils.copy(getClass().getResourceAsStream(rootRelatedPath.toString()), outJar);
                        outJar.closeEntry();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        outJar.close();
    }

}
