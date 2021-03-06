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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.werval.api.outcomes.Outcome;
import io.werval.runtime.routes.RoutesParserProvider;
import io.werval.test.WervalHttpRule;
import org.junit.ClassRule;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static io.werval.api.context.CurrentContext.outcomes;
import static io.werval.api.context.CurrentContext.request;
import static org.hamcrest.Matchers.equalTo;

public class QueryStringTest
{
    public static class Controller
    {
        private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

        public Outcome echo()
            throws JsonProcessingException
        {
            String json = JSON_MAPPER.writeValueAsString( request().queryString().allValues() );
            return outcomes().ok( json ).as( "application/json" ).build();
        }
    }

    @ClassRule
    public static final WervalHttpRule WERVAL = new WervalHttpRule( new RoutesParserProvider(
        "GET /echo io.werval.runtime.http.QueryStringTest$Controller.echo"
    ) );

    @Test
    public void testEmptyQueryString()
    {
        expect().
            statusCode( 200 ).
            body( equalTo( "{}" ) ).
            when().
            get( "/echo" );
    }

    @Test
    public void testSingleValuesQueryString()
    {
        given().
            queryParam( "foo", "bar" ).
            queryParam( "baz" ).
            expect().
            statusCode( 200 ).
            body( "foo[0]", equalTo( "bar" ) ).
            body( "baz[0]", equalTo( "" ) ).
            when().
            get( "/echo" );
    }

    @Test
    public void testMultipleValuesQueryString()
    {
        given().
            queryParam( "foo", "bar" ).
            queryParam( "baz", "cathedral", "bazar" ).
            expect().
            statusCode( 400 ).
            when().
            get( "/echo" );
    }
}
