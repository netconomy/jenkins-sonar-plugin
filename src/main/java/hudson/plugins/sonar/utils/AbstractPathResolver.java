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
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

/**
 * PathResolver which checks input before delegating calls
 *
 * @author spuchmann
 * @since 2.2
 */
public abstract class AbstractPathResolver implements PathResolver {

    public Set<String> resolvePath(FilePath workspace, String pathPattern) throws IOException, InterruptedException {
        if (workspace == null || StringUtils.isBlank(pathPattern)) {
            return Collections.emptySet();
        }

        return doResolvePath(workspace, pathPattern);
    }

    protected abstract Set<String> doResolvePath(FilePath workspace, String pathPattern) throws IOException, InterruptedException;
}
