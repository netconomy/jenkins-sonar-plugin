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

import com.google.common.collect.Sets;
import hudson.FilePath;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.Set;

/**
 * @author spuchmann
 * @since 2.2
 */
public class LibraryPathResolver extends AbstractPathResolver {

    private static final String LIBRARY_EXTENSION = ".jar";
    private static final String LIBRARY_PATTERN = "*" + LIBRARY_EXTENSION;

    @Override
    protected Set<String> doResolvePath(FilePath workspace, String pathPattern) throws IOException, InterruptedException {
        Set<String> resolvedPathSet = Sets.newHashSet();

        String pattern = checkAndFixPattern(pathPattern);
        FilePath[] resolvedPaths = workspace.list(pattern);
        for (FilePath resolvedPath : resolvedPaths) {
            resolvedPathSet.add(resolvedPath.getRemote());
        }

        return resolvedPathSet;
    }

    protected String checkAndFixPattern(String pattern) {
        if (pattern.endsWith(LIBRARY_PATTERN)) {
            return pattern;
        }

        String clearedPattern = pattern;

        if (StringUtils.endsWithIgnoreCase(clearedPattern, LIBRARY_EXTENSION)) {
            clearedPattern = StringUtils.removeEndIgnoreCase(clearedPattern, LIBRARY_EXTENSION);
        }

        return clearedPattern + LIBRARY_PATTERN;
    }
}
