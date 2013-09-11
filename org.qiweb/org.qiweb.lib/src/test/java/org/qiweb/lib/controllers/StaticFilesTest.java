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

import com.jayway.restassured.response.Response;
import java.io.File;
import java.math.BigDecimal;
import org.junit.Test;
import org.qiweb.runtime.routes.RoutesParserProvider;
import org.qiweb.runtime.routes.RoutesProvider;
import org.qiweb.test.AbstractQiWebTest;

import static com.jayway.restassured.RestAssured.expect;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Assert that the StaticFiles controller behave correctly.
 * <p>Please note that this test rely on the fact that the current working directory is set to the module base dir.</p>
 */
public class StaticFilesTest
    extends AbstractQiWebTest
{

    private static final File ROOT = new File( "src/test/resources" );

    @Override
    protected RoutesProvider routesProvider()
    {
        return new RoutesParserProvider(
            "GET /single org.qiweb.lib.controllers.StaticFiles.file( String file = 'src/test/resources/logback.xml' )\n"
            + "GET /tree/*path org.qiweb.lib.controllers.StaticFiles.tree( String root = 'src/test/resources', String path )" );
    }

    @Test
    public void givenSingleStaticFileRouteWhenRequestingExpectCorrectResult()
    {
        int expectedLength = fileLength( "logback.xml" );
        Response response = expect().
            statusCode( 200 ).
            header( "Content-Length", String.valueOf( expectedLength ) ).
            when().
            get( "/single" );
        assertThat( response.body().asByteArray().length, equalTo( expectedLength ) );
    }

    @Test
    public void givenTreeStaticFileRouteWhenRequestingDirectoryExpectIndexFile()
        throws Exception
    {
        int expectedLength = fileLength( "staticfiles/index.html" );
        Response response = expect().
            statusCode( 200 ).
            header( "Content-Length", String.valueOf( expectedLength ) ).
            when().
            get( "/tree/staticfiles/" );
        assertThat( response.body().asByteArray().length, equalTo( expectedLength ) );
    }

    @Test
    public void givenTreeStaticFilesRouteWhenRequestingNotPresentExpectNotFound()
    {
        expect().
            statusCode( 404 ).
            when().
            get( "/tree/not.found" );
    }

    @Test
    public void givenTreeStaticFilesRouteWhenRequestingExpectCorrectResult()
        throws Exception
    {
        int expectedLength = fileLength( "logback.xml" );
        Response response = expect().
            statusCode( 200 ).
            header( "Content-Length", String.valueOf( expectedLength ) ).
            when().
            get( "/tree/logback.xml" );
        assertThat( response.body().asByteArray().length, equalTo( expectedLength ) );
    }

    private int fileLength( String relativePath )
    {
        return new BigDecimal( new File( ROOT, relativePath ).length() ).intValueExact();
    }
}
