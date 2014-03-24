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
package org.qiweb.test;

import org.junit.ClassRule;
import org.junit.Test;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.runtime.routes.RoutesParserProvider;

import static com.jayway.restassured.RestAssured.expect;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.qiweb.api.context.CurrentContext.outcomes;

/**
 * Assert QiWeb HTTP Test behaviour.
 */
public class QiWebHttpTestTest
{
    public static class Controller
    {
        public Outcome index()
        {
            return outcomes().ok( "good" ).build();
        }
    }

    @ClassRule
    public static final QiWebHttpRule QIWEB = new QiWebHttpRule( new RoutesParserProvider(
        "GET / org.qiweb.test.QiWebHttpTestTest$Controller.index"
    ) );

    @Test
    public void qiWebHttpTest()
    {
        expect()
            .statusCode( 200 )
            .body( equalTo( "good" ) )
            .when()
            .get( "/" );
    }
}
