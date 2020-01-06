package com.adobe.cq.testing.util;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;

public class FileVaultPackageUtilTest {

    @Test
    public void getInstance() {
        assertNotNull(FileVaultPackageUtil.getInstance());
    }

    @Test(expected = NullPointerException.class)
    public void generatePackageThrowNPEWithNoResourceFound() throws IOException, URISyntaxException {
            FileVaultPackageUtil.getInstance().generatePackage("/test-no-resources");
    }
    @Test
    public void generatePackageForExistingResources() throws IOException, URISyntaxException {
        File file = FileVaultPackageUtil.getInstance().generatePackage("/test-filevaultpackageutil");
        assertNotNull(file);
        assertTrue(file.exists());
        assertTrue(file.isFile());
        assertValidJar(file);
    }

    private void assertValidJar(File file) throws IOException {
        JarInputStream jarInput = new JarInputStream(new FileInputStream(file));
        assertNotNull(jarInput);
        Manifest manifest = jarInput.getManifest();
        assertNotNull(manifest);
        assertNotNull(manifest.getMainAttributes().getValue("Build-Jdk"));
        assertEquals("META-INF/vault/properties.xml", jarInput.getNextJarEntry().getName());
        assertEquals("META-INF/vault/filter.xml", jarInput.getNextJarEntry().getName());
        assertEquals("jcr_root/.content.xml", jarInput.getNextJarEntry().getName());
        assertEquals("jcr_root/content/.content.xml", jarInput.getNextJarEntry().getName());
        assertEquals("jcr_root/content/dummy-content/.content.xml", jarInput.getNextJarEntry().getName());
        assertNull(jarInput.getNextJarEntry());
    }
}