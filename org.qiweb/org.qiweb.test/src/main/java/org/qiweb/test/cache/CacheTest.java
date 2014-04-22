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

import java.time.Duration;
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
    @Test
    public void hasSetHasGetRemoveHas()
    {
        Cache cache = application().cache();
        assertFalse( cache.has( "foo" ) );
        cache.set( "foo", "bar" );
        assertTrue( cache.has( "foo" ) );
        assertThat( cache.get( "foo" ), equalTo( "bar" ) );
        cache.remove( "foo" );
        assertFalse( cache.has( "foo" ) );
    }

    @Test
    public void getOptional()
    {
        Cache cache = application().cache();
        assertFalse( cache.getOptional( "foo" ).isPresent() );
        cache.set( "foo", "bar" );
        assertTrue( cache.getOptional( "foo" ).isPresent() );
        assertThat( cache.getOptional( "foo" ).get(), equalTo( "bar" ) );
        cache.remove( "foo" );
        assertFalse( cache.getOptional( "foo" ).isPresent() );
    }

    @Test
    public void getOrSetDefault()
    {
        Cache cache = application().cache();
        assertFalse( cache.has( "foo" ) );
        assertThat( cache.getOrSetDefault( "foo", "bar" ), equalTo( "bar" ) );
        assertTrue( cache.has( "foo" ) );
        cache.set( "foo", "bazar" );
        assertThat( cache.getOrSetDefault( "foo", "bar" ), equalTo( "bazar" ) );
    }

    @Test
    public void setExpirationSeconds()
        throws InterruptedException
    {
        Cache cache = application().cache();
        assertFalse( cache.has( "foo" ) );
        cache.set( 1, "foo", "bar" );
        assertThat( cache.get( "foo" ), equalTo( "bar" ) );
        Thread.sleep( 1100 );
        assertFalse( cache.has( "foo" ) );
    }

    @Test
    public void setExpirationDuration()
        throws InterruptedException
    {
        Cache cache = application().cache();
        assertFalse( cache.has( "foo" ) );
        cache.set( Duration.ofSeconds( 1 ), "foo", "bar" );
        assertThat( cache.get( "foo" ), equalTo( "bar" ) );
        Thread.sleep( 1100 );
        assertFalse( cache.has( "foo" ) );
    }

    @Test
    public void getOrSetDefaultExpiration()
        throws InterruptedException
    {
        Cache cache = application().cache();
        assertFalse( cache.has( "foo" ) );
        assertThat( cache.getOrSetDefault( "foo", Duration.ofSeconds( 1 ), "bar" ), equalTo( "bar" ) );
        Thread.sleep( 1100 );
        assertFalse( cache.has( "foo" ) );
    }

    @Test
    public void removeAbsent()
    {
        Cache cache = application().cache();
        assertFalse( cache.has( "foo" ) );
        cache.remove( "foo" );
    }
}
