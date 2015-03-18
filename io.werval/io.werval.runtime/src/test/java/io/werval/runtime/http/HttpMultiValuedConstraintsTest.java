/*
 * Copyright (c) 2015 the original author or authors
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
import io.werval.test.WervalHttpRule;
import org.junit.ClassRule;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.when;
import static io.werval.api.context.CurrentContext.outcomes;
import static io.werval.api.context.CurrentContext.request;
import static io.werval.api.context.CurrentContext.response;
import static io.werval.api.context.CurrentContext.reverseRoutes;

/**
 * HTTP MultiValued Constraints Test.
 */
public class HttpMultiValuedConstraintsTest
{
    public static class Controller
    {
        public Outcome requestHeaders()
        {
            request().headers().singleValue( "foo" );
            return outcomes().ok().build();
        }

        public Outcome responseHeaders()
        {
            response().headers().singleValue( "foo" );
            return outcomes().ok().build();
        }

        public Outcome requestQueryString()
        {
            request().queryString().singleValue( "foo" );
            return outcomes().ok().build();
        }

        public Outcome requestFormAttributes()
        {
            request().body().formAttributes().singleValue( "foo" );
            return outcomes().ok().build();
        }

        public Outcome requestFormUploads()
        {
            request().body().formUploads().singleValue( "foo" );
            return outcomes().ok().build();
        }
    }

    @ClassRule
    public static final WervalHttpRule WERVAL = new WervalHttpRule( new RoutesParserProvider(
        "GET /requestHeaders io.werval.runtime.http.HttpMultiValuedConstraintsTest$Controller.requestHeaders\n"
        + "GET /responseHeaders io.werval.runtime.http.HttpMultiValuedConstraintsTest$Controller.responseHeaders\n"
        + "GET /requestQueryString io.werval.runtime.http.HttpMultiValuedConstraintsTest$Controller.requestQueryString\n"
        + "GET /requestFormAttributes io.werval.runtime.http.HttpMultiValuedConstraintsTest$Controller.requestFormAttributes\n"
        + "GET /requestFormUploads io.werval.runtime.http.HttpMultiValuedConstraintsTest$Controller.requestFormUploads\n"
    ) );

    @Test
    public void requestHeaders()
    {
        when().get( "/requestHeaders" ).then().statusCode( 400 );
    }

    @Test
    public void responseHeaders()
    {
        when().get( "/responseHeaders" ).then().statusCode( 500 );
    }

    @Test
    public void requestQueryString()
    {
        when().get( "/requestQueryString" ).then().statusCode( 400 );
    }

    @Test
    public void requestFormAttributes()
    {
        when().get( "/requestFormAttributes" ).then().statusCode( 400 );
    }

    @Test
    public void requestFormUploads()
    {
        when().get( "/requestFormUploads" ).then().statusCode( 400 );
    }
}
