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

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import hudson.AbortException;
import hudson.FilePath;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * Class which resolves wildcards for the given path pattern
 *
 * @author spuchmann
 * @since 2.2
 */
public class PathResolverOperator {

    public static final String SONAR_TEST = "sonar.tests";
    public static final String SONAR_SOURCES = "sonar.sources";
    public static final String SONAR_JAVA_BINARIES = "sonar.java.binaries";
    public static final String SONAR_JAVA_LIBRARIES = "sonar.java.libraries";
    public static final String SONAR_JAVA_BINARIES_INCLUDE = "sonar.java.binaries.include";
    public static final String SONAR_JAVA_BINARIES_EXCLUDE = "sonar.java.binaries.exclude";
    public static final String SONAR_JAVA_LIBRARIES_INCLUDE = "sonar.java.libraries.include";
    public static final String SONAR_JAVA_LIBRARIES_EXCLUDE = "sonar.java.libraries.exclude";

    private static final Set<String> PATH_PROPERTIES = Sets.newHashSet(SONAR_TEST, SONAR_SOURCES, SONAR_JAVA_BINARIES_EXCLUDE, SONAR_JAVA_BINARIES_INCLUDE, SONAR_JAVA_LIBRARIES_INCLUDE, SONAR_JAVA_LIBRARIES_EXCLUDE);

    private Map<String, PathResolver> resolverMap = createResolverMap();
    private Joiner joiner = Joiner.on(",");

    protected Map<String, PathResolver> createResolverMap() {
        Map<String, PathResolver> resolverMap = Maps.newHashMap();
        resolverMap.put(SONAR_JAVA_LIBRARIES_INCLUDE, new LibraryPathResolver());
        resolverMap.put(SONAR_JAVA_LIBRARIES_EXCLUDE, new LibraryPathResolver());
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

    public Properties resolvePaths(Properties prop) throws IOException, InterruptedException {
        Properties result = new Properties();
        Map<String, Set<String>> rawResults = Maps.newHashMap();
        for (Map.Entry<Object, Object> entry : prop.entrySet()) {
            String key = entry.getKey().toString();
            if (!PATH_PROPERTIES.contains(key)) {
                result.put(key, entry.getValue());
                continue;
            }

            String value = entry.getValue().toString();
            rawResults.put(key, doResolvePathSegments(key, value));
        }

        rawResults = mergeInternalProperties(rawResults);

        for (Map.Entry<String, Set<String>> rawEntry : rawResults.entrySet()) {
            result.put(rawEntry.getKey(), joiner.join(rawEntry.getValue()));
        }
        return result;
    }

    public String resolvePath(String key, String value) throws IOException, InterruptedException {
        Set<String> result = doResolvePathSegments(key, value);
        return joiner.join(result);
    }

    protected Set<String> doResolvePathSegments(String key, String value) throws IOException, InterruptedException {
        if (!PATH_PROPERTIES.contains(key)) {
            return Sets.newHashSet(value);
        }

        String trimmedValue = StringUtils.trim(value);
        if (StringUtils.isEmpty(trimmedValue)) {
            return Sets.newHashSet(value);
        }

        Iterable<String> pathIterable = Splitter.on(",").trimResults().omitEmptyStrings().split(trimmedValue);
        Set<String> result = Sets.newHashSet();
        for (String path : pathIterable) {
            result.addAll(doResolvePath(key, path));
        }
        return result;
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

    protected Map<String, Set<String>> mergeInternalProperties(Map<String, Set<String>> rawResults) {
        Map<String, Set<String>> resultMap;
        resultMap = mergeInternalProperty(rawResults, SONAR_JAVA_BINARIES, SONAR_JAVA_BINARIES_INCLUDE, SONAR_JAVA_BINARIES_EXCLUDE);
        resultMap = mergeInternalProperty(resultMap, SONAR_JAVA_LIBRARIES, SONAR_JAVA_LIBRARIES_INCLUDE, SONAR_JAVA_LIBRARIES_EXCLUDE);
        return resultMap;
    }

    protected Map<String, Set<String>> mergeInternalProperty(Map<String, Set<String>> rawResults, String mergedKey, String includeKey, String excludeKey) {
        Map<String, Set<String>> resultMap = Maps.newHashMap(rawResults);
        Set<String> mergedSet = retrieveSet(rawResults, mergedKey);
        mergedSet.addAll(retrieveSet(rawResults, includeKey));
        mergedSet.removeAll(retrieveSet(rawResults, excludeKey));

        resultMap.put(mergedKey, mergedSet);
        resultMap.remove(includeKey);
        resultMap.remove(excludeKey);
        return resultMap;
    }

    protected Set<String> retrieveSet(Map<String, Set<String>> map, String key) {
        Set<String> retrievedSet = map.get(key);
        return retrievedSet != null ? retrievedSet : new HashSet<String>();
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
