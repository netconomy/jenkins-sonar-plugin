/*
 * Jenkins Plugin for SonarQube, open source software quality management tool.
 * mailto:contact AT sonarsource DOT com
 *
 * Jenkins Plugin for SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Jenkins Plugin for SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FolderPathResolverTest {

    private static final int AMOUNT_OF_PROJECTS = 3;
    private File testBaseDir = new File("target/testDir");
    private FilePath workspace = new FilePath(testBaseDir);
    private FolderPathResolver pathResolver = new FolderPathResolver();

    @Before
    public void before() throws IOException {
        WorkspaceCreationHelper.createFolderStructure(testBaseDir, AMOUNT_OF_PROJECTS);
        new File(testBaseDir, "project_1/src/main/java/hudson/plugin/singleFolder").mkdirs();
    }

    @After
    public void after() throws IOException {
        FileUtils.deleteDirectory(testBaseDir);
    }

    @Test
    public void testDoResolvePath() throws IOException, InterruptedException {
        Set<String> stringSet = pathResolver.doResolvePath(workspace, "**/singleFolder");
        assertEquals(1, stringSet.size());
    }

    @Test
    public void testDoResolvePathMultipleResults() throws IOException, InterruptedException {
        Set<String> stringSet = pathResolver.doResolvePath(workspace, "**/main/java");
        assertEquals(AMOUNT_OF_PROJECTS, stringSet.size());
        for (String path : stringSet) {
            assertTrue("Not right ending: " + path, path.endsWith("main" + File.separator + "java"));
        }
    }
}