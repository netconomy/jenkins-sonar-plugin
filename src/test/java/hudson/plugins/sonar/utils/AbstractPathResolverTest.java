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
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class AbstractPathResolverTest {
    private static final String TEST_RETURN_ENTRY = "testEntry";
    private AbstractPathResolver pathResolver = new MockAbstractPathResolver(TEST_RETURN_ENTRY);

    @Test
    public void testResolvePath() throws IOException, InterruptedException {
        Set<String> result = this.pathResolver.resolvePath(new FilePath(new File("dummyFile")), "dummyPattern");
        assertThat(result, is(notNullValue()));
        assertThat(result.size(), is(1));
        assertThat(result.iterator().next(), is(TEST_RETURN_ENTRY));
    }

    @Test
    public void testResolvePathMissingFilePath() throws IOException, InterruptedException {
        Set<String> result = this.pathResolver.resolvePath(null, "dummyPattern");
        assertThat(result, is(Collections.<String>emptySet()));
    }

    @Test
    public void testResolvePathMissingPattern() throws IOException, InterruptedException {
        FilePath filePath = new FilePath(new File("dummyFile"));
        Set<String> result = this.pathResolver.resolvePath(filePath, "");
        assertThat(result, is(Collections.<String>emptySet()));
        result = this.pathResolver.resolvePath(filePath, null);
        assertThat(result, is(Collections.<String>emptySet()));
    }


}