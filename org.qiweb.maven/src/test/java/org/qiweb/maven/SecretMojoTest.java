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
package org.qiweb.maven;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;

/**
 * SecretMojo Test.
 */
public class SecretMojoTest
    extends AbstractMojoTestCase
{
    public void testSecret()
        throws Exception
    {
        File pom = new File( "src/test/resources/org/qiweb/maven/unittest-pom.xml" );
        assertTrue( pom.exists() );

        SecretMojo secretMojo = (SecretMojo) lookupMojo( "secret", pom );
        assertNotNull( secretMojo );

        PrintStream oldOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream( baos );
        System.setOut( out );

        secretMojo.execute();

        System.setOut( oldOut );
        out.flush();
        String output = new String( baos.toByteArray() );
        assertThat( output, containsString( "New Application Secret" ) );
    }
}
