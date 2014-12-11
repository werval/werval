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
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.qiweb.test.QiWebHttpRule;

import static com.jayway.restassured.RestAssured.given;
import static io.werval.api.BuildVersion.COMMIT;
import static io.werval.api.BuildVersion.DATE;
import static io.werval.api.BuildVersion.DETAILED_VERSION;
import static io.werval.api.BuildVersion.DIRTY;
import static io.werval.api.BuildVersion.VERSION;
import static io.werval.api.mime.MimeTypesNames.APPLICATION_JSON;
import static io.werval.api.mime.MimeTypesNames.TEXT_HTML;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class IntrospectTest
{
    @ClassRule
    public static final QiWebHttpRule QIWEB = new QiWebHttpRule( new RoutesParserProvider(
        "GET /@config io.werval.controllers.Introspect.config\n"
        + "GET /@version io.werval.controllers.Introspect.version\n"
    ) );

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
