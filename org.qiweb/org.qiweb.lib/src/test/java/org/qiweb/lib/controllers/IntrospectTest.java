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
import org.qiweb.test.QiWebTest;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.qiweb.api.BuildVersion.COMMIT;
import static org.qiweb.api.BuildVersion.DATE;
import static org.qiweb.api.BuildVersion.DETAILED_VERSION;
import static org.qiweb.api.BuildVersion.DIRTY;
import static org.qiweb.api.BuildVersion.VERSION;
import static org.qiweb.api.mime.MimeTypes.APPLICATION_JSON;
import static org.qiweb.api.mime.MimeTypes.TEXT_HTML;

public class IntrospectTest
    extends QiWebTest
{

    @Override
    protected RoutesProvider routesProvider()
    {
        return new RoutesParserProvider(
            "GET /@config org.qiweb.lib.controllers.Introspect.config\n"
            + "GET /@version org.qiweb.lib.controllers.Introspect.version\n" );
    }

    @Test
    public void testJSONConfig()
    {
        given().
            header( "Accept", APPLICATION_JSON ).
            expect().
            statusCode( 200 ).
            contentType( APPLICATION_JSON ).
            body( "app.secret", notNullValue() ).
            when().
            get( "/@config" );
    }

    @Test
    public void testJSONVersion()
    {
        given().
            header( "Accept", APPLICATION_JSON ).
            expect().
            statusCode( 200 ).
            contentType( APPLICATION_JSON ).
            body( "version", equalTo( VERSION ) ).
            body( "commit", equalTo( COMMIT ) ).
            body( "dirty", is( DIRTY ) ).
            body( "date", equalTo( DATE ) ).
            body( "detail", equalTo( DETAILED_VERSION ) ).
            when().
            get( "/@version" );
    }

    @Test
    @Ignore
    public void testHTMLVersion()
    {
        given().
            header( "Accept", TEXT_HTML ).
            expect().
            statusCode( 200 ).
            contentType( TEXT_HTML ).
            when().
            get( "/@version" );
    }
}
