package org.qiweb.api.routes;

import org.qiweb.api.util.TypeResolver;

public interface PathBinder<T>
{

    boolean accept( Class<?> type );

    T bind( java.lang.String pathParamName, java.lang.String pathParamValue );

    java.lang.String unbind( java.lang.String pathParamName, T pathParamValue );

    public static abstract class AbstractPathBinder<T>
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
        extends AbstractPathBinder<java.lang.String>
    {

        @Override
        public java.lang.String bind( java.lang.String pathParamName, java.lang.String pathParamValue )
        {
            return pathParamValue.intern();
        }

        @Override
        public java.lang.String unbind( java.lang.String pathParamName, java.lang.String pathParamValue )
        {
            return pathParamValue.intern();
        }
    }

    public static final class Boolean
        extends AbstractPathBinder<java.lang.Boolean>
    {

        @Override
        public java.lang.Boolean bind( java.lang.String pathParamName, java.lang.String pathParamValue )
        {
            return java.lang.Boolean.valueOf( pathParamValue );
        }

        @Override
        public java.lang.String unbind( java.lang.String pathParamName, java.lang.Boolean pathParamValue )
        {
            return java.lang.String.valueOf( pathParamValue );
        }
    }

    public static final class Short
        extends AbstractPathBinder<java.lang.Short>
    {

        @Override
        public java.lang.Short bind( java.lang.String pathParamName, java.lang.String pathParamValue )
        {
            return java.lang.Short.valueOf( pathParamValue );
        }

        @Override
        public java.lang.String unbind( java.lang.String pathParamName, java.lang.Short pathParamValue )
        {
            return java.lang.String.valueOf( pathParamValue );
        }
    }

    public static final class Integer
        extends AbstractPathBinder<java.lang.Integer>
    {

        @Override
        public java.lang.Integer bind( java.lang.String pathParamName, java.lang.String pathParamValue )
        {
            return java.lang.Integer.valueOf( pathParamValue );
        }

        @Override
        public java.lang.String unbind( java.lang.String pathParamName, java.lang.Integer pathParamValue )
        {
            return java.lang.String.valueOf( pathParamValue );
        }
    }

    public static final class Long
        extends AbstractPathBinder<java.lang.Long>
    {

        @Override
        public java.lang.Long bind( java.lang.String pathParamName, java.lang.String pathParamValue )
        {
            return java.lang.Long.valueOf( pathParamValue );
        }

        @Override
        public java.lang.String unbind( java.lang.String pathParamName, java.lang.Long pathParamValue )
        {
            return java.lang.String.valueOf( pathParamValue );
        }
    }

    public static final class Double
        extends AbstractPathBinder<java.lang.Double>
    {

        @Override
        public java.lang.Double bind( java.lang.String pathParamName, java.lang.String pathParamValue )
        {
            return java.lang.Double.valueOf( pathParamValue );
        }

        @Override
        public java.lang.String unbind( java.lang.String pathParamName, java.lang.Double pathParamValue )
        {
            return java.lang.String.valueOf( pathParamValue );
        }
    }

    public static final class Float
        extends AbstractPathBinder<java.lang.Float>
    {

        @Override
        public java.lang.Float bind( java.lang.String pathParamName, java.lang.String pathParamValue )
        {
            return java.lang.Float.valueOf( pathParamValue );
        }

        @Override
        public java.lang.String unbind( java.lang.String pathParamName, java.lang.Float pathParamValue )
        {
            return java.lang.String.valueOf( pathParamValue );
        }
    }

    public static final class BigInteger
        extends AbstractPathBinder<java.math.BigInteger>
    {

        @Override
        public java.math.BigInteger bind( java.lang.String pathParamName, java.lang.String pathParamValue )
        {
            return new java.math.BigInteger( pathParamValue );
        }

        @Override
        public java.lang.String unbind( java.lang.String pathParamName, java.math.BigInteger pathParamValue )
        {
            return pathParamValue.toString();
        }
    }

    public static final class BigDecimal
        extends AbstractPathBinder<java.math.BigDecimal>
    {

        @Override
        public java.math.BigDecimal bind( java.lang.String pathParamName, java.lang.String pathParamValue )
        {
            return new java.math.BigDecimal( pathParamValue );
        }

        @Override
        public java.lang.String unbind( java.lang.String pathParamName, java.math.BigDecimal pathParamValue )
        {
            return pathParamValue.toString();
        }
    }

    public static final class UUID
        extends AbstractPathBinder<java.util.UUID>
    {

        @Override
        public java.util.UUID bind( java.lang.String pathParamName, java.lang.String pathParamValue )
        {
            return java.util.UUID.fromString( pathParamValue );
        }

        @Override
        public java.lang.String unbind( java.lang.String pathParamName, java.util.UUID pathParamValue )
        {
            return pathParamValue.toString();
        }
    }
}
