/*
 * Copyright (c) 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.werval.gradle;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import org.gradle.testkit.functional.ExecutionResult;
import org.gradle.testkit.functional.GradleRunner;
import org.gradle.testkit.functional.GradleRunnerFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static io.werval.api.BuildVersion.VERSION;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ModulePluginIntegTest
    extends AbstractPluginIntegTest
{
    private static final Charset UTF_8 = Charset.forName( "UTF-8" );
    private static final String BUILD;
    private static final String DOC;

    static
    {
        BUILD
        = "\n"
          + "buildscript {\n"
          + "  repositories {\n"
          + "    maven { url wervalLocalRepository }\n"
          + "    maven { url 'https://repo.codeartisans.org/werval' }\n"
          + "    jcenter()\n"
          + "  }\n"
          + "  dependencies { classpath 'io.werval:io.werval.gradle:" + VERSION + "' }\n"
          + "}\n"
          + "apply plugin: \"io.werval.module\"\n"
          + "moduleDescriptor.plugin( 'foo', impl: 'com.acme.bar.FooPlugin' )\n"
          + "moduleDocumentation.skip = false\n"
          + "\n";
        DOC
        = "\n"
          + "= UnderTest Module\n"
          + "Some module for integration test\n"
          + ":jbake-type: module\n\n"
          + "The UnderTest module provide nothing.\n"
          + "\n";
        new File( "build/tmp/it" ).mkdirs();
    }

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder( new File( "build/tmp/it" ) )
    {
        @Override
        public void delete()
        {
            // super.delete();
        }
    };

    @Before
    public void setupProjectLayout()
        throws IOException
    {
        Files.write(
            new File( tmp.getRoot(), "build.gradle" ).toPath(),
            BUILD.getBytes( UTF_8 )
        );
        File docRoot = new File( tmp.getRoot(), "src/doc" );
        Files.createDirectories( docRoot.toPath() );
        Files.write(
            new File( docRoot, "index.adoc" ).toPath(),
            DOC.getBytes( UTF_8 )
        );
        Files.write(
            new File( docRoot, "some-resource.jpg" ).toPath(),
            "Some resource data".getBytes( UTF_8 )
        );
    }

    @Test
    public void moduleDescriptorAndDocumentation()
        throws IOException
    {
        GradleRunner runner = GradleRunnerFactory.create();
        runner.setDirectory( tmp.getRoot() );
        runner.setArguments( singletonList( "assemble" ) );

        ExecutionResult result = runner.run();

        assertThat( result.getStandardOutput(), containsString( "moduleDescriptor" ) );
        assertTrue(
            new File(
                tmp.getRoot(),
                "build/generated-src/werval-dyndesc/resources/META-INF/werval-plugins.properties"
            ).exists()
        );

        assertThat( result.getStandardOutput(), containsString( "moduleDocumentation_asciidoctor" ) );
        assertTrue(
            new File(
                tmp.getRoot(),
                "build/generated-src/werval-doc/resources/" + tmp.getRoot().getName() + "/doc/index.html"
            ).exists()
        );

        assertThat( result.getStandardOutput(), containsString( "moduleDocumentation_dyndocs" ) );
        assertTrue( new File( tmp.getRoot(), "build/generated-src/werval-dyndocs/resources/reference.conf" ).exists() );

        assertThat( result.getStandardOutput(), containsString( "moduleDocumentation_doczip" ) );
        assertThat( new File( tmp.getRoot(), "build/distributions" ).listFiles().length, is( 1 ) );
    }
}
