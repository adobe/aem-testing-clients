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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.runners.model.Statement;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.Iterator;
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

    
    private File generatePackage(String resourceFolder) throws IOException {
        URL res = getClass().getResource(resourceFolder);
        String srcPath = null;
        if (res.toString().startsWith("jar:")) {
            // extract jar in temp folder
            // TODO
        } else {
            // resource is not in jar
            srcPath = res.toString();
            if (srcPath.startsWith("file:")) {
                srcPath = srcPath.substring(5);
            }
        }

        return buildJarFromFolder(srcPath);
    }

    private File buildJarFromFolder(String srcPath) throws IOException {
        File generatedPackage = File.createTempFile("temp-package-", ".zip");
        generatedPackage.deleteOnExit();

        Manifest man = new Manifest();

        Attributes atts = man.getMainAttributes();
        atts.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        atts.putValue("Build-Jdk", ManagementFactory.getRuntimeMXBean().getVmVersion());

        JarOutputStream outJar = new JarOutputStream(new FileOutputStream(generatedPackage), man);
        
        Iterator<File> fileIter = FileUtils.iterateFilesAndDirs(new File(srcPath), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        while (fileIter.hasNext()) {
            File currFile = fileIter.next();
            if (currFile.isDirectory()) continue;
            String entryName = currFile.getAbsolutePath().substring(srcPath.length()+1);
            JarEntry je = new JarEntry(entryName);
            je.setTime(currFile.lastModified());
            je.setSize(currFile.length());
            outJar.putNextEntry(je);
            IOUtils.copy(new FileInputStream(currFile), outJar);
            outJar.closeEntry();
        }
        outJar.close();
        return generatedPackage;
    }

}
