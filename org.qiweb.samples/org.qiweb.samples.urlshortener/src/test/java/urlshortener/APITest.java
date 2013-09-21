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
package urlshortener;

import com.jayway.restassured.response.Response;
import java.util.Map;
import org.qiweb.test.QiWebTest;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.qiweb.api.mime.MimeTypes.TEXT_CSS;

/**
 * Assert API behaviour.
 */
public class APITest
    extends QiWebTest
{

    @Test
    @SuppressWarnings( "unchecked" )
    public void testUrlShortenerAPI()
    {
        // Assert list is empty
        expect().
            statusCode( 200 ).
            body( equalTo( "[]" ) ).
            when().
            get( "/api/list" );

        String longUrl = baseHttpUrl() + "/client/lib/webjars/bootstrap/3.0.0/css/bootstrap.min.css";

        // Assert can shorten URL
        Response response = given().
            queryParam( "url", longUrl ).
            expect().
            statusCode( 200 ).
            body( "hash", notNullValue() ).
            body( "short_url", startsWith( "http://" ) ).
            when().
            get( "/api/shorten" );

        Map<String, String> data = response.as( Map.class );
        String hash = data.get( "hash" );
        String shortUrl = data.get( "short_url" );

        // Assert presence in list
        expect().
            statusCode( 200 ).
            body( containsString( hash ) ).
            body( containsString( longUrl ) ).
            when().
            get( "/api/list" );

        // Assert expand to correct URL
        response = given().
            queryParam( "hash", hash ).
            expect().
            statusCode( 200 ).
            body( "hash", equalTo( hash ) ).
            body( "long_url", startsWith( "http://" ) ).
            when().
            get( "/api/expand" );
        data = response.as( Map.class );
        assertThat( data.get( "long_url" ), equalTo( longUrl ) );

        // Assert lookup form long URL
        given().
            queryParam( "url", longUrl ).
            expect().
            statusCode( 200 ).
            body( containsString( hash ) ).
            body( containsString( shortUrl ) ).
            when().
            get( "/api/lookup" );

        // Assert redirection works
        expect().
            statusCode( 200 ).
            contentType( TEXT_CSS ).
            body( containsString( "Bootstrap" ) ).
            when().
            get( shortUrl );
    }
}
