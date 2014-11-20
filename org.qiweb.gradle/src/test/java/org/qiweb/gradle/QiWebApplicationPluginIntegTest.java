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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.gradle.testkit.functional.ExecutionResult;
import org.gradle.testkit.functional.GradleRunner;
import org.gradle.testkit.functional.GradleRunnerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.qiweb.runtime.util.Holder;
import org.qiweb.test.util.Processes;
import org.qiweb.util.InputStreams;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;
import static org.qiweb.api.BuildVersion.VERSION;
import static org.qiweb.util.InputStreams.BUF_SIZE_4K;

/**
 * Assert that the {@literal secret}, {@literal start} and {@literal devshell} tasks execute successfuly.
 *
 * Generates a project, run it in dev mode using the plugin, change source code and assert code is reloaded.
 * <p>
 * As this test spawn several Gradle Daemons it ends by killing them all to leave the running system in a proper state.
 */
public class QiWebApplicationPluginIntegTest
  extends AbstractQiWebIntegTest
{
    private static final Charset UTF_8 = Charset.forName( "UTF-8" );
    private static final String BUILD;
    private static final String ROUTES;
    private static final String CONFIG;
    private static final String CONFIG_DEV;
    private static final String CONTROLLER;
    private static final String CONTROLLER_CHANGED;
    private static final String CONTROLLER_EXCEPTIONAL;
    private static final String CONTROLLER_BUILD_ERROR;

    static
    {
        BUILD
        = "\n"
          + "buildscript {\n"
          + "  repositories {\n"
          + "    maven { url qiwebLocalRepository }\n"
          + "    maven { url 'https://repo.codeartisans.org/qiweb' }\n"
          + "  }\n"
          + "  dependencies { classpath 'org.qiweb:org.qiweb.gradle:" + VERSION + "' }\n"
          + "}\n"
          + "apply plugin: \"org.qiweb.application\"\n"
          + "dependencies {\n"
          + "  runtime 'ch.qos.logback:logback-classic:1.1.2'\n"
          + "}\n"
          + "sourceSets {\n"
          + "  custom\n"
          + "}\n"
          + "classes.dependsOn customClasses\n"
          + "devshell {\n"
          + "  sourceSets += project.sourceSets.custom\n"
          + "  configResource = 'development.conf'\n"
          + "}\n"
          + "start {\n"
          + "  sourceSets += project.sourceSets.custom\n"
          + "  configResource = 'application-custom.conf'\n"
          + "}\n"
          + "\n";
        CONFIG
        = "\n"
          + "app.secret = e6bcdba3bc6840aa08013ef20505a0c27f800dbbcced6fbb71e8cf197fe83866\n"
          + "tag = custom\n";
        CONFIG_DEV = "include \"application-custom.conf\"\ntag = development\n";
        ROUTES = "GET / controllers.Application.index";
        CONTROLLER
        = "\n"
          + "package controllers;\n"
          + "import org.qiweb.api.outcomes.Outcome;\n"
          + "import static org.qiweb.api.context.CurrentContext.application;\n"
          + "import static org.qiweb.api.context.CurrentContext.outcomes;\n"
          + "public class Application\n"
          + "{\n"
          + "    public Outcome index()\n"
          + "    {\n"
          + "        return outcomes().ok( \"I ran! \" + application().config().string( \"tag\" ) ).build();\n"
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
        CONTROLLER_EXCEPTIONAL
        = "\n"
          + "package controllers;\n"
          + "import org.qiweb.api.outcomes.Outcome;\n"
          + "import static org.qiweb.api.context.CurrentContext.outcomes;\n"
          + "public class Application\n"
          + "{\n"
          + "    public Outcome index()\n"
          + "    {\n"
          + "        throw new RuntimeException( \"I throwed!\" );\n"
          + "    }\n"
          + "}\n";
        CONTROLLER_BUILD_ERROR
        = "\n"
          + "package controllers;\n"
          + "import org.qiweb.api.outcomes.Outcome;\n"
          + "import static org.qiweb.api.context.CurrentContext.outcomes;\n"
          + "public class Application\n"
          + "{\n"
          + "    public Outcome index()\n"
          + "    {\n"
          + "        I FAILED TO COMPILE!\n"
          + "    }\n"
          + "}\n";
        new File( "build/tmp/it" ).mkdirs();
    }

    private final File devshellLock = new File( ".devshell.lock" );

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder( new File( "build/tmp/it" ) )
    {
        @Override
        public void delete()
        {
          super.delete();
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
        File dev = new File( tmp.getRoot(), "src/dev/resources" );
        Files.createDirectories( dev.toPath() );
        Files.write(
            new File( dev, "development.conf" ).toPath(),
            CONFIG_DEV.getBytes( UTF_8 )
        );
        File custom = new File( tmp.getRoot(), "src/custom/resources" );
        Files.createDirectories( custom.toPath() );
        Files.write(
            new File( custom, "application-custom.conf" ).toPath(),
            CONFIG.getBytes( UTF_8 )
        );
        File resources = new File( tmp.getRoot(), "src/main/resources" );
        Files.createDirectories( resources.toPath() );
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

    @After
    public void cleanupDevShellLock()
        throws Exception
    {
          if( devshellLock.exists() )
          {
              Files.delete( devshellLock.toPath() );
          }
    }

    @Test
    public void secretTaskIntegrationTest()
        throws IOException
    {
        GradleRunner runner = GradleRunnerFactory.create();
        runner.setDirectory( tmp.getRoot() );
        runner.setArguments( singletonList( "secret" ) );

        ExecutionResult result = runner.run();
        assertThat( result.getStandardOutput(), containsString( "Generate new QiWeb Application Secret" ) );
    }

    @Test
    public void devshellTaskIntegrationTest()
        throws InterruptedException, IOException
    {
        final Holder<Exception> errorHolder = new Holder<>();
        Thread devshellThread = new Thread( newRunnable( errorHolder, "devshell" ), "gradle-qiweb-devshell-thread" );
        try
        {
            devshellThread.start();

            await().atMost( 60, SECONDS ).until(
                new Callable<Boolean>()
                {
                    @Override
                    public Boolean call()
                    throws Exception
                    {
                        return devshellLock.exists();
                    }
                }
            );

            if( errorHolder.isSet() )
            {
                throw new RuntimeException(
                    "Error during qiweb:devshell invocation: " + errorHolder.get().getMessage(),
                    errorHolder.get()
                );
            }

            final HttpClient client = new DefaultHttpClient();
            final HttpGet get = new HttpGet( "http://localhost:23023/" );
            final ResponseHandler<String> successHandler = new BasicResponseHandler();

            await().atMost( 60, SECONDS ).pollInterval( 5, SECONDS ).until(
                new Callable<String>()
                {
                    @Override
                    public String call()
                    throws Exception
                    {
                        try
                        {
                            return client.execute( get, successHandler );
                        }
                        catch( Exception ex )
                        {
                            return null;
                        }
                    }
                },
                allOf( containsString( "I ran!" ), containsString( "development" ) )
            );

            // Source code change
            Files.write(
                new File( tmp.getRoot(), "src/main/java/controllers/Application.java" ).toPath(),
                CONTROLLER_CHANGED.getBytes( UTF_8 )
            );
            Thread.sleep( 2000 ); // Wait for source code change to be detected
            assertThat(
                client.execute( get, successHandler ),
                containsString( "I ran changed!" )
            );

            // Exception
            Files.write(
                new File( tmp.getRoot(), "src/main/java/controllers/Application.java" ).toPath(),
                CONTROLLER_EXCEPTIONAL.getBytes( UTF_8 )
            );
            Thread.sleep( 2000 ); // Wait for source code change to be detected
            HttpResponse response = client.execute( get );
            int code = response.getStatusLine().getStatusCode();
            String body = InputStreams.readAllAsString( response.getEntity().getContent(), BUF_SIZE_4K, UTF_8 );
            assertThat( code, is( 500 ) );
            assertThat( body, containsString( "I throwed!" ) );

            // Build error
            Files.write(
                new File( tmp.getRoot(), "src/main/java/controllers/Application.java" ).toPath(),
                CONTROLLER_BUILD_ERROR.getBytes( UTF_8 )
            );
            Thread.sleep( 2000 ); // Wait for source code change to be detected
            response = client.execute( get );
            code = response.getStatusLine().getStatusCode();
            body = InputStreams.readAllAsString( response.getEntity().getContent(), BUF_SIZE_4K, UTF_8 );
            assertThat( code, is( 500 ) );
            assertThat( body, containsString( "I FAILED TO COMPILE!" ) );

            // Back to normal
            Files.write(
                new File( tmp.getRoot(), "src/main/java/controllers/Application.java" ).toPath(),
                CONTROLLER.getBytes( UTF_8 )
            );
            Thread.sleep( 2000 ); // Wait for source code change to be detected
            assertThat(
                client.execute( get, successHandler ),
                containsString( "I ran!" )
            );

            // QiWeb Documentation
            assertThat(
                client.execute( new HttpGet( "http://localhost:23023/@doc" ), successHandler ),
                containsString( "QiWeb Documentation" )
            );

            client.getConnectionManager().shutdown();
        }
        finally
        {
            devshellThread.interrupt();
        }
    }

    @Test
    public void startTaskIntegrationTest()
        throws InterruptedException, IOException
    {
        final Holder<Exception> errorHolder = new Holder<>();
        Thread runThread = new Thread( newRunnable( errorHolder, "start" ), "gradle-qiweb-start-thread" );
        try
        {
            runThread.start();

            final HttpClient client = new DefaultHttpClient();
            final HttpGet get = new HttpGet( "http://localhost:23023/" );
            final ResponseHandler<String> handler = new BasicResponseHandler();

            await().atMost( 60, SECONDS ).pollInterval( 5, SECONDS ).until(
                new Callable<String>()
                {
                    @Override
                    public String call()
                    throws Exception
                    {
                        try
                        {
                            return client.execute( get, handler );
                        }
                        catch( Exception ex )
                        {
                            return null;
                        }
                    }
                },
                allOf( containsString( "I ran!" ), containsString( "custom" ) )
            );

            client.getConnectionManager().shutdown();

            if( errorHolder.isSet() )
            {
                throw new RuntimeException(
                    "Error during qiweb:start invocation: " + errorHolder.get().getMessage(),
                    errorHolder.get()
                );
            }
        }
        finally
        {
            runThread.interrupt();
        }
    }

    private Runnable newRunnable( final Holder<Exception> errorHolder, final String task )
    {
        return new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    GradleRunner runner = GradleRunnerFactory.create();
                    runner.setDirectory( tmp.getRoot() );
                    runner.setArguments( singletonList( task ) );
                    runner.run();
                }
                catch( Exception ex )
                {
                    errorHolder.set( ex );
                }
            }
        };
    }
}
