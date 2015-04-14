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
package io.werval.maven;

import io.werval.runtime.util.Holder;
import java.io.IOException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.core.StringContains.containsString;

/**
 * StartMojo Integration Test.
 * <p>
 * Generates a project, run it in prod mode and assert applicaiton is available.
 */
public class StartMojoIT
    extends AbstractRunGoalIT
{
    @Test
    public void startMojoIntegrationTest()
        throws IOException, InterruptedException
    {
        final Holder<Exception> errorHolder = new Holder<>();
        Thread runThread = new Thread(
            newRunnable( errorHolder, "werval:start" ),
            "maven-werval-start-thread"
        );
        try
        {
            runThread.start();

            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet( "http://localhost:23023/" );
            ResponseHandler<String> handler = new BasicResponseHandler();

            await().atMost( 60, SECONDS ).pollDelay( 5, SECONDS ).until(
                () ->
                {
                    try
                    {
                        return client.execute( get, handler );
                    }
                    catch( Exception ex )
                    {
                        return null;
                    }
                },
                containsString( "I ran!" )
            );

            client.getConnectionManager().shutdown();
        }
        finally
        {
            runThread.interrupt();
        }
    }
}
