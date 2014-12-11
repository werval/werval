/*
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
package io.werval.runtime.http;

import io.werval.api.outcomes.Outcome;
import io.werval.runtime.routes.RoutesParserProvider;
import org.junit.ClassRule;
import org.junit.Test;
import org.qiweb.test.QiWebHttpRule;

import static com.jayway.restassured.RestAssured.given;
import static io.werval.api.context.CurrentContext.outcomes;
import static io.werval.api.context.CurrentContext.request;
import static io.werval.api.mime.MimeTypesNames.TEXT_PLAIN;
import static org.hamcrest.Matchers.equalTo;

public class HttpRequestBodyTest
{
    public static class Controller
    {
        public Outcome echo()
        {
            return outcomes().
                ok( request().body().asString() ).
                as( TEXT_PLAIN ).
                build();
        }
    }

    @ClassRule
    public static final QiWebHttpRule QIWEB = new QiWebHttpRule( new RoutesParserProvider(
        "POST /echo io.werval.runtime.http.HttpRequestBodyTest$Controller.echo" ) );

    @Test
    public void testBodyAsString()
    {
        given().
            contentType( TEXT_PLAIN ).
            body( "FooBarBazar" ).
            expect().
            statusCode( 200 ).
            body( equalTo( "FooBarBazar" ) ).
            when().
            post( "/echo" );
    }
}
