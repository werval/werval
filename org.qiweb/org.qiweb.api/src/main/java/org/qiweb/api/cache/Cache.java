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
package org.qiweb.api.cache;

import java.time.Duration;
import java.util.Optional;

/**
 * Cache.
 */
public interface Cache
{
    /**
     * Check if the Cache has an object for a given key.
     *
     * @param key Cache Key
     *
     * @return {@literal true} if the cache has an object for the given key
     */
    boolean has( String key );

    /**
     * Fetch cached object for a given key.
     *
     * @param <T> Object Type
     * @param key Cache Key
     *
     * @return The cached object for the given key, or {@literal null} if absent
     */
    <T> T get( String key );

    /**
     * Optionaly fetch cached object for a given key.
     *
     * @param <T> Object Type
     * @param key Cache Key
     *
     * @return An Optional of the cached object for the given key
     */
    <T> Optional<T> getOptional( String key );

    /**
     * Fetch cached object for a given key or set a non-expiring default value.
     *
     * If the cache has a non-expired object for the given key, it is returned.
     * <p>
     * Otherwise, the given default value is set in the cache and returned.
     *
     * @param <T>          Object Type
     * @param key          Cache Key
     * @param defaultValue Default Value
     *
     * @return The existing cached object for the given key, or the given default value, never return {@literal null}
     */
    <T> T getOrSetDefault( String key, T defaultValue );

    /**
     * Fetch cached object for a given key or set an expiring default value.
     *
     * If the cache has a non-expired object for the given key, it is returned.
     * <p>
     * Otherwise, the given default value is set in the cache and returned.
     *
     * @param <T>          Object Type
     * @param key          Cache Key
     * @param ttl          Default Value Time To Live Duration, only seconds will be used, not nano nor milliseconds
     * @param defaultValue Default Value
     *
     * @return The existing cached object for the given key, or the given default value, never return {@literal null}
     */
    <T> T getOrSetDefault( String key, Duration ttl, T defaultValue );

    /**
     * Set a non-expiring object for a given key in the Cache.
     *
     * @param <T>   Object Type
     * @param key   Cache Key
     * @param value Value Object
     */
    <T> void set( String key, T value );

    /**
     * Set an expiring object for a given key in the Cache.
     *
     * @param <T>        Object Type
     * @param ttlSeconds Time To Live in Seconds
     * @param key        Cache Key
     * @param value      Value Object
     */
    <T> void set( int ttlSeconds, String key, T value );

    /**
     * Set an expiring object for a given key in the Cache.
     *
     * @param <T>   Object Type
     * @param ttl   Time To Live Duration, only seconds will be used, not nano nor milliseconds
     * @param key   Cache Key
     * @param value Value Object
     */
    <T> void set( Duration ttl, String key, T value );

    /**
     * Remove a Cache entry.
     *
     * @param key Key of the entry to remove
     */
    void remove( String key );
}
