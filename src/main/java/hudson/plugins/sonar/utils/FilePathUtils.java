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

import hudson.AbortException;
import hudson.FilePath;
import hudson.Util;
import hudson.remoting.VirtualChannel;
import jenkins.SlaveToMasterFileCallable;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * This class exists because of modifier restriction in jenkins
 *
 * @author spuchmann
 * @since 2.2
 */
public class FilePathUtils {

    //Taken from FilePath (unfortunately there is no access to this code part)
    private static final Pattern DRIVE_PATTERN = Pattern.compile("[A-Za-z]:[\\\\/].*"),
            UNC_PATTERN = Pattern.compile("^\\\\\\\\.*");


    public static FilePath[] listFolder(FilePath base, final String includePattern) throws IOException, InterruptedException {
        if (base == null || !base.exists()) {
            throw new AbortException("Unable to find base dir");
        }
        return base.act(new SlaveToMasterFileCallable<FilePath[]>() {
            private static final long serialVersionUID = 1L;

            public FilePath[] invoke(File f, VirtualChannel channel) throws IOException {
                String[] files = antDirSearch(f, includePattern);

                FilePath[] r = new FilePath[files.length];
                for (int i = 0; i < r.length; i++)
                    r[i] = new FilePath(new File(f, files[i]));

                return r;
            }
        });
    }

    /**
     * taken from FilePath#glob
     *
     * @param dir
     * @param includes
     * @return
     * @throws IOException
     */
    private static String[] antDirSearch(File dir, String includes) throws IOException {
        if(isAbsolute(includes))
            throw new IOException("Expecting Ant GLOB pattern, but saw '"+includes+"'. See http://ant.apache.org/manual/Types/fileset.html for syntax");
        FileSet fs = Util.createFileSet(dir, includes, null);
        fs.setDefaultexcludes(true);
        DirectoryScanner ds = fs.getDirectoryScanner(new Project());
        return ds.getIncludedDirectories();
    }

    /**
     * Taken from FilePath#isAbsolute(String)
     *
     * @param rel
     * @return
     */
    private static boolean isAbsolute(String rel) {
        return rel.startsWith("/") || DRIVE_PATTERN.matcher(rel).matches() || UNC_PATTERN.matcher(rel).matches();
    }

}
