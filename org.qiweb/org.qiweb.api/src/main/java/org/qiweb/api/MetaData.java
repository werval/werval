package org.qiweb.api;

import java.util.LinkedHashMap;

public final class MetaData
    extends LinkedHashMap<String, Object>
{

    public <T> T get( Class<T> type, String key )
    {
        Object value = get( key );
        if( value == null )
        {
            return null;
        }
        return type.cast( value );
    }
}
