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

import io.werval.runtime.util.Holder;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;

/**
 * DevShellMojo Integration Test.
 *
 * Generates a project, run it in dev mode using the plugin, change source code and assert code is reloaded.
 */
public class DevShellMojoIT
    extends AbstractQiWebMojoIT
{
    @Test
    public void devshellMojoIntegrationTest()
        throws InterruptedException, IOException
    {
        final Holder<Exception> errorHolder = new Holder<>();
        Thread devshellThread = new Thread(
            newRunnable( errorHolder, "qiweb:devshell" ),
            "maven-qiweb-devshell-thread"
        );
        try
        {
            devshellThread.start();

            await().atMost( 30, SECONDS ).until( () -> lock.exists() );

            if( errorHolder.isSet() )
            {
                throw new RuntimeException(
                    "Error during devhell invocation: " + errorHolder.get().getMessage(),
                    errorHolder.get()
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

            assertThat(
                client.execute( new HttpGet( "http://localhost:23023/@doc" ), handler ),
                containsString( "QiWeb Documentation" )
            );

            client.getConnectionManager().shutdown();
        }
        finally
        {
            devshellThread.interrupt();
        }
    }
}
