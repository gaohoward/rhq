/*
 * RHQ Management Platform
 * Copyright (C) 2005-2012 Red Hat, Inc.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.rhq.test.arquillian.impl.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;

import org.rhq.core.util.ZipUtil;
import org.rhq.core.util.file.FileUtil;

/**
 * 
 *
 * @author Lukas Krejci
 */
public class SigarInstaller {

    private static final Log LOG = LogFactory.getLog(SigarInstaller.class);

    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");

    private File rootDir;
    private MavenResolvedArtifact sigarDistArtifact;

    public SigarInstaller(File rootDir) {
        this.rootDir = rootDir;
        init();
    }

    private void init() {

        // Read the properties from the Maven filtered resource file
        Properties pomProperties = new Properties();
        InputStream propertyFileInputStream = getClass().getClassLoader().getResourceAsStream(
            "maven-properties.properties");
        try {
            pomProperties.load(propertyFileInputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (propertyFileInputStream != null) {
                try {
                    propertyFileInputStream.close();
                } catch (IOException ignore) {
                }
            }
        }

        String sigarVersion = pomProperties.getProperty("sigar.version");
        if (sigarVersion == null) {
            throw new RuntimeException("Could not read Sigar version from the properties file");
        }

        sigarDistArtifact = Maven.resolver().offline().loadPomFromFile("pom.xml")
            .resolve("org.hyperic:sigar-dist:zip:" + sigarVersion).withoutTransitivity().asSingleResolvedArtifact();
    }

    public boolean isSigarAvailable() {
        return sigarDistArtifact != null;
    }

    public void installSigarNativeLibraries() {
        LOG.debug("Installing SIGAR native libraries to [" + rootDir + "]...");

        File tempDir = new File(TMP_DIR, UUID.randomUUID().toString());
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        File sigarLibDir = null;
        try {
            File explodedDir = new File(tempDir, "sigar-dist");
            LOG.debug("Unzipping " + sigarDistArtifact + " to " + tempDir + "...");
            ZipUtil.unzipFile(sigarDistArtifact.asFile(), explodedDir);

            sigarLibDir = findSigarLibDir(explodedDir);
            // Make sure the target dir does not exist, since FileUtil.copyDirectory() requires that to be the case.
            FileUtil.purge(rootDir, true);

            FileUtil.copyDirectory(sigarLibDir, rootDir);

            // The Sigar class uses the below sysprop to locate the SIGAR native libraries.
            System.setProperty("org.hyperic.sigar.path", rootDir.getPath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to copy SIGAR shared libraries from [" + sigarLibDir + "] to ["
                + rootDir + "].", e);
        } finally {
            FileUtil.purge(tempDir, true);
        }
    }

    private static File findSigarLibDir(File explodedSigarDistZip) {
        //the sigar-dist zip contains one root directory called "hyperic-sigar-<version>", let's look it up
        File[] foundRootDirs = explodedSigarDistZip.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("hyperic-sigar");
            }
        });

        if (foundRootDirs.length == 0) {
            throw new IllegalStateException(
                "Could not find a directory called \"hyperic-sigar-<VERSION>\" under the Sigar distribution ZIP file.");
        } else if (foundRootDirs.length > 1) {
            throw new IllegalStateException(
                "There seems to be more than 1 directory starting with \"hyperic-sigar\" under the Sigar distribution ZIP file.");
        }

        File rootDir = foundRootDirs[0];

        //k, now let's see if we can get down to "lib"
        File bin = new File(rootDir, "sigar-bin");
        if (!bin.exists()) {
            throw new IllegalStateException("\"sigar-bin\" not found in the Sigar distribution ZIP file.");
        }

        File lib = new File(bin, "lib");
        if (!lib.exists()) {
            throw new IllegalStateException("\"sigar-bin/lib\" not found in the Sigar distribution ZIP file.");
        }

        return lib;
    }
}
