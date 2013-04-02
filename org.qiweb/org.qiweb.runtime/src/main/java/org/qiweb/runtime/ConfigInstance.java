package org.qiweb.runtime;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import org.qiweb.api.Config;

import static io.netty.util.CharsetUtil.UTF_8;

public class ConfigInstance
    implements Config
{

    private com.typesafe.config.Config config = com.typesafe.config.ConfigFactory.load();

    @Override
    public boolean has( String key )
    {
        return config.hasPath( key );
    }

    @Override
    public Boolean getBoolean( String key )
    {
        return config.getBoolean( key );
    }

    @Override
    public Integer getInteger( String key )
    {
        return config.getInt( key );
    }

    @Override
    public Double getDouble( String key )
    {
        return config.getDouble( key );
    }

    @Override
    public String getString( String key )
    {
        return config.getString( key );
    }

    @Override
    public List<String> getStringList( String key )
    {
        return config.getStringList( key );
    }

    @Override
    public char[] getChars( String key )
    {
        return config.getString( key ).toCharArray();
    }

    @Override
    public byte[] getUtf8Bytes( String key )
    {
        return config.getString( key ).getBytes( UTF_8 );
    }

    @Override
    public URL getURL( String key )
    {
        String urlString = config.getString( key );
        try
        {
            return new URL( urlString );
        }
        catch( MalformedURLException ex )
        {
            throw new com.typesafe.config.ConfigException.WrongType(
                config.origin(), "Malformed URL in config, key: " + key + ", value: " + urlString, ex );
        }
    }

    @Override
    public File getFile( String key )
    {
        return new File( config.getString( key ) );
    }

    @Override
    public Long getMilliseconds( String key )
    {
        return config.getMilliseconds( key );
    }

    public final void refresh()
    {
        config = com.typesafe.config.ConfigFactory.load();
    }
}
