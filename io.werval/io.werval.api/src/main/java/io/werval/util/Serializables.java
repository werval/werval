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
package io.werval.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;

import static io.werval.util.Charsets.UTF_8;

/**
 * Serializables utilities.
 */
public final class Serializables
{
    /**
     * Serialize an Object to String.
     *
     * @param object Object to serialize
     *
     * @return Serialized Object as String, {@literal UTF-8} encoded
     */
    public static String toString( Object object )
    {
        return toString( object, UTF_8 );
    }

    /**
     * Serialize an Object to String.
     *
     * @param object  Object to serialize
     * @param charset Charset
     *
     * @return Serialized Object as String
     */
    public static String toString( Object object, Charset charset )
    {
        return new String( toBytes( object ), charset );
    }

    /**
     * Serialize an Object to bytes.
     *
     * @param object Object to serialize
     *
     * @return Serialized Object bytes
     */
    public static byte[] toBytes( Object object )
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try( ObjectOutputStream oos = new ObjectOutputStream( baos ) )
        {
            oos.writeObject( object );
        }
        catch( IOException ex )
        {
            throw new UncheckedIOException( ex );
        }
        return baos.toByteArray();
    }

    /**
     * Deserialize an Object from a String.
     *
     * @param <T>        Parameterized type of the deserialized Object
     * @param serialized Serialized state as String, {@literal UTF-8} encoded
     *
     * @return Deserialized Object
     */
    public static <T> T fromString( String serialized )
    {
        return fromString( serialized, UTF_8 );
    }

    /**
     * Deserialize an Object from a String.
     *
     * @param <T>        Parameterized type of the deserialized Object
     * @param serialized Serialized state as String
     * @param charset    Charset
     *
     * @return Deserialized Object
     */
    public static <T> T fromString( String serialized, Charset charset )
    {
        return fromBytes( serialized.getBytes( charset ) );
    }

    /**
     * Deserialize an Object from bytes.
     *
     * @param <T>        Parameterized type of the deserialized Object
     * @param serialized Serialized state bytes
     *
     * @return Deserialized Object
     */
    public static <T> T fromBytes( byte[] serialized )
    {
        ByteArrayInputStream bais = new ByteArrayInputStream( serialized );
        try( ObjectInputStream ois = new ObjectInputStream( bais ) )
        {
            return (T) ois.readObject();
        }
        catch( IOException ex )
        {
            throw new UncheckedIOException( ex );
        }
        catch( ClassNotFoundException ex )
        {
            throw new RuntimeException( ex.getMessage(), ex );
        }
    }

    private Serializables()
    {
    }
}
