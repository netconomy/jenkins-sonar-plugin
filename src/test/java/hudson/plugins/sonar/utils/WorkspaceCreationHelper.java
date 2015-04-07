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

import java.io.File;
import java.io.IOException;

/**
 * @author spuchmann
 */
public final class WorkspaceCreationHelper {

    private WorkspaceCreationHelper(){}

    public static void createFolderStructure(File baseDir, int amountOfSubfolders) throws IOException {
        baseDir.mkdirs();
        createProjectFolders(baseDir, amountOfSubfolders);
    }

    private static void createProjectFolders(File baseDir, int amount) throws IOException {
        for (int i = 0; i < amount; i++) {
            File projectDir = new File(baseDir, "project_" + i);
            createProjectSubfolder(projectDir);
        }
    }

    private static void createProjectSubfolder(File baseDir) throws IOException {
        new File(baseDir, "src/main/java/hudson/plugin").mkdirs();
        new File(baseDir, "src/main/resources").mkdirs();
        new File(baseDir, "src/main/webapp").mkdirs();
        new File(baseDir, "src/test/java").mkdirs();
        new File(baseDir, "src/test/resources").mkdirs();
        new File(baseDir, "src/main/java/hudson/plugin/plugin.java").createNewFile();
        new File(baseDir, "src/main/java/hudson/plugin/plugin.txt").createNewFile();
        new File(baseDir, "src/main/java/hudson/plugin/plugin.jar").createNewFile();
    }
}
