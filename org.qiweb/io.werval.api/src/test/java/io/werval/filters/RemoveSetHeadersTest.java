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
package io.werval.filters;

import com.jayway.restassured.response.Response;
import io.werval.api.outcomes.Outcome;
import io.werval.runtime.routes.RoutesParserProvider;
import org.junit.ClassRule;
import org.junit.Test;
import org.qiweb.test.QiWebHttpRule;

import static com.jayway.restassured.RestAssured.expect;
import static io.werval.api.context.CurrentContext.outcomes;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Remove/Set Headers Test.
 */
public class RemoveSetHeadersTest
{
    @ClassRule
    public static final QiWebHttpRule QIWEB = new QiWebHttpRule( new RoutesParserProvider(
        "GET /setHeader io.werval.filters.RemoveSetHeadersTest$Controller.setHeader\n"
        + "GET /removeHeaders io.werval.filters.RemoveSetHeadersTest$Controller.removeHeaders\n"
    ) );

    public static class Controller
    {
        @SetHeader( name = "foo", values = "bar" )
        @SetHeader( name = "bazar", values =
                {
                    "cathedral", "foo"
        } )
        public Outcome setHeader()
        {
            return outcomes().ok().build();
        }

        @RemoveHeaders( "foo" )
        public Outcome removeHeaders()
        {
            return outcomes().ok().withHeader( "foo", "bar" ).withHeader( "bazar", "cathedral" ).build();
        }
    }

    @Test
    public void setHeader()
    {
        Response response = expect()
            .statusCode( 200 )
            .when()
            .get( "/setHeader" );
        assertThat( response.header( "foo" ), equalTo( "bar" ) );
        assertThat( response.getHeaders().getValues( "bazar" ), contains( "cathedral", "foo" ) );
    }

    @Test
    public void removeHeaders()
    {
        expect()
            .statusCode( 200 )
            .header( "foo", nullValue() )
            .header( "bazar", "cathedral" )
            .when()
            .get( "/removeHeaders" );
    }
}
