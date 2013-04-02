package com.acme.app;

import java.util.Objects;

public class CustomParam
{

    private final String internalValue;

    public CustomParam( String internalValue )
    {
        this.internalValue = internalValue;
    }

    public String computedValue()
    {
        return "Custom-" + internalValue;
    }

    /* package */ String internalValue()
    {
        return internalValue;
    }

    public static class PathBinder
        implements org.qiweb.api.routes.PathBinder<CustomParam>
    {

        @Override
        public boolean accept( Class<?> type )
        {
            return CustomParam.class.equals( type );
        }

        @Override
        public CustomParam bind( java.lang.String pathParamName, java.lang.String pathParamValue )
        {
            return new CustomParam( pathParamValue.intern() );
        }

        @Override
        public java.lang.String unbind( java.lang.String pathParamName, CustomParam pathParamValue )
        {
            return pathParamValue.internalValue().intern();
        }
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode( this.internalValue );
        return hash;
    }

    @Override
    public boolean equals( Object obj )
    {
        if( obj == null )
        {
            return false;
        }
        if( getClass() != obj.getClass() )
        {
            return false;
        }
        final CustomParam other = (CustomParam) obj;
        if( !Objects.equals( this.internalValue, other.internalValue ) )
        {
            return false;
        }
        return true;
    }

    @Override
    public java.lang.String toString()
    {
        return computedValue();
    }
}
