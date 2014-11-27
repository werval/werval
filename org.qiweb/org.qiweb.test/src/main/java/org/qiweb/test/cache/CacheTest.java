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
package org.qiweb.test.cache;

import org.junit.Test;
import org.qiweb.api.cache.Cache;
import org.qiweb.api.http.Request;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.filters.Cached;
import org.qiweb.runtime.routes.RoutesParserProvider;
import org.qiweb.runtime.routes.RoutesProvider;
import org.qiweb.test.QiWebTest;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.qiweb.api.context.CurrentContext.outcomes;
import static org.qiweb.api.http.Headers.Names.ETAG;
import static org.qiweb.api.http.Headers.Names.IF_NONE_MATCH;
import static org.qiweb.api.http.Method.GET;

/**
 * Cache Test.
 * <p>
 * Assert that a Cache Plugin and the @{@link Cached} annotation work as expected.
 * <p>
 * Extends in your Cache Plugin implementations to test it easily.
 *
 * @navassoc 1 test * Cache
 */
public abstract class CacheTest
    extends QiWebTest
{
    public static class Controller
    {
        private static int hits = 0;

        @Cached
        public Outcome cached()
        {
            hits++;
            return outcomes().ok( "CACHED" ).build();
        }
    }

    private static final String FOO = "foo";
    private static final String BAR = "bar";

    @Override
    protected RoutesProvider routesProvider()
    {
        return new RoutesParserProvider( "GET /cached org.qiweb.test.cache.CacheTest$Controller.cached" );
    }

    @Test
    public void cachedControllerMethod()
    {
        Controller.hits = 0;

        Request request = newRequestBuilder().method( GET ).uri( "/cached" ).build();

        // Request
        assertThat(
            application().handleRequest( request ).join().responseHeader().status().code(),
            is( 200 )
        );
        assertThat( Controller.hits, is( 1 ) );

        // Assert Server-Side Cache
        Outcome outcome = application().handleRequest( request ).join();
        assertThat(
            outcome.responseHeader().status().code(),
            is( 200 )
        );
        assertThat( Controller.hits, is( 1 ) );

        // Assert Client-Side Cache
        request = newRequestBuilder().method( GET ).uri( "/cached" ).headers(
            singletonMap( IF_NONE_MATCH, singletonList( outcome.responseHeader().headers().singleValue( ETAG ) ) )
        ).build();
        assertThat(
            application().handleRequest( request ).join().responseHeader().status().code(),
            is( 304 )
        );
        assertThat( Controller.hits, is( 1 ) );
    }

    @Test
    public void hasSetHasGetRemoveHas()
    {
        Cache cache = application().cache();
        assertFalse( cache.has( FOO ) );
        cache.set( FOO, BAR );
        assertTrue( cache.has( FOO ) );
        assertThat( cache.get( FOO ), equalTo( BAR ) );
        cache.remove( FOO );
        assertFalse( cache.has( FOO ) );
    }

    @Test
    public void getOptional()
    {
        Cache cache = application().cache();
        assertFalse( cache.getOptional( FOO ).isPresent() );
        cache.set( FOO, BAR );
        assertTrue( cache.getOptional( FOO ).isPresent() );
        assertThat( cache.getOptional( FOO ).get(), equalTo( BAR ) );
        cache.remove( FOO );
        assertFalse( cache.getOptional( FOO ).isPresent() );
    }

    @Test
    public void getOrSetDefault()
    {
        Cache cache = application().cache();
        assertFalse( cache.has( FOO ) );
        assertThat( cache.getOrSetDefault( FOO, BAR ), equalTo( BAR ) );
        assertTrue( cache.has( FOO ) );
        cache.set( FOO, "bazar" );
        assertThat( cache.getOrSetDefault( FOO, BAR ), equalTo( "bazar" ) );
    }

    @Test
    public void setTimeToLive()
        throws InterruptedException
    {
        Cache cache = application().cache();
        assertFalse( cache.has( FOO ) );
        cache.set( 1, FOO, BAR );
        assertThat( cache.get( FOO ), equalTo( BAR ) );
        Thread.sleep( 1100 );
        assertFalse( cache.has( FOO ) );
    }

    @Test
    public void getOrSetDefaultTimeToLive()
        throws InterruptedException
    {
        Cache cache = application().cache();
        assertFalse( cache.has( FOO ) );
        assertThat( cache.getOrSetDefault( FOO, 1, BAR ), equalTo( BAR ) );
        Thread.sleep( 1100 );
        assertFalse( cache.has( FOO ) );
        cache.set( 1, FOO, "bazar" );
        assertThat( cache.getOrSetDefault( FOO, 1, BAR ), equalTo( "bazar" ) );
        Thread.sleep( 1100 );
        assertThat( cache.getOrSetDefault( FOO, BAR ), equalTo( BAR ) );
    }

    @Test
    public void setTimeToLiveZero()
        throws InterruptedException
    {
        Cache cache = application().cache();
        assertFalse( cache.has( FOO ) );
        cache.set( 0, FOO, BAR );
        assertThat( cache.get( FOO ), equalTo( BAR ) );
        Thread.sleep( 1100 );
        assertTrue( cache.has( FOO ) );
    }

    @Test
    public void getOrSetDefaultTimeToZero()
        throws InterruptedException
    {
        Cache cache = application().cache();
        assertFalse( cache.has( FOO ) );
        assertThat( cache.getOrSetDefault( FOO, 0, BAR ), equalTo( BAR ) );
        Thread.sleep( 1100 );
        assertTrue( cache.has( FOO ) );
        cache.set( 0, FOO, "bazar" );
        assertThat( cache.getOrSetDefault( FOO, 0, BAR ), equalTo( "bazar" ) );
        Thread.sleep( 1100 );
        assertThat( cache.get( FOO ), equalTo( "bazar" ) );
    }

    @Test
    public void removeAbsent()
    {
        Cache cache = application().cache();
        assertFalse( cache.has( FOO ) );
        cache.remove( FOO );
    }
}
