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
package org.qiweb.modules.xml.internal;

import java.io.OutputStream;
import java.io.Writer;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;

/**
 * XMLOutputFactory (StAX) implementation for XMLPlugin.
 * <p>
 * Factory for getting {@link XMLEventWriter}s and {@link XMLStreamWriter}s.
 *
 * @see XMLOutputFactory
 */
public final class XMLOutputFactoryImpl
    extends XMLOutputFactory
{
    // Aalto
    // private final XMLOutputFactory delegate = new com.fasterxml.aalto.stax.OutputFactoryImpl();
    // Woodstox
    private final XMLOutputFactory delegate = new com.ctc.wstx.stax.WstxOutputFactory();

    public XMLOutputFactoryImpl()
    {
        super();
        // No feature/property to set
    }

    @Override
    public XMLStreamWriter createXMLStreamWriter( Writer stream )
        throws XMLStreamException
    {
        return delegate.createXMLStreamWriter( stream );
    }

    @Override
    public XMLStreamWriter createXMLStreamWriter( OutputStream stream )
        throws XMLStreamException
    {
        return delegate.createXMLStreamWriter( stream );
    }

    @Override
    public XMLStreamWriter createXMLStreamWriter( OutputStream stream, String encoding )
        throws XMLStreamException
    {
        return delegate.createXMLStreamWriter( stream, encoding );
    }

    @Override
    public XMLStreamWriter createXMLStreamWriter( Result result )
        throws XMLStreamException
    {
        return delegate.createXMLStreamWriter( result );
    }

    @Override
    public XMLEventWriter createXMLEventWriter( Result result )
        throws XMLStreamException
    {
        return delegate.createXMLEventWriter( result );
    }

    @Override
    public XMLEventWriter createXMLEventWriter( OutputStream stream )
        throws XMLStreamException
    {
        return delegate.createXMLEventWriter( stream );
    }

    @Override
    public XMLEventWriter createXMLEventWriter( OutputStream stream, String encoding )
        throws XMLStreamException
    {
        return delegate.createXMLEventWriter( stream, encoding );
    }

    @Override
    public XMLEventWriter createXMLEventWriter( Writer stream )
        throws XMLStreamException
    {
        return delegate.createXMLEventWriter( stream );
    }

    @Override
    public void setProperty( String name, Object value )
        throws IllegalArgumentException
    {
        delegate.setProperty( name, value );
    }

    @Override
    public Object getProperty( String name )
        throws IllegalArgumentException
    {
        return delegate.getProperty( name );
    }

    @Override
    public boolean isPropertySupported( String name )
    {
        return delegate.isPropertySupported( name );
    }
}
