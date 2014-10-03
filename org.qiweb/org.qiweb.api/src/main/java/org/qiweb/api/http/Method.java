/*
 * Copyright (c) 2014 the original author or authors
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
package org.qiweb.api.http;

import java.io.Serializable;
import java.util.Objects;

import static org.qiweb.api.util.IllegalArguments.ensureNotEmpty;

/**
 * HTTP Method.
 */
public final class Method
    implements Serializable
{
    private static final String GET_NAME = "GET";
    private static final String HEAD_NAME = "HEAD";
    private static final String OPTIONS_NAME = "OPTIONS";
    private static final String TRACE_NAME = "TRACE";
    private static final String CONNECT_NAME = "CONNECT";
    private static final String PUT_NAME = "PUT";
    private static final String POST_NAME = "POST";
    private static final String PATCH_NAME = "PATCH";
    private static final String DELETE_NAME = "DELETE";

    public static final Method GET = new Method( GET_NAME, true );
    public static final Method HEAD = new Method( HEAD_NAME, true );
    public static final Method OPTIONS = new Method( OPTIONS_NAME, true );
    public static final Method TRACE = new Method( TRACE_NAME, true );
    public static final Method CONNECT = new Method( CONNECT_NAME, true );
    public static final Method PUT = new Method( PUT_NAME, false );
    public static final Method POST = new Method( POST_NAME, false );
    public static final Method PATCH = new Method( PATCH_NAME, false );
    public static final Method DELETE = new Method( DELETE_NAME, false );

    public static Method valueOf( String name )
    {
        switch( name )
        {
            case GET_NAME:
                return GET;
            case HEAD_NAME:
                return HEAD;
            case OPTIONS_NAME:
                return OPTIONS;
            case TRACE_NAME:
                return TRACE;
            case CONNECT_NAME:
                return CONNECT;
            case PUT_NAME:
                return PUT;
            case POST_NAME:
                return POST;
            case PATCH_NAME:
                return PATCH;
            case DELETE_NAME:
                return DELETE;
            default:
                return new Method( name, false );
        }
    }

    private final String name;
    private final boolean idempotent;

    /**
     * Create a new Method instance.
     *
     * @param name       Method's name
     * @param idempotent {@literal true} if idempotent, {@literal false} otherwise
     */
    public Method( String name, boolean idempotent )
    {
        ensureNotEmpty( "Method's name", name );
        this.name = name;
        this.idempotent = idempotent;
    }

    /**
     * @return Method's name
     */
    public String name()
    {
        return name;
    }

    /**
     * @return {@literal true} if Method is idempotent, otherwise return {@literal false}
     */
    public boolean idempotent()
    {
        return idempotent;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode( this.name );
        hash = 67 * hash + ( this.idempotent ? 1 : 0 );
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
        final Method other = (Method) obj;
        if( !Objects.equals( this.name, other.name ) )
        {
            return false;
        }
        return this.idempotent == other.idempotent;
    }

    /**
     * @return Method's name
     */
    @Override
    public String toString()
    {
        return name;
    }
}
