/*
 * Copyright (c) 2013-2014 the original author or authors
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
package org.qiweb.api.routes;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Route Controller Params.
 * <p>
 * Used internally by {@link RouteBuilder}.
 */
public final class ControllerParams
    implements Iterable<ControllerParams.Param>
{
    public static final ControllerParams EMPTY = new ControllerParams();
    private final Map<String, Param> params = new LinkedHashMap<>();

    private ControllerParams()
    {
    }

    public ControllerParams( Map<String, Param> params )
    {
        this.params.putAll( params );
    }

    @Override
    public Iterator<Param> iterator()
    {
        return params.values().iterator();
    }

    /**
     * @param name Name of the ControllerParam
     *
     * @return The ControllerParam or null if absent
     */
    public Param get( String name )
    {
        return params.get( name );
    }

    /**
     * @return The names of all ControllerParam in method order
     */
    public Iterable<String> names()
    {
        return params.keySet();
    }

    /**
     * @return The types of all ControllerParams in method order
     */
    public Class<?>[] types()
    {
        Class<?>[] types = new Class<?>[ params.size() ];
        int idx = 0;
        for( Param param : params.values() )
        {
            types[idx] = param.type();
            idx++;
        }
        return types;
    }

    public Map<String, Param> asMap()
    {
        return Collections.unmodifiableMap( params );
    }

    /**
     * Route Controller Param Value Kind.
     */
    public static enum ParamValue
    {
        FORCED, DEFAULTED, NONE
    }

    /**
     * Route Controller Param.
     */
    public static final class Param
    {
        private final String name;
        private final Class<?> type;
        private final ParamValue valueKind;
        private final Object value;

        public Param( String name, Class<?> type )
        {
            this.name = name;
            this.type = type;
            this.valueKind = ParamValue.NONE;
            this.value = null;
        }

        public Param( String name, Class<?> type, ParamValue valueKind, Object value )
        {
            this.name = name;
            this.type = type;
            this.valueKind = valueKind;
            this.value = value;
        }

        /**
         * @return Name of the Controller Param
         */
        public String name()
        {
            return name;
        }

        /**
         * @return Type of the Controller Param
         */
        public Class<?> type()
        {
            return type;
        }

        /**
         * @return The Param Value Kind
         */
        public ParamValue valueKind()
        {
            return valueKind;
        }

        /**
         * @return Forced value of the Controller Param
         *
         * @throws IllegalStateException if no forced value
         */
        public Object forcedValue()
        {
            if( ParamValue.FORCED != valueKind )
            {
                throw new IllegalStateException( "ControllerParam " + name + " has no forced value!" );
            }
            return value;
        }

        /**
         * @return Defaulted value of the Controller Param
         *
         * @throws IllegalStateException if no defaulted value
         */
        public Object defaultedValue()
        {
            if( ParamValue.DEFAULTED != valueKind )
            {
                throw new IllegalStateException( "ControllerParam " + name + " has no defaulted value!" );
            }
            return value;
        }

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder( "Param{name=" );
            sb.append( name ).append( ", type=" ).append( type );
            switch( valueKind )
            {
                case FORCED:
                    sb.append( ", forcedValue=" ).append( value );
                    break;
                case DEFAULTED:
                    sb.append( ", defaultedValue=" ).append( value );
                    break;
                default:
            }
            sb.append( '}' );
            return sb.toString();
        }
    }
}
