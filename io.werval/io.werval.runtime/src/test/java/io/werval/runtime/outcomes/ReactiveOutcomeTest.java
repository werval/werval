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
import io.werval.util.ByteArrayByteSource;
import io.werval.util.ByteSource;
import java.util.Arrays;
import java.util.List;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import rx.Observable;

import static com.jayway.restassured.RestAssured.when;
import static io.werval.api.context.CurrentContext.executor;
import static io.werval.api.context.CurrentContext.outcomes;
import static org.hamcrest.Matchers.containsString;
import static rx.RxReactiveStreams.toPublisher;

/**
 * ReactiveOutcomeTest.
 */
public class ReactiveOutcomeTest
{
    public static class Controller
    {
        private static final List<ByteSource> ITEMS = Arrays.asList(
            ByteArrayByteSource.of( "foo" ),
            ByteArrayByteSource.of( "bar" )
        );

        public Outcome rs()
        {
            return outcomes().ok()
                .withBody(
                    subscriber ->
                    {
                        new Thread(
                            () ->
                            {
                                try
                                {
                                    ITEMS.stream().forEach( item -> subscriber.onNext( item ) );
                                    subscriber.onComplete();
                                }
                                catch( Exception ex )
                                {
                                    subscriber.onError( ex );
                                }
                            }
                        ).start();
                    }
                )
                .build();
        }

        public Outcome rx()
        {
            return outcomes().ok()
                .withBody( toPublisher( Observable.from( ITEMS ) ) )
                .build();
        }

        public Outcome rs_context()
        {
            return outcomes()
                .ok()
                .withBody(
                    subscriber ->
                    {
                        // No more context as application has already closed the interaction
                        // So we can't use werval executors!
                        //
                        // BTW, I think it's time we split context in InteractionContext and ApplicationContext
                        // For explicitness and readability
                        // Not sure how exactly yet
                        executor().execute(
                            () ->
                            {
                                try
                                {
                                    ITEMS.stream().forEach( item -> subscriber.onNext( item ) );
                                    subscriber.onComplete();
                                }
                                catch( Exception ex )
                                {
                                    subscriber.onError( ex );
                                }
                            }
                        );
                    }
                )
                .build();
        }
    }

    @ClassRule
    public static final WervalHttpRule WERVAL = new WervalHttpRule( new RoutesParserProvider(
        "GET /rx io.werval.runtime.outcomes.ReactiveOutcomeTest$Controller.rx\n"
        + "GET /rs io.werval.runtime.outcomes.ReactiveOutcomeTest$Controller.rs\n"
        + "GET /rs-context io.werval.runtime.outcomes.ReactiveOutcomeTest$Controller.rs_context\n"
    ) );

    @Test
    public void rx()
    {
        when()
            .get( "/rx" )
            .then().statusCode( 200 )
            .and().body( containsString( "bar" ) );
    }

    @Test
    @Ignore( "FAIL NOT ALWAYS ..." )
    public void rs()
    {
        when()
            .get( "/rs" )
            .then().statusCode( 200 )
            .and().body( containsString( "bar" ) );
    }

    @Test
    @Ignore( "FAIL" )
    public void rs_context()
    {
        when()
            .get( "/rs-context" )
            .then().statusCode( 200 )
            .and().body( containsString( "bar" ) );
    }
}
