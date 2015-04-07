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

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import hudson.AbortException;
import hudson.FilePath;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Class which resolves wildcards for the given path pattern
 *
 * @author spuchmann
 * @since 2.2
 */
public class PathResolverOperator {

    public static final String SONAR_TEST = "sonar.tests";
    public static final String SONAR_BINARIES = "sonar.binaries";
    public static final String SONAR_SOURCES = "sonar.sources";
    public static final String SONAR_LIBRARIES = "sonar.libraries";

    private static final Set<String> PATH_PROPERTIES = Sets.newHashSet(SONAR_TEST, SONAR_BINARIES, SONAR_SOURCES, SONAR_LIBRARIES);

    private Map<String, PathResolver> resolverMap = createResolverMap();

    protected Map<String, PathResolver> createResolverMap() {
        Map<String, PathResolver> resolverMap = Maps.newHashMap();
        resolverMap.put(SONAR_LIBRARIES, new LibraryPathResolver());
        return resolverMap;
    }

    private PathResolver defaultPathResolver = new FolderPathResolver();

    private FilePath workspace;


    public PathResolverOperator() {
    }

    public PathResolverOperator(FilePath workspace) throws IOException, InterruptedException, AbortException {
        if (workspace == null || !workspace.exists()) {
            throw new AbortException("Unable to initiate path resolving because of non existing workspace");
        }
        this.workspace = workspace;
    }

    public String resolvePath(String key, String value) throws IOException, InterruptedException {
        if (!PATH_PROPERTIES.contains(key)) {
            return value;
        }

        String trimmedValue = StringUtils.trim(value);
        if (StringUtils.isEmpty(trimmedValue)) {
            return value;
        }

        Iterable<String> pathIterable = Splitter.on(",").trimResults().omitEmptyStrings().split(trimmedValue);
        Set<String> result = Sets.newHashSet();
        for (String path : pathIterable) {
            result.addAll(doResolvePath(key, path));
        }

        return Joiner.on(",").join(result);
    }

    protected Set<String> doResolvePath(String key, String path) throws IOException, InterruptedException {
        if (MapUtils.isNotEmpty(resolverMap) && resolverMap.containsKey(key)) {
            return resolverMap.get(key).resolvePath(workspace, path);
        }

        if (defaultPathResolver != null) {
            return this.defaultPathResolver.resolvePath(this.workspace, path);
        }

        return Collections.emptySet();
    }

    public FilePath getWorkspace() {
        return workspace;
    }

    public void setWorkspace(FilePath workspace) {
        this.workspace = workspace;
    }

    public Map<String, PathResolver> getResolverMap() {
        return resolverMap;
    }

    public void setResolverMap(Map<String, PathResolver> resolverMap) {
        this.resolverMap = resolverMap;
    }

    public PathResolver getDefaultPathResolver() {
        return defaultPathResolver;
    }

    public void setDefaultPathResolver(PathResolver defaultPathResolver) {
        this.defaultPathResolver = defaultPathResolver;
    }
}
