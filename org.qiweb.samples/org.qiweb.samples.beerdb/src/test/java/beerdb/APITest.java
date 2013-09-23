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
package beerdb;

import com.jayway.restassured.response.Response;
import org.junit.Test;
import org.qiweb.test.QiWebTest;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.qiweb.api.http.Headers.Names.LOCATION;
import static org.qiweb.api.mime.MimeTypesNames.APPLICATION_JSON;
import static org.qiweb.api.mime.MimeTypesNames.TEXT_PLAIN;
import static org.qiweb.samples.beerdb.BuildVersion.COMMIT;
import static org.qiweb.samples.beerdb.BuildVersion.DATE;
import static org.qiweb.samples.beerdb.BuildVersion.DETAILED_VERSION;
import static org.qiweb.samples.beerdb.BuildVersion.DIRTY;
import static org.qiweb.samples.beerdb.BuildVersion.NAME;
import static org.qiweb.samples.beerdb.BuildVersion.VERSION;

/**
 * Assert API Behaviour.
 */
public class APITest
    extends QiWebTest
{

    @Test
    public void testIndex()
    {
        expect().
            statusCode( 200 ).
            contentType( APPLICATION_JSON ).
            body( "_links.self.href", equalTo( baseHttpUrl() + "/api" ) ).
            body( "_links.beers.href", equalTo( baseHttpUrl() + "/api/beers" ) ).
            body( "_links.breweries.href", equalTo( baseHttpUrl() + "/api/breweries" ) ).
            body( "commit", equalTo( COMMIT ) ).
            body( "date", equalTo( DATE ) ).
            body( "detailed-version", equalTo( DETAILED_VERSION ) ).
            body( "dirty", equalTo( DIRTY ) ).
            body( "name", equalTo( NAME ) ).
            body( "version", equalTo( VERSION ) ).
            when().
            get( "/api" );
    }

    @Test
    public void testBeers()
    {
        expect().
            statusCode( 200 ).
            contentType( APPLICATION_JSON ).
            body( "_links.self.href", equalTo( baseHttpUrl() + "/api/beers" ) ).
            body( "count", equalTo( 0 ) ).
            when().
            get( "/api/beers" );
    }

    @Test
    public void testBreweries()
    {
        expect().
            statusCode( 200 ).
            contentType( APPLICATION_JSON ).
            body( "_links.self.href", equalTo( baseHttpUrl() + "/api/breweries" ) ).
            body( "count", equalTo( 0 ) ).
            when().
            get( "/api/breweries" );
    }

    @Test
    public void testCreateBreweriesBadContentType()
    {
        given().
            contentType( TEXT_PLAIN ).
            body( "{ \"name\":\"ZengBrewery\", \"url\":\"http://zeng-beers.com/\" }" ).
            expect().
            statusCode( 400 ).
            body( containsString( "content-type" ) ).
            when().
            post( "/api/breweries" );

        given().
            contentType( APPLICATION_JSON ).
            body( "BAD PAYLOAD" ).
            expect().
            statusCode( 400 ).
            body( containsString( "unrecognized" ) ).
            when().
            post( "/api/breweries" );

        given().
            contentType( APPLICATION_JSON ).
            body( "{}" ).
            expect().
            statusCode( 400 ).
            body( containsString( "name" ) ).
            when().
            post( "/api/breweries" );

        given().
            contentType( APPLICATION_JSON ).
            body( "{ \"url\":\"http://zeng-beers.com/\" }" ).
            expect().
            statusCode( 400 ).
            body( containsString( "name" ) ).
            when().
            post( "/api/breweries" );

        given().
            contentType( APPLICATION_JSON ).
            body( "{ \"name\":\"Wow it's a name\" }" ).
            expect().
            statusCode( 400 ).
            body( containsString( "url" ) ).
            when().
            post( "/api/breweries" );

        given().
            contentType( APPLICATION_JSON ).
            body( "{ \"name\":\"\", \"url\":\"http://zeng-beers.com/\" }" ).
            expect().
            statusCode( 400 ).
            body( containsString( "name" ) ).
            when().
            post( "/api/breweries" );

        given().
            contentType( APPLICATION_JSON ).
            body( "{ \"name\":\"Wow it's a name\", \"url\":\"But this is not an URL\" }" ).
            expect().
            statusCode( 400 ).
            body( containsString( "url" ) ).
            when().
            post( "/api/breweries" );

        given().
            contentType( APPLICATION_JSON ).
            body( "{ \"name\":\"   \", \"url\":\"But this is not an URL\" }" ).
            expect().
            statusCode( 400 ).
            body( containsString( "name" ) ).
            body( containsString( "url" ) ).
            when().
            post( "/api/breweries" );
    }

    @Test
    public void testCreateBreweries()
    {
        Response response = given().
            contentType( APPLICATION_JSON ).
            body( "{ \"name\":\"ZengBrewery\", \"url\":\"http://zeng-beers.com/\" }" ).
            expect().
            statusCode( 201 ).
            when().
            post( "/api/breweries" );

        String breweryUrl = response.header( LOCATION );

        expect().
            statusCode( 200 ).
            contentType( APPLICATION_JSON ).
            body( "_links.self.href", equalTo( breweryUrl ) ).
            body( "name", equalTo( "ZengBrewery" ) ).
            when().
            get( breweryUrl );

        expect().
            statusCode( 200 ).
            contentType( APPLICATION_JSON ).
            body( "_links.self.href", equalTo( baseHttpUrl() + "/api/breweries" ) ).
            body( "count", equalTo( 1 ) ).
            body( "_embedded.brewery.name", equalTo( "ZengBrewery" ) ).
            body( "_embedded.brewery._links.self.href", equalTo( breweryUrl ) ).
            when().
            get( "/api/breweries" );
    }
}
