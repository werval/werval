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
package io.werval.controllers;

import io.werval.runtime.routes.RoutesParserProvider;
import io.werval.test.WervalHttpRule;
import org.junit.ClassRule;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.expect;

public class DefaultTest
{
    @ClassRule
    public static final WervalHttpRule WERVAL = new WervalHttpRule( new RoutesParserProvider(
        "GET /notFound Default.notFound\n"
        + "GET /internalServerError Default.internalServerError\n"
        + "GET /notImplemented Default.notImplemented" ) );

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
