package org.qiweb.api;

import java.io.File;
import java.net.URL;
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
}
