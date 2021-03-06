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
package io.werval.api;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import io.werval.util.Reflectively;

/**
 * Application Config.
 * <p>
 * All getters never return null.
 * They will throw a non checked exception depending on the error.
 * <p>
 * All getters have an <em>Optional</em> counterpart, suffixed {@literal Optional} that do not throw and return an
 * {@link Optional} that is empty if the config is missing or of the wrong type.
 */
@Reflectively.Loaded( by = "DevShell" )
public interface Config
{
    /**
     * @param key Config entry key
     *
     * @return TRUE if the key is present, otherwise return FALSE
     */
    boolean has( String key );

    /**
     * @param key config entry key
     *
     * @return {@literal true} if the config property under the given key is an atPath
     */
    boolean isObject( String key );

    /**
     * @param key config entry key
     *
     * @return {@literal true} if the config property under the given key is an array
     */
    boolean isArray( String key );

    /**
     * @param key Config entry key
     *
     * @return new Config object for the config properties under the given key
     */
    Config atKey( String key );

    /**
     * @param key Config entry key
     *
     * @return new Config object for the config properties under the given key
     */
    Optional<Config> atKeyOptional( String key );

    /**
     * @param path Config entry path
     *
     * @return new Config object for the config properties under the given path
     */
    Config atPath( String path );

    /**
     * @param path Config entry path
     *
     * @return new Config object for the config properties under the given path
     */
    Optional<Config> atPathOptional( String path );

    /**
     * @param key Config entry key
     *
     * @return the list of Config objects for the config properties under the given key
     */
    List<Config> array( String key );

    /**
     * @param key Config entry key
     *
     * @return the list of Config objects for the config properties under the given key
     */
    Optional<List<Config>> arrayOptional( String key );

    /**
     * @return the set of direct subkeys of the current Config
     */
    Set<String> subKeys();

    /**
     * @param key Config entry key
     *
     * @return Config entry value as Boolean
     */
    Boolean bool( String key );

    /**
     * @param key Config entry key
     *
     * @return Config entry value as Boolean
     */
    Optional<Boolean> boolOptional( String key );

    /**
     * @param key Config entry key
     *
     * @return Config entry value as Integer
     */
    @Reflectively.Invoked( by = "DevShell" )
    Integer intNumber( String key );

    /**
     * @param key Config entry key
     *
     * @return Config entry value as Integer
     */
    Optional<Integer> intOptional( String key );

    /**
     * @param key Config entry key
     *
     * @return Config entry value as Long
     */
    Long longNumber( String key );

    /**
     * @param key Config entry key
     *
     * @return Config entry value as Long
     */
    Optional<Long> longOptional( String key );

    /**
     * @param key Config entry key
     *
     * @return Config entry value as Double
     */
    Double doubleNumber( String key );

    /**
     * @param key Config entry key
     *
     * @return Config entry value as Double
     */
    Optional<Double> doubleOptional( String key );

    /**
     * @param key Config entry key
     *
     * @return Config entry value as String
     */
    @Reflectively.Invoked( by = "DevShell" )
    String string( String key );

    /**
     * @param key Config entry key
     *
     * @return Config entry value as String
     */
    Optional<String> stringOptional( String key );

    /**
     * @param key config entry key
     *
     * @return {@literal true} if the config property under the given key is a list
     */
    boolean isList( String key );

    /**
     * @param key Config entry key
     *
     * @return Config entry values as List of Booleans
     */
    List<Boolean> boolList( String key );

    /**
     * @param key Config entry key
     *
     * @return Config entry values as List of Booleans
     */
    Optional<List<Boolean>> boolListOptional( String key );

    /**
     * @param key Config entry key
     *
     * @return Config entry values as List of Integers
     */
    List<Integer> intList( String key );

    /**
     * @param key Config entry key
     *
     * @return Config entry values as List of Integers
     */
    Optional<List<Integer>> intListOptional( String key );

    /**
     * @param key Config entry key
     *
     * @return Config entry values as List of Doubles
     */
    List<Double> doubleList( String key );

    /**
     * @param key Config entry key
     *
     * @return Config entry values as List of Doubles
     */
    Optional<List<Double>> doubleListOptional( String key );

    /**
     * @param key Config entry key
     *
     * @return Config entry values as List of String
     */
    List<String> stringList( String key );

    /**
     * @param key Config entry key
     *
     * @return Config entry values as List of String
     */
    Optional<List<String>> stringListOptional( String key );

    /**
     * @param key Config entry key
     *
     * @return Config entry keys and values as Map of String and String
     */
    Map<String, String> stringMap( String key );

    /**
     * @param key Config entry key
     *
     * @return Config entry keys and values as Map of String and String
     */
    Optional<Map<String, String>> stringMapOptional( String key );

    /**
     * @param key Config entry key
     *
     * @return Config entry value as char[]
     */
    char[] chars( String key );

    /**
     * @param key Config entry key
     *
     * @return Config entry value as char[]
     */
    Optional<char[]> charsOptional( String key );

    /**
     * @param key Config entry key
     *
     * @return Config entry value as UTF-8 bytes
     */
    byte[] utf8Bytes( String key );

    /**
     * @param key Config entry key
     *
     * @return Config entry value as UTF-8 bytes
     */
    Optional<byte[]> utf8BytesOptional( String key );

    /**
     * @param key Config entry key
     *
     * @return Config entry value as Charset
     */
    Charset charset( String key );

    /**
     * @param key Config entry key
     *
     * @return Config entry value as Charset
     */
    Optional<Charset> charsetOptional( String key );

    /**
     * @param key Config entry key
     *
     * @return Config entry value as URL
     */
    URL url( String key );

    /**
     * @param key Config entry key
     *
     * @return Config entry value as URL
     */
    Optional<URL> urlOptional( String key );

    /**
     * @param key Config entry key
     *
     * @return Config entry value as File
     */
    File file( String key );

    /**
     * @param key Config entry key
     *
     * @return Config entry value as File
     */
    Optional<File> fileOptional( String key );

    /**
     * @param key Config entry key
     *
     * @return Config entry value as Long representing seconds
     */
    Long seconds( String key );

    /**
     * @param key Config entry key
     *
     * @return Config entry value as Long representing seconds
     */
    Optional<Long> secondsOptional( String key );

    /**
     * @param key Config entry key
     *
     * @return Config entry value as Long representing milliseconds
     */
    Long milliseconds( String key );

    /**
     * @param key Config entry key
     *
     * @return Config entry value as Long representing milliseconds
     */
    Optional<Long> millisecondsOptional( String key );

    /**
     * Render resolved configuration as JSON.
     *
     * @return Resolved configuration as a JSON String.
     */
    @Override
    String toString();
}
