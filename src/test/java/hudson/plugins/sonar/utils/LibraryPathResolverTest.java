/*
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package hudson.plugins.sonar.utils;

import hudson.FilePath;
import org.apache.commons.io.FileUtils;
import org.jfree.util.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LibraryPathResolverTest {

    private static final int AMOUNT_OF_PROJECTS = 3;
    private static final String LIB_JAR = "**/lib/*.jar";

    private File testBaseDir = new File("target/testDir");
    private FilePath workspace = new FilePath(testBaseDir);
    private LibraryPathResolver pathResolver = new LibraryPathResolver();

    @Before
    public void before() throws IOException {
        WorkspaceCreationHelper.createFolderStructure(testBaseDir, AMOUNT_OF_PROJECTS);
    }

    @After
    public void after() throws IOException {
        FileUtils.deleteDirectory(testBaseDir);
    }

    @Test
    public void testDoResolvePath() throws IOException, InterruptedException {
        Set<String> stringSet = pathResolver.doResolvePath(workspace, "**/*.jar");
        assertEquals(AMOUNT_OF_PROJECTS, stringSet.size());
        for (String jarFile : stringSet) {
            assertTrue(jarFile + " doesn't end with the right extension", StringUtils.endsWithIgnoreCase(jarFile, ".jar"));
        }
    }


    @Test
    public void testCheckAndFixPattern() {
        assertEquals(LIB_JAR, pathResolver.checkAndFixPattern(LIB_JAR));
        assertEquals(LIB_JAR, pathResolver.checkAndFixPattern("**/lib/.jar"));
        assertEquals(LIB_JAR, pathResolver.checkAndFixPattern("**/lib/"));
    }

}