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
package org.qiweb.gradle;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.gradle.internal.classloader.ClasspathUtil;
import org.gradle.testkit.functional.ExecutionResult;
import org.gradle.testkit.functional.GradleRunner;
import org.gradle.testkit.functional.GradleRunnerFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.qiweb.runtime.util.Holder;

import static java.util.Collections.singletonList;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;

/**
 * Assert that the {@literal secret} and {@literal devshell} tasks execute successfuly.
 *
 * Generates a project, run it in dev mode using the plugin, change source code and assert code is reloaded.
 */
public class QiWebPluginIntegTest
{
    private static final Charset UTF_8 = Charset.forName( "UTF-8" );
    private static final String BUILD;
    private static final String ROUTES;
    private static final String CONFIG;
    private static final String CONTROLLER;
    private static final String CONTROLLER_CHANGED;

    static
    {
        List<URL> classpathUrls = ClasspathUtil.getClasspath( QiWebPluginIntegTest.class.getClassLoader() );
        StringBuilder builder = new StringBuilder( "files(" );
        Iterator<URL> it = classpathUrls.iterator();
        while( it.hasNext() )
        {
            builder.append( " '" ).append( it.next() ).append( "'" );
            if( it.hasNext() )
            {
                builder.append( "," );
            }
        }
        builder.append( " )" );
        String classpath = builder.toString();
        BUILD
        = "\n"
          + "buildscript {\n"
          + "  dependencies {\n"
          + "    classpath " + classpath + "\n"
          + "  }\n"
          + "}\n"
          + "apply plugin: \"java\"\n"
          + "apply plugin: \"qiweb\"\n"
          + "dependencies {\n"
          + "  compile " + classpath + "\n"
          + "}\n"
          + "\n";
        CONFIG
        = "\n"
          + "app: {\n"
          + "    secret: e6bcdba3bc6840aa08013ef20505a0c27f800dbbcced6fbb71e8cf197fe83866\n"
          + "}\n";
        ROUTES
        = "\n"
          + "GET / controllers.Application.index";
        CONTROLLER
        = "\n"
          + "package controllers;\n"
          + "import org.qiweb.api.outcomes.Outcome;\n"
          + "import static org.qiweb.api.context.CurrentContext.outcomes;\n"
          + "public class Application\n"
          + "{\n"
          + "    public Outcome index()\n"
          + "    {\n"
          + "        return outcomes().ok( \"I ran!\" ).build();\n"
          + "    }\n"
          + "}\n";
        CONTROLLER_CHANGED
        = "\n"
          + "package controllers;\n"
          + "import org.qiweb.api.outcomes.Outcome;\n"
          + "import static org.qiweb.api.context.CurrentContext.outcomes;\n"
          + "public class Application\n"
          + "{\n"
          + "    public Outcome index()\n"
          + "    {\n"
          + "        return outcomes().ok( \"I ran changed!\" ).build();\n"
          + "    }\n"
          + "}\n";
    }

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Before
    public void setupProjectLayout()
        throws IOException
    {
        Files.write(
            new File( tmp.getRoot(), "build.gradle" ).toPath(),
            BUILD.getBytes( UTF_8 )
        );
        File resources = new File( tmp.getRoot(), "src/main/resources" );
        Files.createDirectories( resources.toPath() );
        Files.write(
            new File( resources, "application.conf" ).toPath(),
            CONFIG.getBytes( UTF_8 )
        );
        Files.write(
            new File( resources, "routes.conf" ).toPath(),
            ROUTES.getBytes( UTF_8 )
        );
        File controllers = new File( tmp.getRoot(), "src/main/java/controllers" );
        Files.createDirectories( controllers.toPath() );
        Files.write(
            new File( controllers, "Application.java" ).toPath(),
            CONTROLLER.getBytes( UTF_8 )
        );
    }

    @Test
    public void secretTask()
        throws IOException
    {
        GradleRunner runner = GradleRunnerFactory.create();
        runner.setDirectory( tmp.getRoot() );
        runner.setArguments( singletonList( "secret" ) );

        ExecutionResult result = runner.run();
        assertThat( result.getStandardOutput(), containsString( "Generate new QiWeb Application Secret" ) );
    }

    @Test
    public void devshellTask()
        throws InterruptedException, IOException
    {
        final Holder<Throwable> devshellError = new Holder<>();
        Runnable devshellRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    GradleRunner runner = GradleRunnerFactory.create();
                    runner.setDirectory( tmp.getRoot() );
                    runner.setArguments( singletonList( "devshell" ) );
                    runner.run();
                }
                catch( Exception ex )
                {
                    devshellError.set( ex );
                }
            }
        };
        Thread devshellThread = new Thread( devshellRunnable, "devshell-thread" );
        try
        {
            devshellThread.start();

            // TODO Find a way to run ASAP, devshell run lock file?
            Thread.sleep( 30 * 1000 );

            if( devshellError.isSet() )
            {
                throw new RuntimeException(
                    "Error during deshell invocation: " + devshellError.get().getMessage(),
                    devshellError.get()
                );
            }

            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet( "http://localhost:23023/" );
            ResponseHandler<String> handler = new BasicResponseHandler();

            assertThat(
                client.execute( get, handler ),
                containsString( "I ran!" )
            );

            Files.write(
                new File( tmp.getRoot(), "src/main/java/controllers/Application.java" ).toPath(),
                CONTROLLER_CHANGED.getBytes( UTF_8 )
            );

            // Wait for source code change to be detected
            Thread.sleep( 2000 );

            assertThat(
                client.execute( get, handler ),
                containsString( "I ran changed!" )
            );
        }
        finally
        {
            devshellThread.interrupt();
        }
    }
}
