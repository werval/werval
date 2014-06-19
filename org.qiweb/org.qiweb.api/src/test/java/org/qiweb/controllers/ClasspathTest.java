/*
 * Copyright (c) 2013-2014 the original author or authors
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
package org.qiweb.controllers;

import com.jayway.restassured.response.Response;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import org.junit.ClassRule;
import org.junit.Test;
import org.qiweb.runtime.routes.RoutesParserProvider;
import org.qiweb.test.QiWebHttpRule;

import static com.jayway.restassured.RestAssured.expect;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ClasspathTest
{
    @ClassRule
    public static final QiWebHttpRule QIWEB = new QiWebHttpRule( new RoutesParserProvider(
        "GET /*path org.qiweb.controllers.Classpath.metainf( String path )" ) );

    @Test
    public void givenNonExistentResourceWhenRequestingExpectNotFound()
    {
        expect().
            statusCode( 404 ).
            when().
            get( "/qiweb/donotexists.yet" );
    }

    @Test
    public void givenResourceSmallerThanOneChunkWhenRequestingExpectCorrectResult()
        throws Exception
    {
        Response response = expect().
            statusCode( 200 ).
            header( "Transfer-Encoding", "chunked" ).
            header( "Content-Type", "application/octet-stream" ).
            // Trailers are not accessible in HttpClient API
            // See http://httpcomponents.10934.n7.nabble.com/HTTP-Trailers-in-HttpClient-4-0-1-td15030.html
            // header( "X-QiWeb-Content-Length", "666" ).
            when().
            get( "/qiweb/666B" );
        assertThat( response.asByteArray().length, equalTo( 666 ) );
    }

    @Test
    public void givenResourceSpanningSeveralCompleteChunksWhenRequestingExpectCorrectResult()
        throws Exception
    {
        Response response = expect().
            statusCode( 200 ).
            header( "Transfer-Encoding", "chunked" ).
            header( "Content-Type", "application/octet-stream" ).
            // Trailers are not accessible in HttpClient API
            // See http://httpcomponents.10934.n7.nabble.com/HTTP-Trailers-in-HttpClient-4-0-1-td15030.html
            // header( "X-QiWeb-Content-Length", "32768" ).
            when().
            get( "/qiweb/32KB" );
        assertThat( response.asByteArray().length, equalTo( 32768 ) );
    }

    @Test
    public void givenResourceSpanningOneChunkAndABitMoreWhenRequestingExpectCorrectResult()
        throws Exception
    {
        Response response = expect().
            statusCode( 200 ).
            header( "Transfer-Encoding", "chunked" ).
            header( "Content-Type", "application/octet-stream" ).
            // Trailers are not accessible in HttpClient API
            // See http://httpcomponents.10934.n7.nabble.com/HTTP-Trailers-in-HttpClient-4-0-1-td15030.html
            // header( "X-QiWeb-Content-Length", "32768" ).
            when().
            get( "/qiweb/8858B" );
        assertThat( response.asByteArray().length, equalTo( 8858 ) );
    }

    @Test
    public void indexFiles()
    {
        int expectedLength = new BigDecimal(
            new File( "src/test/resources/META-INF/resources/qiweb/index.html" ).length()
        ).intValueExact();

        Response response = expect()
            .statusCode( 200 )
            .when()
            .get( "/qiweb/index.html" );
        assertThat( response.body().asByteArray().length, equalTo( expectedLength ) );

        response = expect()
            .statusCode( 200 )
            .when()
            .get( "/qiweb" );
        assertThat( response.body().asByteArray().length, equalTo( expectedLength ) );
    }

    @Test
    public void givenDirectoryTraversalAttemptsWhenProcessingExpectBadRequest()
        throws Exception
    {
        // Simple directory traversal
        assertDirectoryTraversalAttemptFailed( "/qiweb/../../../shadow" );
        assertDirectoryTraversalAttemptFailed( "/../shadow" );

        // URI encoded directory traversal
        assertDirectoryTraversalAttemptFailed( "/%2e%2e%2fshadow" );
        assertDirectoryTraversalAttemptFailed( "/%2e%2e%5cshadow" );
        assertDirectoryTraversalAttemptFailed( "/%2e%2e/shadow" );
        assertDirectoryTraversalAttemptFailed( "/%2e./shadow" );
        assertDirectoryTraversalAttemptFailed( "/.%2e/shadow" );
        assertDirectoryTraversalAttemptFailed( "/..%2fshadow" );
        assertDirectoryTraversalAttemptFailed( "/..%5cshadow" );

        // Unicode / UTF-8 encoded directory traversal
        assertDirectoryTraversalAttemptFailed( "/\u002e\u002e\u002fshadow" );
        assertDirectoryTraversalAttemptFailed( "/\u002e.\u002fshadow" );
        assertDirectoryTraversalAttemptFailed( "/.\u002e\u002fshadow" );
        assertDirectoryTraversalAttemptFailed( "/..\u002fshadow" );
        assertDirectoryTraversalAttemptFailed( "/\u002e\u002e/shadow" );
    }

    private void assertDirectoryTraversalAttemptFailed( String path )
        throws IOException
    {
        expect().
            statusCode( either( is( 400 ) ).or( is( 404 ) ) ).
            when().
            get( path );
    }

    @Test
    public void removeHeadingSlash()
    {
        assertThat( Classpath.removeHeadingSlash( null ), equalTo( "" ) );
        assertThat( Classpath.removeHeadingSlash( "" ), equalTo( "" ) );
        assertThat( Classpath.removeHeadingSlash( "/" ), equalTo( "" ) );
        assertThat( Classpath.removeHeadingSlash( "///////" ), equalTo( "" ) );

        assertThat( Classpath.removeHeadingSlash( "/foo" ), equalTo( "foo" ) );
        assertThat( Classpath.removeHeadingSlash( "///////foo" ), equalTo( "foo" ) );
        assertThat( Classpath.removeHeadingSlash( "bar/" ), equalTo( "bar/" ) );
    }

    @Test
    public void removeTrailingSlash()
    {
        assertThat( Classpath.removeTrailingSlash( null ), equalTo( "" ) );
        assertThat( Classpath.removeTrailingSlash( "" ), equalTo( "" ) );
        assertThat( Classpath.removeTrailingSlash( "/" ), equalTo( "" ) );
        assertThat( Classpath.removeTrailingSlash( "///////" ), equalTo( "" ) );

        assertThat( Classpath.removeTrailingSlash( "/foo" ), equalTo( "/foo" ) );
        assertThat( Classpath.removeTrailingSlash( "bar/" ), equalTo( "bar" ) );
        assertThat( Classpath.removeTrailingSlash( "bar///////" ), equalTo( "bar" ) );
    }
}
