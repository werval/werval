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
package org.qiweb.runtime.http;

import org.junit.ClassRule;
import org.junit.Test;
import org.qiweb.api.http.FormAttributes;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.runtime.routes.RoutesParserProvider;
import org.qiweb.test.QiWebHttpRule;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.qiweb.api.context.CurrentContext.outcomes;
import static org.qiweb.api.context.CurrentContext.request;
import static org.qiweb.api.mime.MimeTypesNames.TEXT_PLAIN;

/**
 * Form Test.
 */
public class FormTest
{
    public static class Controller
    {
        public Outcome attributes()
        {
            FormAttributes form = request().body().formAttributes();
            return outcomes().ok( form.allValues().toString() ).asTextual( TEXT_PLAIN ).build();
        }
    }

    @ClassRule
    public static final QiWebHttpRule QIWEB = new QiWebHttpRule( new RoutesParserProvider(
        "POST /attributes org.qiweb.runtime.http.FormTest$Controller.attributes"
    ) );

    @Test
    public void attributes()
    {
        given()
            .formParam( "foo", "bar" )
            .formParam( "bazar", "cathedral" )
            .expect()
            .body( equalTo( "{bazar=[cathedral], foo=[bar]}" ) )
            .when()
            .post( "/attributes" );

        given()
            .formParam( "mou", "zou" )
            .formParam( "grou", "mlou" )
            .expect()
            .body( equalTo( "{grou=[mlou], mou=[zou]}" ) )
            .when()
            .post( "/attributes" );

        given()
            .formParam( "foo", "bar" )
            .formParam( "bazar", "cathedral" )
            .expect()
            .body( equalTo( "{bazar=[cathedral], foo=[bar]}" ) )
            .when()
            .post( "/attributes" );
    }
}
