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
    Boolean getBoolean( String key );

    /**
     * @param key Config entry key
     * @return Config entry value as Integer
     */
    Integer getInteger( String key );

    /**
     * @param key Config entry key
     * @return Config entry value as Double
     */
    Double getDouble( String key );

    /**
     * @param key Config entry key
     * @return Config entry value as String
     */
    String getString( String key );

    /**
     * @param key Config entry key
     * @return Config entry values as List of String
     */
    List<String> getStringList( String key );

    /**
     * @param key Config entry key
     * @return Config entry keys and values as Map of String and String
     */
    Map<String, String> getStringMap( String key );

    /**
     * @param key Config entry key
     * @return Config entry value as char[]
     */
    char[] getChars( String key );

    /**
     * @param key Config entry key
     * @return Config entry value as UTF-8 bytes
     */
    byte[] getUtf8Bytes( String key );

    /**
     * @param key Config entry key
     * @return Config entry value as URL
     */
    URL getURL( String key );

    /**
     * @param key Config entry key
     * @return Config entry value as File
     */
    File getFile( String key );

    /**
     * @param key Config entry key
     * @return Config entry value as Long representing seconds
     */
    Long getSeconds( String key );

    /**
     * @param key Config entry key
     * @return Config entry value as Long representing milliseconds
     */
    Long getMilliseconds( String key );
}
