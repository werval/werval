/**
 * Copyright (c) 2013 the original author or authors
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
package org.qiweb.lib.controllers;

import java.io.File;
import java.math.BigDecimal;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.qiweb.test.AbstractQiWebTest;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 * Assert that the StaticFiles controller behave correctly.
 * <p>Please note that this test rely on the fact that the current working directory is set to the module base dir.</p>
 */
public class StaticFilesTest
    extends AbstractQiWebTest
{

    private static final File ROOT = new File( "src/test/resources" );

    @Override
    protected String routesString()
    {
        return "GET /single org.qiweb.lib.controllers.StaticFiles.file( String file = 'src/test/resources/logback.xml' )\n"
               + "GET /tree/*path org.qiweb.lib.controllers.StaticFiles.tree( String root = 'src/test/resources', String path )";
    }

    @Test
    public void givenSingleStaticFileRouteWhenRequestingExpectCorrectResult()
        throws Exception
    {
        HttpResponse response = newHttpClientInstance().execute( new HttpGet( BASE_URL + "single" ) );
        soutResponseHead( response );
        assertFileResponse( response, new File( ROOT, "logback.xml" ) );
    }

    @Test
    public void givenTreeStaticFileRouteWhenRequestingDirectoryExpectIndexFile()
        throws Exception
    {
        HttpResponse response = newHttpClientInstance().execute( new HttpGet( BASE_URL + "tree/staticfiles/" ) );
        soutResponseHead( response );
        assertFileResponse( response, new File( ROOT, "staticfiles/index.html" ) );
    }

    @Test
    public void givenTreeStaticFilesRouteWhenRequestingExpectCorrectResult()
        throws Exception
    {
        HttpResponse response = newHttpClientInstance().execute( new HttpGet( BASE_URL + "tree/not.found" ) );
        soutResponseHead( response );
        assertThat( response.getStatusLine().getStatusCode(), equalTo( 404 ) );

        response = newHttpClientInstance().execute( new HttpGet( BASE_URL + "tree/logback.xml" ) );
        soutResponseHead( response );
        assertFileResponse( response, new File( ROOT, "logback.xml" ) );
    }

    private void assertFileResponse( HttpResponse response, File file )
        throws Exception
    {
        assertThat( response.getStatusLine().getStatusCode(), equalTo( 200 ) );
        long length = file.length();
        assertThat( response.getLastHeader( "Content-Length" ).getValue(),
                    equalTo( String.valueOf( length ) ) );
        byte[] body = EntityUtils.toByteArray( response.getEntity() );
        assertThat( body.length, equalTo( new BigDecimal( length ).intValueExact() ) );
    }
}
