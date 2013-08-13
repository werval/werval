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
package org.qiweb.runtime.routes;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.qi4j.functional.Function;

import static org.qi4j.functional.Iterables.map;
import static org.qi4j.functional.Iterables.toList;

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
