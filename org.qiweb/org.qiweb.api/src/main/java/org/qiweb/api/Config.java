/**
 * Copyright (c) 2013 the original author or authors
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
package org.qiweb.api;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * Application Config.
 * <p>All getters never return null. They will throw a non checked exception depending on the error.</p>
 */
public interface Config
{

    /**
     * @param key Config entry key
     * @return TRUE if the key is present, otherwise return FALSE
     */
    boolean has( String key );

    /**
     * @param key Config entry key
     * @return Config entry value as Boolean
     */
    Boolean bool( String key );

    /**
     * @param key Config entry key
     * @return Config entry value as Integer
     */
    Integer intNumber( String key );

    /**
     * @param key Config entry key
     * @return Config entry value as Double
     */
    Double doubleNumber( String key );

    /**
     * @param key Config entry key
     * @return Config entry value as String
     */
    String string( String key );

    /**
     * @param key Config entry key
     * @return Config entry values as List of String
     */
    List<String> stringList( String key );

    /**
     * @param key Config entry key
     * @return Config entry keys and values as Map of String and String
     */
    Map<String, String> stringMap( String key );

    /**
     * @param key Config entry key
     * @return Config entry value as char[]
     */
    char[] chars( String key );

    /**
     * @param key Config entry key
     * @return Config entry value as UTF-8 bytes
     */
    byte[] utf8Bytes( String key );

    /**
     * @param key Config entry key
     * @return Config entry value as Charset
     */
    Charset charset( String key );

    /**
     * @param key Config entry key
     * @return Config entry value as URL
     */
    URL url( String key );

    /**
     * @param key Config entry key
     * @return Config entry value as File
     */
    File file( String key );

    /**
     * @param key Config entry key
     * @return Config entry value as Long representing seconds
     */
    Long seconds( String key );

    /**
     * @param key Config entry key
     * @return Config entry value as Long representing milliseconds
     */
    Long milliseconds( String key );

    /**
     * Render resolved configuration as JSON.
     * @return Resolved configuration as a JSON String.
     */
    @Override
    String toString();
}
