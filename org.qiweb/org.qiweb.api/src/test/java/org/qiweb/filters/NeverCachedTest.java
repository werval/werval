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
package org.qiweb.filters;

import org.junit.ClassRule;
import org.junit.Test;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.runtime.routes.RoutesParserProvider;
import org.qiweb.test.QiWebHttpRule;

import static com.jayway.restassured.RestAssured.expect;
import static org.qiweb.api.context.CurrentContext.outcomes;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.qiweb.api.http.Headers.Names.CACHE_CONTROL;
import static org.qiweb.api.http.Headers.Names.EXPIRES;
import static org.qiweb.api.http.Headers.Names.PRAGMA;
import static org.qiweb.api.http.StatusClass.CLIENT_ERROR;
import static org.qiweb.api.http.StatusClass.SERVER_ERROR;

/**
 * NeverCachedTest.
 */
public class NeverCachedTest
{
    @ClassRule
    public static final QiWebHttpRule QIWEB = new QiWebHttpRule( new RoutesParserProvider(
        "GET /normal org.qiweb.filters.NeverCachedTest$Controller.normal\n"
        + "GET /error org.qiweb.filters.NeverCachedTest$Controller.error\n"
        + "GET /errorNeverCached org.qiweb.filters.NeverCachedTest$Controller.errorNeverCached\n"
    ) );

    public static class Controller
    {
        @NeverCached
        public Outcome normal()
        {
            return outcomes().ok().build();
        }

        public Outcome error()
        {
            return outcomes().internalServerError().build();
        }

        @NeverCached( { SERVER_ERROR, CLIENT_ERROR } )
        public Outcome errorNeverCached()
        {
            return outcomes().internalServerError().build();
        }
    }

    @Test
    public void normal()
    {
        expect()
            .statusCode( 200 )
            .header( CACHE_CONTROL, "no-cache, no-store, max-age=0, must-revalidate" )
            .header( PRAGMA, "no-cache" )
            .header( EXPIRES, "0" )
            .when()
            .get( "/normal" );
    }

    @Test
    public void error()
    {
        expect()
            .statusCode( 500 )
            .header( CACHE_CONTROL, nullValue() )
            .header( PRAGMA, nullValue() )
            .header( EXPIRES, nullValue() )
            .when()
            .get( "/error" );
    }

    @Test
    public void errorNeverCached()
    {
        expect()
            .statusCode( 500 )
            .header( CACHE_CONTROL, "no-cache, no-store, max-age=0, must-revalidate" )
            .header( PRAGMA, "no-cache" )
            .header( EXPIRES, "0" )
            .when()
            .get( "/errorNeverCached" );
    }
}
