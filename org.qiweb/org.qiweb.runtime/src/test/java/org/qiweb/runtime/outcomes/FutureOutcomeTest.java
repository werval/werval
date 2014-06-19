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
package org.qiweb.runtime.outcomes;

import java.util.concurrent.CompletableFuture;
import org.junit.ClassRule;
import org.junit.Test;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.runtime.routes.RoutesParserProvider;
import org.qiweb.test.QiWebHttpRule;

import static com.jayway.restassured.RestAssured.expect;
import static org.qiweb.api.context.CurrentContext.get;
import static org.qiweb.api.context.CurrentContext.outcomes;

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
            return CompletableFuture.supplyAsync(
                () -> outcomes().ok().build(),
                get().executor()
            );
        }
    }

    @ClassRule
    public static final QiWebHttpRule QIWEB = new QiWebHttpRule( new RoutesParserProvider(
        "GET /normal org.qiweb.runtime.outcomes.FutureOutcomeTest$Controller.normal\n"
        + "GET /future org.qiweb.runtime.outcomes.FutureOutcomeTest$Controller.future\n"
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
