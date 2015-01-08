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
package io.werval.runtime.routes;

import io.werval.api.exceptions.ParameterBinderException;
import io.werval.api.routes.ParameterBinder;
import io.werval.api.routes.ParameterBinders;
import io.werval.runtime.util.TypeResolver;
import java.net.MalformedURLException;
import java.time.DateTimeException;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parameter Binders instance.
 *
 * Include standard binders.
 */
public final class ParameterBindersInstance
    implements ParameterBinders
{
    private abstract static class StrictTypingParameterBinder<T>
        implements ParameterBinder<T>
    {
        @Override
        public final boolean accept( java.lang.Class<?> type )
        {
            return type.equals( TypeResolver.resolveArgument( getClass(), ParameterBinder.class ) );
        }
    }

    /**
     * {@link java.lang.String} Parameter Binder.
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
     * {@link java.lang.Boolean} Parameter Binder.
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
     * {@link java.lang.Short} Parameter Binder.
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
     * {@link java.lang.Integer} Parameter Binder.
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
     * {@link java.lang.Long} Parameter Binder.
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
     * {@link java.lang.Double} Parameter Binder.
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
     * {@link java.lang.Float} Parameter Binder.
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
     * {@link java.math.BigInteger} Parameter Binder.
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
     * {@link java.math.BigDecimal} Parameter Binder.
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
     * {@link java.util.UUID} Parameter Binder.
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
     * {@link java.net.URL} Parameter Binder.
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
     * {@link java.lang.Class} Parameter Binder.
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

    /**
     * {@link java.time.Duration} Parameter Binder.
     */
    public static final class Duration
        extends StrictTypingParameterBinder<java.time.Duration>
    {
        @Override
        public java.time.Duration bind( java.lang.String name, java.lang.String value )
        {
            try
            {
                return java.time.Duration.parse( value );
            }
            catch( DateTimeParseException ex )
            {
                throw new ParameterBinderException( "Invalid parameter '" + name + "' format: '" + value + "'", ex );
            }
        }

        @Override
        public java.lang.String unbind( java.lang.String name, java.time.Duration value )
        {
            return value.toString();
        }
    }

    /**
     * {@link java.time.Period} Parameter Binder.
     */
    public static final class Period
        extends StrictTypingParameterBinder<java.time.Period>
    {
        @Override
        public java.time.Period bind( java.lang.String name, java.lang.String value )
        {
            try
            {
                return java.time.Period.parse( value );
            }
            catch( DateTimeParseException ex )
            {
                throw new ParameterBinderException( "Invalid parameter '" + name + "' format: '" + value + "'", ex );
            }
        }

        @Override
        public java.lang.String unbind( java.lang.String name, java.time.Period value )
        {
            return value.toString();
        }
    }

    /**
     * {@link java.time.Year} Parameter Binder.
     */
    public static final class Year
        extends StrictTypingParameterBinder<java.time.Year>
    {
        @Override
        public java.time.Year bind( java.lang.String name, java.lang.String value )
        {
            try
            {
                return java.time.Year.parse( value );
            }
            catch( DateTimeParseException ex )
            {
                throw new ParameterBinderException( "Invalid parameter '" + name + "' format: '" + value + "'", ex );
            }
        }

        @Override
        public java.lang.String unbind( java.lang.String name, java.time.Year value )
        {
            return value.toString();
        }
    }

    /**
     * {@link java.time.Month} Parameter Binder.
     */
    public static final class Month
        extends StrictTypingParameterBinder<java.time.Month>
    {
        @Override
        public java.time.Month bind( java.lang.String name, java.lang.String value )
        {
            try
            {
                return java.time.Month.of( java.lang.Integer.valueOf( value ) );
            }
            catch( NumberFormatException | DateTimeException ex )
            {
                throw new ParameterBinderException( "Invalid parameter '" + name + "' format: '" + value + "'", ex );
            }
        }

        @Override
        public java.lang.String unbind( java.lang.String name, java.time.Month value )
        {
            return java.lang.String.valueOf( value.getValue() );
        }
    }

    /**
     * {@link java.time.DayOfWeek} Parameter Binder.
     */
    public static final class DayOfWeek
        extends StrictTypingParameterBinder<java.time.DayOfWeek>
    {
        @Override
        public java.time.DayOfWeek bind( java.lang.String name, java.lang.String value )
        {
            try
            {
                return java.time.DayOfWeek.of( java.lang.Integer.valueOf( value ) );
            }
            catch( NumberFormatException | DateTimeException ex )
            {
                throw new ParameterBinderException( "Invalid parameter '" + name + "' format: '" + value + "'", ex );
            }
        }

        @Override
        public java.lang.String unbind( java.lang.String name, java.time.DayOfWeek value )
        {
            return java.lang.String.valueOf( value.getValue() );
        }
    }

    /**
     * {@link java.time.YearMonth} Parameter Binder.
     */
    public static final class YearMonth
        extends StrictTypingParameterBinder<java.time.YearMonth>
    {
        @Override
        public java.time.YearMonth bind( java.lang.String name, java.lang.String value )
        {
            return java.time.YearMonth.parse( value );
        }

        @Override
        public java.lang.String unbind( java.lang.String name, java.time.YearMonth value )
        {
            return value.toString();
        }
    }

    /**
     * {@link java.time.MonthDay} Parameter Binder.
     */
    public static final class MonthDay
        extends StrictTypingParameterBinder<java.time.MonthDay>
    {
        @Override
        public java.time.MonthDay bind( java.lang.String name, java.lang.String value )
        {
            return java.time.MonthDay.parse( value );
        }

        @Override
        public java.lang.String unbind( java.lang.String name, java.time.MonthDay value )
        {
            return value.toString();
        }
    }

    /**
     * {@link java.time.LocalDate} Parameter Binder.
     */
    public static final class LocalDate
        extends StrictTypingParameterBinder<java.time.LocalDate>
    {
        @Override
        public java.time.LocalDate bind( java.lang.String name, java.lang.String value )
        {
            return java.time.LocalDate.parse( value );
        }

        @Override
        public java.lang.String unbind( java.lang.String name, java.time.LocalDate value )
        {
            return value.toString();
        }
    }

    /**
     * {@link java.time.LocalTime} Parameter Binder.
     */
    public static final class LocalTime
        extends StrictTypingParameterBinder<java.time.LocalTime>
    {
        @Override
        public java.time.LocalTime bind( java.lang.String name, java.lang.String value )
        {
            return java.time.LocalTime.parse( value );
        }

        @Override
        public java.lang.String unbind( java.lang.String name, java.time.LocalTime value )
        {
            return value.toString();
        }
    }

    /**
     * {@link java.time.LocalDateTime} Parameter Binder.
     */
    public static final class LocalDateTime
        extends StrictTypingParameterBinder<java.time.LocalDateTime>
    {
        @Override
        public java.time.LocalDateTime bind( java.lang.String name, java.lang.String value )
        {
            return java.time.LocalDateTime.parse( value );
        }

        @Override
        public java.lang.String unbind( java.lang.String name, java.time.LocalDateTime value )
        {
            return value.toString();
        }
    }

    /**
     * {@link java.time.OffsetTime} Parameter Binder.
     */
    public static final class OffsetTime
        extends StrictTypingParameterBinder<java.time.OffsetTime>
    {
        @Override
        public java.time.OffsetTime bind( java.lang.String name, java.lang.String value )
        {
            return java.time.OffsetTime.parse( value );
        }

        @Override
        public java.lang.String unbind( java.lang.String name, java.time.OffsetTime value )
        {
            return value.toString();
        }
    }

    /**
     * {@link java.time.OffsetDateTime} Parameter Binder.
     */
    public static final class OffsetDateTime
        extends StrictTypingParameterBinder<java.time.OffsetDateTime>
    {
        @Override
        public java.time.OffsetDateTime bind( java.lang.String name, java.lang.String value )
        {
            return java.time.OffsetDateTime.parse( value );
        }

        @Override
        public java.lang.String unbind( java.lang.String name, java.time.OffsetDateTime value )
        {
            return value.toString();
        }
    }

    /**
     * {@link java.time.ZonedDateTime} Parameter Binder.
     */
    public static final class ZonedDateTime
        extends StrictTypingParameterBinder<java.time.ZonedDateTime>
    {
        @Override
        public java.time.ZonedDateTime bind( java.lang.String name, java.lang.String value )
        {
            return java.time.ZonedDateTime.parse( value );
        }

        @Override
        public java.lang.String unbind( java.lang.String name, java.time.ZonedDateTime value )
        {
            return value.toString();
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
