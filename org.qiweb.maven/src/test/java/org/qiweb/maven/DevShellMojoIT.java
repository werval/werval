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
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.qiweb.runtime.util.Holder;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;
import static org.qiweb.api.BuildVersion.VERSION;

/**
 * DevShellMojo Integration Test.
 *
 * Generates a project, run it in dev mode using the plugin, change source code and assert code is reloaded.
 */
public class DevShellMojoIT
{
    private static final Charset UTF_8 = Charset.forName( "UTF-8" );
    private static final String POM;
    private static final String ROUTES;
    private static final String CONFIG;
    private static final String CONTROLLER;
    private static final String CONTROLLER_CHANGED;
    private static final File BASEDIR = new File( System.getProperty( "project.basedir", "" ) );

    static
    {
        POM
        = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
          + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
          + "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
          + "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n"
          + "    <modelVersion>4.0.0</modelVersion>\n"
          + "\n"
          + "    <groupId>org.qiweb.maven.test</groupId>\n"
          + "    <artifactId>org.qiweb.maven.test.unit</artifactId>\n"
          + "    <version>" + VERSION + "</version>\n"
          + "\n"
          + "    <properties>\n"
          + "        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n"
          + "        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>\n"
          + "    </properties>\n"
          + "\n"
          + "    <build>\n"
          + "        <plugins>\n"
          + "            <plugin>\n"
          + "                <groupId>org.qiweb</groupId>\n"
          + "                <artifactId>org.qiweb.maven</artifactId>\n"
          + "                <version>" + VERSION + "</version>\n"
          + "            </plugin>\n"
          + "            <plugin>\n"
          + "                <artifactId>maven-resources-plugin</artifactId>\n"
          + "                <version>2.6</version>\n"
          + "            </plugin>\n"
          + "            <plugin>\n"
          + "                <artifactId>maven-compiler-plugin</artifactId>\n"
          + "                <version>3.1</version>\n"
          + "            </plugin>\n"
          + "            <plugin>\n"
          + "                <artifactId>maven-surefire-plugin</artifactId>\n"
          + "                <version>2.17</version>\n"
          + "            </plugin>\n"
          + "            <plugin>\n"
          + "                <artifactId>maven-jar-plugin</artifactId>\n"
          + "                <version>2.4</version>\n"
          + "            </plugin>\n"
          + "        </plugins>\n"
          + "    </build>\n"
          + "    \n"
          + "    <dependencies>\n"
          + "        <dependency>\n"
          + "            <groupId>org.qiweb</groupId>\n"
          + "            <artifactId>org.qiweb.api</artifactId>\n"
          + "            <version>" + VERSION + "</version>\n"
          + "        </dependency>\n"
          + "        <dependency>\n"
          + "            <groupId>org.qiweb</groupId>\n"
          + "            <artifactId>org.qiweb.server.bootstrap</artifactId>\n"
          + "            <version>" + VERSION + "</version>\n"
          + "            <scope>runtime</scope>\n"
          + "        </dependency>\n"
          + "        <dependency>\n"
          + "            <groupId>org.slf4j</groupId>\n"
          + "            <artifactId>slf4j-simple</artifactId>\n"
          + "            <version>1.7.7</version>\n"
          + "            <scope>runtime</scope>\n"
          + "        </dependency>\n"
          + "    </dependencies>\n"
          + "    \n"
          + "</project>\n";
        CONFIG
        = "app {\n"
          + "    secret = e6bcdba3bc6840aa08013ef20505a0c27f800dbbcced6fbb71e8cf197fe83866\n"
          + "}\n";
        ROUTES = "GET / controllers.Application.index";
        CONTROLLER
        = "package controllers;\n"
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
        = "package controllers;\n"
          + "import org.qiweb.api.outcomes.Outcome;\n"
          + "import static org.qiweb.api.context.CurrentContext.outcomes;\n"
          + "public class Application\n"
          + "{\n"
          + "    public Outcome index()\n"
          + "    {\n"
          + "        return outcomes().ok( \"I ran changed!\" ).build();\n"
          + "    }\n"
          + "}\n";
        new File( BASEDIR, "target/it-tmp" ).mkdirs();
    }

    @ClassRule
    public static TemporaryFolder tmp = new TemporaryFolder( new File( BASEDIR, "target/it-tmp" ) )
    {
        @Override
        public void delete()
        {
            // NOOP
        }
    };

    private static File lock;

    @BeforeClass
    public static void setupProjectLayout()
        throws IOException
    {
        Files.write(
            new File( tmp.getRoot(), "pom.xml" ).toPath(),
            POM.getBytes( UTF_8 )
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
        lock = new File( tmp.getRoot(), ".devshell.lock" );
    }

    @AfterClass
    public static void forceDevShellStopIfAny()
        throws Exception
    {
        if( lock.exists() )
        {
            Files.delete( lock.toPath() );
        }
        else
        {
            System.err.println( "No devshell lock file at " + lock + ". Lookout for zombies!" );
        }
    }

    @Test
    public void devshell()
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
                    Invoker invoker = new DefaultInvoker();
                    invoker.setLocalRepositoryDirectory( new File( BASEDIR, "target/it-local-repository" ) );
                    invoker.setWorkingDirectory( tmp.getRoot() );

                    InvocationRequest request = new DefaultInvocationRequest();
                    request.setOffline( true );
                    request.setPomFile( new File( tmp.getRoot(), "pom.xml" ) );
                    request.setGoals( Arrays.asList( "qiweb:devshell" ) );

                    InvocationResult result = invoker.execute( request );

                    if( result.getExecutionException() != null )
                    {
                        devshellError.set( result.getExecutionException() );
                    }
                    else if( result.getExitCode() != 0 )
                    {
                        devshellError.set(
                            new RuntimeException( "Maven invocation failure, exit code was: " + result.getExitCode() )
                        );
                    }
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

            await().atMost( 30, SECONDS ).until( () -> lock.exists() );

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

            client.getConnectionManager().shutdown();
        }
        finally
        {
            devshellThread.interrupt();
        }
    }
}
