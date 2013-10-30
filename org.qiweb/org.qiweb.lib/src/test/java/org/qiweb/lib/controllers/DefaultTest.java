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

import org.junit.ClassRule;
import org.junit.Test;
import org.qiweb.runtime.routes.RoutesParserProvider;
import org.qiweb.test.QiWebRule;

import static com.jayway.restassured.RestAssured.expect;

public class DefaultTest
{

    @ClassRule
    public static final QiWebRule QIWEB = new QiWebRule( new RoutesParserProvider(
        "GET /notFound org.qiweb.lib.controllers.Default.notFound\n"
        + "GET /internalServerError org.qiweb.lib.controllers.Default.internalServerError\n"
        + "GET /notImplemented org.qiweb.lib.controllers.Default.notImplemented" ) );

    @Test
    public void givenNotFoundRouteWhenRequestingExpectNotFound()
        throws Exception
    {
        expect().
            statusCode( 404 ).
            when().
            get( "/notFound" );
    }

    @Test
    public void givenInternalServerErrorRouteWhenRequestingExpectInternalServerError()
        throws Exception
    {
        expect().
            statusCode( 500 ).
            when().
            get( "/internalServerError" );
    }

    @Test
    public void givenNotImplementedRouteWhenRequestingExpectNotImplemented()
        throws Exception
    {
        expect().
            statusCode( 501 ).
            when().
            get( "/notImplemented" );
    }

}
