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
import hudson.FilePath;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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


}