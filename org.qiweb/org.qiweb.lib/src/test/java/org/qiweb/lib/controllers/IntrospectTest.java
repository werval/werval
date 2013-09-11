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

import org.junit.Ignore;
import org.junit.Test;
import org.qiweb.runtime.routes.RoutesParserProvider;
import org.qiweb.runtime.routes.RoutesProvider;
import org.qiweb.test.AbstractQiWebTest;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class IntrospectTest
    extends AbstractQiWebTest
{

    @Override
    protected RoutesProvider routesProvider()
    {
        return new RoutesParserProvider(
            "GET /@config org.qiweb.lib.controllers.Introspect.config\n"
            + "GET /@version org.qiweb.lib.controllers.Introspect.version\n"
            + "GET /@classpath org.qiweb.lib.controllers.Introspect.classpath\n"
            + "GET /@logs org.qiweb.lib.controllers.Introspect.logs\n" );
    }

    @Test
    public void testJSONConfig()
    {
        given().
            header( "Accept", "application/json" ).
            expect().
            statusCode( 200 ).
            contentType( "application/json" ).
            body( "app.secret", notNullValue() ).
            when().
            get( "/@config" );
    }

    @Test
    public void testJSONVersion()
    {
        given().
            header( "Accept", "application/json" ).
            expect().
            statusCode( 200 ).
            contentType( "application/json" ).
            body( "version", notNullValue() ).
            body( "commit", notNullValue() ).
            body( "dirty", either( is( false ) ).or( is( true ) ) ).
            body( "date", notNullValue() ).
            when().
            get( "/@version" );
    }

    @Test
    @Ignore
    public void testHTMLVersion()
    {
        given().
            header( "Accept", "text/html" ).
            expect().
            statusCode( 200 ).
            contentType( "text/html" ).
            when().
            get( "/@version" );
    }
}
