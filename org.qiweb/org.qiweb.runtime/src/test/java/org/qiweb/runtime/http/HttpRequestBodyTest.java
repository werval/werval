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
package org.qiweb.runtime.http;

import org.junit.ClassRule;
import org.junit.Test;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.runtime.routes.RoutesParserProvider;
import org.qiweb.test.QiWebRule;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.qiweb.api.context.CurrentContext.outcomes;
import static org.qiweb.api.context.CurrentContext.request;
import static org.qiweb.api.mime.MimeTypesNames.TEXT_PLAIN;

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
    public static final QiWebRule QIWEB = new QiWebRule( new RoutesParserProvider(
        "POST /echo org.qiweb.runtime.http.HttpRequestBodyTest$Controller.echo" ) );

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
