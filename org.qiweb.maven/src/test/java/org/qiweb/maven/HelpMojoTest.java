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

import java.io.File;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

/**
 * HelpMojo Test.
 */
public class HelpMojoTest
    extends AbstractMojoTestCase
{
    private final File basedir = new File( System.getProperty( "project.basedir", "" ) );

    public void testHelp()
        throws Exception
    {
        File pom = new File( basedir, "src/test/resources/org/qiweb/maven/unittest-pom.xml" );
        assertTrue( pom.exists() );

        HelpMojo helpMojo = (HelpMojo) lookupMojo( "help", pom );
        assertNotNull( helpMojo );
    }
}
