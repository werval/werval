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
package org.qiweb.runtime.routes;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import org.qiweb.api.exceptions.ParameterBinderException;
import org.qiweb.api.routes.ParameterBinder;
import org.qiweb.api.routes.ParameterBinders;
import org.qiweb.runtime.util.TypeResolver;

/**
 * Parameter Binders instance.
 */
public final class ParameterBindersInstance
    implements ParameterBinders
{
    /* package */ abstract static class StrictTypingParameterBinder<T>
        implements ParameterBinder<T>
    {
        static
        {
            // TODO Only disable TypeResolver cache when in DEV mode
            TypeResolver.disableCache();
        }

        @Override
        public final boolean accept( java.lang.Class<?> type )
        {
            return type.equals( TypeResolver.resolveArgument( getClass(), ParameterBinder.class ) );
        }
    }

    /**
     * String Parameter Binder.
     */
    public static final class String
        extends StrictTypingParameterBinder<java.lang.String>
    {
        @Override
        public java.lang.String bind( java.lang.String name, java.lang.String value )
        {
            return value;
        }

        @Override
        public java.lang.String unbind( java.lang.String name, java.lang.String value )
        {
            return value;
        }
    }

    /**
     * Boolean Parameter Binder.
     */
    public static final class Boolean
        extends StrictTypingParameterBinder<java.lang.Boolean>
    {
        @Override
        public java.lang.Boolean bind( java.lang.String name, java.lang.String value )
        {
            return java.lang.Boolean.valueOf( value );
        }

        @Override
        public java.lang.String unbind( java.lang.String name, java.lang.Boolean value )
        {
            return java.lang.String.valueOf( value );
        }
    }

    /**
     * Short Parameter Binder.
     */
    public static final class Short
        extends StrictTypingParameterBinder<java.lang.Short>
    {
        @Override
        public java.lang.Short bind( java.lang.String name, java.lang.String value )
        {
            try
            {
                return java.lang.Short.valueOf( value );
            }
            catch( NumberFormatException ex )
            {
                throw new ParameterBinderException( "Invalid parameter '" + name + "' format: '" + value + "'", ex );
            }
        }

        @Override
        public java.lang.String unbind( java.lang.String name, java.lang.Short value )
        {
            return java.lang.String.valueOf( value );
        }
    }

    /**
     * Integer Parameter Binder.
     */
    public static final class Integer
        extends StrictTypingParameterBinder<java.lang.Integer>
    {
        @Override
        public java.lang.Integer bind( java.lang.String name, java.lang.String value )
        {
            try
            {
                return java.lang.Integer.valueOf( value );
            }
            catch( NumberFormatException ex )
            {
                throw new ParameterBinderException( "Invalid parameter '" + name + "' format: '" + value + "'", ex );
            }
        }

        @Override
        public java.lang.String unbind( java.lang.String name, java.lang.Integer value )
        {
            return java.lang.String.valueOf( value );
        }
    }

    /**
     * Long Parameter Binder.
     */
    public static final class Long
        extends StrictTypingParameterBinder<java.lang.Long>
    {
        @Override
        public java.lang.Long bind( java.lang.String name, java.lang.String value )
        {
            try
            {
                return java.lang.Long.valueOf( value );
            }
            catch( NumberFormatException ex )
            {
                throw new ParameterBinderException( "Invalid parameter '" + name + "' format: '" + value + "'", ex );
            }
        }

        @Override
        public java.lang.String unbind( java.lang.String name, java.lang.Long value )
        {
            return java.lang.String.valueOf( value );
        }
    }

    /**
     * Double Parameter Binder.
     */
    public static final class Double
        extends StrictTypingParameterBinder<java.lang.Double>
    {
        @Override
        public java.lang.Double bind( java.lang.String name, java.lang.String value )
        {
            try
            {
                return java.lang.Double.valueOf( value );
            }
            catch( NumberFormatException ex )
            {
                throw new ParameterBinderException( "Invalid parameter '" + name + "' format: '" + value + "'", ex );
            }
        }

        @Override
        public java.lang.String unbind( java.lang.String name, java.lang.Double value )
        {
            return java.lang.String.valueOf( value );
        }
    }

    /**
     * Float Parameter Binder.
     */
    public static final class Float
        extends StrictTypingParameterBinder<java.lang.Float>
    {
        @Override
        public java.lang.Float bind( java.lang.String name, java.lang.String value )
        {
            try
            {
                return java.lang.Float.valueOf( value );
            }
            catch( NumberFormatException ex )
            {
                throw new ParameterBinderException( "Invalid parameter '" + name + "' format: '" + value + "'", ex );
            }
        }

        @Override
        public java.lang.String unbind( java.lang.String name, java.lang.Float value )
        {
            return java.lang.String.valueOf( value );
        }
    }

    /**
     * BigInteger Parameter Binder.
     */
    public static final class BigInteger
        extends StrictTypingParameterBinder<java.math.BigInteger>
    {
        @Override
        public java.math.BigInteger bind( java.lang.String name, java.lang.String value )
        {
            try
            {
                return new java.math.BigInteger( value );
            }
            catch( NumberFormatException ex )
            {
                throw new ParameterBinderException( "Invalid parameter '" + name + "' format: '" + value + "'", ex );
            }
        }

        @Override
        public java.lang.String unbind( java.lang.String name, java.math.BigInteger value )
        {
            return value.toString();
        }
    }

    /**
     * BigDecimal Parameter Binder.
     */
    public static final class BigDecimal
        extends StrictTypingParameterBinder<java.math.BigDecimal>
    {
        @Override
        public java.math.BigDecimal bind( java.lang.String name, java.lang.String value )
        {
            try
            {
                return new java.math.BigDecimal( value );
            }
            catch( NumberFormatException ex )
            {
                throw new ParameterBinderException( "Invalid parameter '" + name + "' format: '" + value + "'", ex );
            }
        }

        @Override
        public java.lang.String unbind( java.lang.String name, java.math.BigDecimal value )
        {
            return value.toString();
        }
    }

    /**
     * UUID Parameter Binder.
     */
    public static final class UUID
        extends StrictTypingParameterBinder<java.util.UUID>
    {
        @Override
        public java.util.UUID bind( java.lang.String name, java.lang.String value )
        {
            try
            {
                return java.util.UUID.fromString( value );
            }
            catch( IllegalArgumentException ex )
            {
                throw new ParameterBinderException( "Invalid parameter '" + name + "' format: '" + value + "'", ex );
            }
        }

        @Override
        public java.lang.String unbind( java.lang.String name, java.util.UUID value )
        {
            return value.toString();
        }
    }

    /**
     * URL Parameter Binder.
     */
    public static final class URL
        extends StrictTypingParameterBinder<java.net.URL>
    {
        @Override
        public java.net.URL bind( java.lang.String name, java.lang.String value )
        {
            try
            {
                return new java.net.URL( value );
            }
            catch( MalformedURLException ex )
            {
                throw new ParameterBinderException( "Invalid parameter '" + name + "' format: '" + value + "'", ex );
            }
        }

        @Override
        public java.lang.String unbind( java.lang.String name, java.net.URL value )
        {
            return value.toString();
        }
    }

    /**
     * Class Parameter Binder.
     */
    public static final class Class
        extends StrictTypingParameterBinder<java.lang.Class<?>>
    {
        @Override
        public java.lang.Class<?> bind( java.lang.String name, java.lang.String value )
        {
            try
            {
                return java.lang.Class.forName( value );
            }
            catch( ClassNotFoundException ex )
            {
                throw new ParameterBinderException( "Invalid parameter '" + name + "' format: '" + value + "'", ex );
            }
        }

        @Override
        public java.lang.String unbind( java.lang.String name, java.lang.Class<?> value )
        {
            return value.getCanonicalName();
        }
    }

    private final List<ParameterBinder<?>> parameterBinders = new ArrayList<>();

    public ParameterBindersInstance()
    {
    }

    public ParameterBindersInstance( List<ParameterBinder<?>> parameterBinders )
    {
        this.parameterBinders.addAll( parameterBinders );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T> T bind( java.lang.Class<T> type, java.lang.String paramName, java.lang.String paramValue )
    {
        for( ParameterBinder<?> parameterBinder : parameterBinders )
        {
            if( parameterBinder.accept( type ) )
            {
                return (T) parameterBinder.bind( paramName, paramValue );
            }
        }
        throw new ParameterBinderException( "No ParameterBinder found for type: " + type );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T> java.lang.String unbind( java.lang.Class<T> type, java.lang.String paramName, T paramValue )
    {
        for( ParameterBinder<?> parameterBinder : parameterBinders )
        {
            if( parameterBinder.accept( type ) )
            {
                return ( (ParameterBinder<T>) parameterBinder ).unbind( paramName, paramValue );
            }
        }
        throw new ParameterBinderException( "No ParameterBinder found for type: " + type );
    }
}
