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

import org.junit.Test;
import org.qiweb.test.QiWebTest;

import static com.jayway.restassured.RestAssured.expect;
import static org.hamcrest.Matchers.equalTo;
import static org.qiweb.api.mime.MimeTypesNames.APPLICATION_JSON;
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
}
