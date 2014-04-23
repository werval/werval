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
import org.qiweb.test.QiWebTest;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Cache Test.
 *
 * Assert that a Cache Plugin is working as expected.
 * <p>
 * Extends in your Cache Plugin implementations to test it easily.
 */
public abstract class CacheTest
    extends QiWebTest
{
    private static final String FOO = "foo";
    private static final String BAR = "bar";

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
