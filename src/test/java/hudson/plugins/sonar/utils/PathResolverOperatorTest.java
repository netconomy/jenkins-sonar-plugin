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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import hudson.FilePath;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static hudson.plugins.sonar.utils.PathResolverOperator.*;
import static hudson.plugins.sonar.utils.PathResolverOperator.SONAR_JAVA_BINARIES_EXCLUDE;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;

public class PathResolverOperatorTest {

    private static final String DUMMY_RESOLVER_VALUE = "dummyResolverValue";
    private static final String DEFAULT_RESOLVER_VALUE = "defaultResolverValue";

    private PathResolverOperator resolverOperator;

    private File testBaseDir = new File("target/testDir");

    @Before
    public void before() throws InterruptedException, IOException {
        WorkspaceCreationHelper.createFolderStructure(testBaseDir, 1);
        this.resolverOperator = new PathResolverOperator(new FilePath(testBaseDir));
        this.resolverOperator.setDefaultPathResolver(new MockAbstractPathResolver(DEFAULT_RESOLVER_VALUE));
        Map<String, PathResolver> resolverMap = Maps.newHashMap();
        resolverMap.put(PathResolverOperator.SONAR_TEST, new MockAbstractPathResolver(DUMMY_RESOLVER_VALUE));
        this.resolverOperator.setResolverMap(resolverMap);
    }

    @After
    public void after() throws IOException {
        FileUtils.deleteDirectory(testBaseDir);
    }


    @Test
    public void testResolvePathWithInvalidKey() throws IOException, InterruptedException {
        String value = "value";
        String result = resolverOperator.resolvePath("someKey", value);
        assertEquals(value, result);
    }

    @Test
    public void testResolvePathWithEmptyString() throws IOException, InterruptedException {
        String input = " ";
        String result = resolverOperator.resolvePath(PathResolverOperator.SONAR_SOURCES, input);
        assertEquals(input, result);
    }

    @Test
    public void testResolvePath() throws IOException, InterruptedException {
        String resolvedPath = resolverOperator.resolvePath(PathResolverOperator.SONAR_SOURCES, "****");

        assertNotNull(resolvedPath);
        String[] resultArray = resolvedPath.split(",");
        assertEquals(1, resultArray.length);
        assertEquals(DEFAULT_RESOLVER_VALUE, resultArray[0]);
    }

    private void testSimpleResult(String expectedValue, Set<String> result) {
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedValue, result.iterator().next());
    }

    @Test
    public void testDoResolvePathMappedResolver() throws IOException, InterruptedException {
        Set<String> result = resolverOperator.doResolvePath(PathResolverOperator.SONAR_TEST, "**//*main/java");
        testSimpleResult(DUMMY_RESOLVER_VALUE, result);
    }

    @Test
    public void testDoResolvePathDefaultResolver() throws IOException, InterruptedException {
        Set<String> result = resolverOperator.doResolvePath(PathResolverOperator.SONAR_SOURCES, "**//*main/java");
        testSimpleResult(DEFAULT_RESOLVER_VALUE, result);
    }

    @Test
    public void testDoResolvePathNoResolver() throws IOException, InterruptedException {
        resolverOperator.setDefaultPathResolver(null);
        Set<String> result = resolverOperator.doResolvePath("dummyKey", "dummyValue");
        assertEquals(Collections.emptySet(), result);
    }

    @Test
    public void testRetrieveSet() {
        Map<String, Set<String>> sampleData = Maps.newHashMap();
        Set<String> set1 = Sets.newHashSet("value1");
        sampleData.put("keyWithValues", set1);

        assertThat(resolverOperator.retrieveSet(sampleData, "keyWithValues"), is(set1));
        assertThat(resolverOperator.retrieveSet(sampleData, "someValues"), notNullValue());
    }

    @Test
    public void testMergeInternalProperty() {
        final String mergedSetKey = "mergedSet";
        Map<String, Set<String>> sampleData = Maps.newHashMap();
        sampleData.put("includeSet", Sets.newHashSet("value1", "value2"));
        sampleData.put("excludeSet", Sets.newHashSet("value2", "value3"));

        sampleData.put(mergedSetKey, Sets.newHashSet("value3", "value4"));

        Map<String, Set<String>> result = resolverOperator.mergeInternalProperty(sampleData, mergedSetKey, "includeSet", "excludeSet");
        assertNotNull(result);
        assertEquals(1, result.size());
        Set<String> resultSet = result.get(mergedSetKey);
        assertNotNull(resultSet);

        assertEquals(2, resultSet.size());
        assertTrue(resultSet.containsAll(Sets.newHashSet("value1", "value4")));
    }

    @Test
    public void testMergeInternalProperties() {
        Map<String, Set<String>> sampleData = Maps.newHashMap();
        sampleData.put(SONAR_JAVA_BINARIES_INCLUDE, null);
        sampleData.put(SONAR_JAVA_LIBRARIES_EXCLUDE, null);
        sampleData.put(SONAR_JAVA_LIBRARIES_INCLUDE, new HashSet<String>());

        Map<String, Set<String>> result = resolverOperator.mergeInternalProperties(sampleData);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertNotNull(result.get(SONAR_JAVA_BINARIES));
        assertNotNull(result.get(SONAR_JAVA_LIBRARIES));
    }

    @Test
    public void testResolvePaths() throws IOException, InterruptedException {
        Properties input = new Properties();
        input.put("untouchedKey", "untouchedValue");
        input.put("anotherKey", "anotherValue");
        input.put(SONAR_JAVA_BINARIES_INCLUDE, "lib1,lib2");
        input.put(SONAR_JAVA_BINARIES_EXCLUDE, "lib2");

        Map<String, PathResolver> resolverMap = Maps.newHashMap();
        resolverMap.put(SONAR_JAVA_BINARIES_INCLUDE, new MockAbstractPathResolver("lib1", "lib2"));
        resolverMap.put(SONAR_JAVA_BINARIES_EXCLUDE, new MockAbstractPathResolver("lib2"));
        this.resolverOperator.setResolverMap(resolverMap);

        Properties result = this.resolverOperator.resolvePaths(input);

        assertEquals(4, result.size());
        assertEquals("untouchedValue", result.getProperty("untouchedKey"));
        assertEquals("anotherValue", result.getProperty("anotherKey"));
        assertEquals("lib1", result.getProperty(SONAR_JAVA_BINARIES));
        assertEquals("", result.getProperty(SONAR_JAVA_LIBRARIES));
    }

}