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
package io.werval.runtime.outcomes;

import io.werval.api.outcomes.Outcome;
import io.werval.runtime.routes.RoutesParserProvider;
import io.werval.test.WervalHttpRule;
import java.util.concurrent.CompletableFuture;
import org.junit.ClassRule;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.expect;
import static io.werval.api.context.CurrentContext.outcomes;
import static io.werval.api.context.CurrentContext.executors;

/**
 * Future&lt;Outcome&gt; Test.
 */
public class FutureOutcomeTest
{
    public static class Controller
    {
        public Outcome normal()
        {
            return outcomes().ok().build();
        }

        public CompletableFuture<Outcome> future()
        {
            return executors().supplyAsync( () -> outcomes().ok().build() );
        }
    }

    @ClassRule
    public static final WervalHttpRule WERVAL = new WervalHttpRule( new RoutesParserProvider(
        "GET /normal io.werval.runtime.outcomes.FutureOutcomeTest$Controller.normal\n"
        + "GET /future io.werval.runtime.outcomes.FutureOutcomeTest$Controller.future\n"
    ) );

    @Test
    public void normal()
    {
        expect()
            .statusCode( 200 )
            .when()
            .get( "/normal" );
    }

    @Test
    public void future()
    {
        expect()
            .statusCode( 200 )
            .when()
            .get( "/future" );
    }
}
