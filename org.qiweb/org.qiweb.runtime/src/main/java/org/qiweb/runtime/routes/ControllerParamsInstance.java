package org.qiweb.runtime.routes;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.qi4j.functional.Function;
import org.qiweb.api.routes.ControllerParams;

import static org.qi4j.functional.Iterables.*;

/**
 * Route Controller Params instance.
 */
public class ControllerParamsInstance
    implements ControllerParams
{

    private final Map<String, ControllerParam> params;

    public ControllerParamsInstance()
    {
        this.params = new LinkedHashMap<>();
    }

    public ControllerParamsInstance( Map<String, ControllerParam> params )
    {
        this();
        this.params.putAll( params );
    }

    @Override
    public Iterator<ControllerParam> iterator()
    {
        return params.values().iterator();
    }

    @Override
    public ControllerParam get( String name )
    {
        return params.get( name );
    }

    @Override
    public Iterable<String> names()
    {
        return params.keySet();
    }

    @Override
    public Class<?>[] types()
    {
        return toList( map( new Function<ControllerParam, Class<?>>()
        {
            @Override
            public Class<?> map( ControllerParam param )
            {
                return param.type();
            }
        }, params.values() ) ).toArray( new Class<?>[ params.size() ] );
    }

    public Map<String, ControllerParam> asMap()
    {
        return Collections.unmodifiableMap( params );
    }

    public static class ControllerParamInstance
        implements ControllerParam
    {

        private final String name;
        private final Class<?> type;
        private final boolean hasForcedValue;
        private final Object forcedValue;

        public ControllerParamInstance( String name, Class<?> type )
        {
            this.name = name;
            this.type = type;
            this.hasForcedValue = false;
            this.forcedValue = null;
        }

        public ControllerParamInstance( String name, Class<?> type, Object forcedValue )
        {
            this.name = name;
            this.type = type;
            this.hasForcedValue = true;
            this.forcedValue = forcedValue;
        }

        @Override
        public String name()
        {
            return name;
        }

        @Override
        public Class<?> type()
        {
            return type;
        }

        @Override
        public boolean hasForcedValue()
        {
            return hasForcedValue;
        }

        @Override
        public Object forcedValue()
        {
            if( !hasForcedValue )
            {
                throw new IllegalStateException( "ControllerParam " + name + " has no forced value!" );
            }
            return forcedValue;
        }
    }
}
