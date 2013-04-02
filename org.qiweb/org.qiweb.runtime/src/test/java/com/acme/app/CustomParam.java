package com.acme.app;

public class CustomParam
{

    private final String internalValue;

    public CustomParam( String internalValue )
    {
        this.internalValue = internalValue;
    }

    public String value()
    {
        return "Custom-" + internalValue;
    }
}
