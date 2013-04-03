package org.qiweb.runtime.routes;

import java.util.ArrayList;
import java.util.List;
import org.qiweb.api.routes.PathBinder;
import org.qiweb.api.exceptions.PathBinderException;
import org.qiweb.api.routes.PathBinders;
import org.qiweb.runtime.util.TypeResolver;

public final class PathBindersInstance
    implements PathBinders
{

    /* package */ abstract static class StrictTypingPathBinder<T>
        implements PathBinder<T>
    {

        static
        {
            // TODO Only disable TypeResolver cache when in DEV mode
            TypeResolver.disableCache();
        }

        @Override
        public final boolean accept( Class<?> type )
        {
            return type.equals( TypeResolver.resolveArgument( getClass(), PathBinder.class ) );
        }
    }

    public static final class String
        extends StrictTypingPathBinder<java.lang.String>
    {

        @Override
        public java.lang.String bind( java.lang.String name, java.lang.String value )
        {
            return value.intern();
        }

        @Override
        public java.lang.String unbind( java.lang.String name, java.lang.String value )
        {
            return value.intern();
        }
    }

    public static final class Boolean
        extends StrictTypingPathBinder<java.lang.Boolean>
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

    public static final class Short
        extends StrictTypingPathBinder<java.lang.Short>
    {

        @Override
        public java.lang.Short bind( java.lang.String name, java.lang.String value )
        {
            return java.lang.Short.valueOf( value );
        }

        @Override
        public java.lang.String unbind( java.lang.String name, java.lang.Short value )
        {
            return java.lang.String.valueOf( value );
        }
    }

    public static final class Integer
        extends StrictTypingPathBinder<java.lang.Integer>
    {

        @Override
        public java.lang.Integer bind( java.lang.String name, java.lang.String value )
        {
            return java.lang.Integer.valueOf( value );
        }

        @Override
        public java.lang.String unbind( java.lang.String name, java.lang.Integer value )
        {
            return java.lang.String.valueOf( value );
        }
    }

    public static final class Long
        extends StrictTypingPathBinder<java.lang.Long>
    {

        @Override
        public java.lang.Long bind( java.lang.String name, java.lang.String value )
        {
            return java.lang.Long.valueOf( value );
        }

        @Override
        public java.lang.String unbind( java.lang.String name, java.lang.Long value )
        {
            return java.lang.String.valueOf( value );
        }
    }

    public static final class Double
        extends StrictTypingPathBinder<java.lang.Double>
    {

        @Override
        public java.lang.Double bind( java.lang.String name, java.lang.String value )
        {
            return java.lang.Double.valueOf( value );
        }

        @Override
        public java.lang.String unbind( java.lang.String name, java.lang.Double value )
        {
            return java.lang.String.valueOf( value );
        }
    }

    public static final class Float
        extends StrictTypingPathBinder<java.lang.Float>
    {

        @Override
        public java.lang.Float bind( java.lang.String name, java.lang.String value )
        {
            return java.lang.Float.valueOf( value );
        }

        @Override
        public java.lang.String unbind( java.lang.String name, java.lang.Float value )
        {
            return java.lang.String.valueOf( value );
        }
    }

    public static final class BigInteger
        extends StrictTypingPathBinder<java.math.BigInteger>
    {

        @Override
        public java.math.BigInteger bind( java.lang.String name, java.lang.String value )
        {
            return new java.math.BigInteger( value );
        }

        @Override
        public java.lang.String unbind( java.lang.String name, java.math.BigInteger value )
        {
            return value.toString();
        }
    }

    public static final class BigDecimal
        extends StrictTypingPathBinder<java.math.BigDecimal>
    {

        @Override
        public java.math.BigDecimal bind( java.lang.String name, java.lang.String value )
        {
            return new java.math.BigDecimal( value );
        }

        @Override
        public java.lang.String unbind( java.lang.String name, java.math.BigDecimal value )
        {
            return value.toString();
        }
    }

    public static final class UUID
        extends StrictTypingPathBinder<java.util.UUID>
    {

        @Override
        public java.util.UUID bind( java.lang.String name, java.lang.String value )
        {
            return java.util.UUID.fromString( value );
        }

        @Override
        public java.lang.String unbind( java.lang.String name, java.util.UUID value )
        {
            return value.toString();
        }
    }
    private final List<PathBinder<?>> pathBinders = new ArrayList<>();

    public PathBindersInstance()
    {
    }

    public PathBindersInstance( List<PathBinder<?>> pathBinders )
    {
        this.pathBinders.addAll( pathBinders );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T> T bind( Class<T> type, java.lang.String pathParamName, java.lang.String pathParamValue )
    {
        for( PathBinder<?> pathBinder : pathBinders )
        {
            if( pathBinder.accept( type ) )
            {
                return (T) pathBinder.bind( pathParamName, pathParamValue );
            }
        }
        throw new PathBinderException( "No PathBinder found for type: " + type );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T> java.lang.String unbind( Class<T> type, java.lang.String pathParamName, T pathParamValue )
    {
        for( PathBinder<?> pathBinder : pathBinders )
        {
            if( pathBinder.accept( type ) )
            {
                return ( (PathBinder<T>) pathBinder ).unbind( pathParamName, pathParamValue );
            }
        }
        throw new PathBinderException( "No PathBinder found for type: " + type );
    }
}
