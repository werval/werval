/*
 * Copyright (c) 2013-2015 the original author or authors
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
package io.werval.runtime.filters;

import io.werval.api.context.Context;
import io.werval.api.filters.FilterChain;
import io.werval.api.filters.FilterWith;
import io.werval.api.outcomes.Outcome;
import io.werval.runtime.routes.RoutesParserProvider;
import io.werval.test.WervalHttpRule;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.ClassRule;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.expect;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Filters Test.
 */
public class FiltersTest
{
    @FilterWith( ControllerAnnotationOne.Filter.class )
    @Target(
                {
            ElementType.TYPE, ElementType.METHOD
        } )
    @Retention( RetentionPolicy.RUNTIME )
    @Inherited
    public static @interface ControllerAnnotationOne
    {
        public static class Filter
            implements io.werval.api.filters.Filter<ControllerAnnotationOne>
        {
            @Override
            public CompletableFuture<Outcome> filter( FilterChain chain, Context context, Optional<ControllerAnnotationOne> annotation )
            {
                EVENTS.add( "ControllerAnnotationOne" );
                return chain.next( context );
            }
        }
    }

    @ControllerAnnotationOne
    @FilterWith( ControllerAnnotationTwo.Filter.class )
    @Target(
                {
            ElementType.TYPE, ElementType.METHOD
        } )
    @Retention( RetentionPolicy.RUNTIME )
    @Inherited
    public static @interface ControllerAnnotationTwo
    {
        public static class Filter
            implements io.werval.api.filters.Filter<ControllerAnnotationTwo>
        {
            @Override
            public CompletableFuture<Outcome> filter( FilterChain chain, Context context, Optional<ControllerAnnotationTwo> annotation )
            {
                EVENTS.add( "ControllerAnnotationTwo" );
                return chain.next( context );
            }
        }
    }

    public static class ControllerFilterOne
        implements io.werval.api.filters.Filter<Void>
    {
        @Override
        public CompletableFuture<Outcome> filter( FilterChain chain, Context context, Optional<Void> annotation )
        {
            EVENTS.add( "ControllerFilterOne" );
            return chain.next( context );
        }
    }

    public static class ControllerFilterTwo
        implements io.werval.api.filters.Filter<Void>
    {
        @Override
        public CompletableFuture<Outcome> filter( FilterChain chain, Context context, Optional<Void> annotation )
        {
            EVENTS.add( "ControllerFilterTwo" );
            return chain.next( context );
        }
    }

    @FilterWith( MethodAnnotationOne.Filter.class )
    @Target(
                {
            ElementType.TYPE, ElementType.METHOD
        } )
    @Retention( RetentionPolicy.RUNTIME )
    @Inherited
    public static @interface MethodAnnotationOne
    {
        public static class Filter
            implements io.werval.api.filters.Filter<MethodAnnotationOne>
        {
            @Override
            public CompletableFuture<Outcome> filter( FilterChain chain, Context context, Optional<MethodAnnotationOne> annotation )
            {
                EVENTS.add( "MethodAnnotationOne" );
                return chain.next( context );
            }
        }
    }

    @MethodAnnotationOne
    @FilterWith( MethodAnnotationTwo.Filter.class )
    @Target(
                {
            ElementType.TYPE, ElementType.METHOD
        } )
    @Retention( RetentionPolicy.RUNTIME )
    @Inherited
    @Repeatable( MethodAnnotationTwo.Repeat.class )
    public static @interface MethodAnnotationTwo
    {
        String discriminator() default "default";

        public static class Filter
            implements io.werval.api.filters.Filter<MethodAnnotationTwo>
        {
            @Override
            public CompletableFuture<Outcome> filter( FilterChain chain, Context context, Optional<MethodAnnotationTwo> annotation )
            {
                EVENTS.add( "MethodAnnotationTwo" );
                return chain.next( context );
            }
        }

        @Target(
                        {
                ElementType.TYPE, ElementType.METHOD
            } )
        @Retention( RetentionPolicy.RUNTIME )
        @Inherited
        public static @interface Repeat
        {
            MethodAnnotationTwo[] value();
        }
    }

    public static class MethodFilterOne
        implements io.werval.api.filters.Filter<Void>
    {
        @Override
        public CompletableFuture<Outcome> filter( FilterChain chain, Context context, Optional<Void> annotation )
        {
            EVENTS.add( "MethodFilterOne" );
            return chain.next( context );
        }
    }

    public static class MethodFilterTwo
        implements io.werval.api.filters.Filter<Void>
    {
        @Override
        public CompletableFuture<Outcome> filter( FilterChain chain, Context context, Optional<Void> annotation )
        {
            EVENTS.add( "MethodFilterTwo" );
            context.response().headers().with( "X-Werval-Filtered", "true" );
            return chain.next( context );
        }
    }

    @ControllerAnnotationTwo
    @FilterWith(
                {
            ControllerFilterOne.class, ControllerFilterTwo.class
        } )
    public static class Controller
    {
        @FilterWith(
                        {
                MethodFilterOne.class, MethodFilterTwo.class
            } )
        @MethodAnnotationTwo
        @MethodAnnotationTwo( discriminator = "another one" )
        public Outcome filtered()
        {
            return io.werval.api.context.CurrentContext.outcomes().ok().build();
        }
    }

    @ClassRule
    public static final WervalHttpRule WERVAL = new WervalHttpRule(
        new RoutesParserProvider( "GET / io.werval.runtime.filters.FiltersTest$Controller.filtered" )
    );

    private static final List<String> EVENTS = new ArrayList<>();

    @Test
    public void testFilters()
        throws Exception
    {
        EVENTS.clear();
        expect().
            statusCode( 200 ).
            header( "X-Werval-Filtered", equalTo( "true" ) ).
            when().
            get( "/" );
        System.out.println( "-------------------" );
        System.out.println( EVENTS );
        System.out.println( "-------------------" );
        assertThat(
            EVENTS,
            contains(
                "ControllerAnnotationOne", "ControllerAnnotationTwo",
                "ControllerFilterOne", "ControllerFilterTwo",
                "MethodFilterOne", "MethodFilterTwo",
                "MethodAnnotationOne", "MethodAnnotationTwo",
                "MethodAnnotationOne", "MethodAnnotationTwo"
            )
        );
    }
}
