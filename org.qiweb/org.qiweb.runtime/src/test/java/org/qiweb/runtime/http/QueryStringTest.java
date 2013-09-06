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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.qiweb.api.controllers.Outcome;
import org.qiweb.test.AbstractQiWebTest;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.qiweb.api.controllers.Controller.outcomes;
import static org.qiweb.api.controllers.Controller.request;

public class QueryStringTest
    extends AbstractQiWebTest
{

    public static class Controller
    {

        private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

        public Outcome echo()
            throws JsonProcessingException
        {
            String json = JSON_MAPPER.writeValueAsString( request().queryString().asMapAll() );
            return outcomes().ok( json ).as( "application/json" ).build();
        }
    }

    @Override
    protected String routesString()
    {
        return "GET /echo org.qiweb.runtime.http.QueryStringTest$Controller.echo";
    }

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
            statusCode( 500 ). // TODO Change to 400 once proper error handling is done
            when().
            get( "/echo" );
    }
}
