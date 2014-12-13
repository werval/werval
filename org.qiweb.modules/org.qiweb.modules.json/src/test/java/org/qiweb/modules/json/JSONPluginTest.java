/*
 * Copyright (c) 2014 the original author or authors
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
package org.qiweb.modules.json;

import io.werval.api.outcomes.Outcome;
import io.werval.runtime.routes.RoutesParserProvider;
import io.werval.test.WervalHttpRule;
import java.util.Arrays;
import java.util.List;
import org.junit.ClassRule;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static io.werval.api.context.CurrentContext.outcomes;
import static io.werval.api.mime.MimeTypesNames.APPLICATION_JAVASCRIPT;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.qiweb.modules.json.JSON.json;

/**
 * JSON Plugin Test.
 */
public class JSONPluginTest
{
    public static class Controller
    {
        public Outcome staticJson()
        {
            json();
            return outcomes().ok().build();
        }

        public Outcome jsonp( String callback )
        {
            List<String> pretext = Arrays.asList( "foo", "bar" );
            return outcomes().ok( json().toJSONP( callback, pretext ) ).as( APPLICATION_JAVASCRIPT ).build();
        }
    }

    @ClassRule
    public static final WervalHttpRule WERVAL = new WervalHttpRule( new RoutesParserProvider(
        "GET /static org.qiweb.modules.json.JSONPluginTest$Controller.staticJson\n"
        + "GET /jsonp org.qiweb.modules.json.JSONPluginTest$Controller.jsonp( String callback ?= 'callback' )"
    ) );

    @Test
    public void objectMapper()
    {
        JSON json = WERVAL.application().plugin( JSON.class );
        assertThat( json, notNullValue() );
        assertThat( json.mapper(), notNullValue() );
    }

    @Test
    public void staticJson()
    {
        expect()
            .statusCode( 200 )
            .when()
            .get( "/static" );
    }

    @Test
    public void jsonp()
    {
        expect()
            .statusCode( 200 )
            .contentType( APPLICATION_JAVASCRIPT )
            .body( equalTo( "callback([\"foo\",\"bar\"])" ) )
            .when()
            .get( "/jsonp" );

        given()
            .queryParam( "callback", "customFunction" )
            .expect()
            .statusCode( 200 )
            .contentType( APPLICATION_JAVASCRIPT )
            .body( equalTo( "customFunction([\"foo\",\"bar\"])" ) )
            .when()
            .get( "/jsonp" );
    }
}
