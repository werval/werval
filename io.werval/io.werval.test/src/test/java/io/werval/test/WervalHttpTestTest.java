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
package io.werval.test;

import io.werval.api.outcomes.Outcome;
import io.werval.runtime.routes.RoutesParserProvider;
import org.junit.ClassRule;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.expect;
import static io.werval.api.context.CurrentContext.outcomes;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Assert Werval HTTP Test behaviour.
 */
public class WervalHttpTestTest
{
    public static class Controller
    {
        public Outcome index()
        {
            return outcomes().ok( "good" ).build();
        }
    }

    @ClassRule
    public static final WervalHttpRule WERVAL = new WervalHttpRule( new RoutesParserProvider(
        "GET / io.werval.test.WervalHttpTestTest$Controller.index"
    ) );

    @Test
    public void wervalHttpTest()
    {
        expect()
            .statusCode( 200 )
            .body( equalTo( "good" ) )
            .when()
            .get( "/" );
    }
}
