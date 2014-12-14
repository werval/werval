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
package io.werval.modules.xml.internal;

import com.ctc.wstx.api.WstxInputProperties;
import java.io.InputStream;
import java.io.Reader;
import javax.xml.XMLConstants;
import javax.xml.stream.EventFilter;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.XMLEventAllocator;
import javax.xml.transform.Source;

import static com.ctc.wstx.api.ReaderConfig.DEFAULT_MAX_ATTRIBUTES_PER_ELEMENT;
import static com.ctc.wstx.api.ReaderConfig.DEFAULT_MAX_ATTRIBUTE_LENGTH;
import static com.ctc.wstx.api.ReaderConfig.DEFAULT_MAX_ELEMENT_DEPTH;
import static com.ctc.wstx.api.ReaderConfig.DEFAULT_MAX_ENTITY_COUNT;
import static com.ctc.wstx.api.ReaderConfig.DEFAULT_MAX_ENTITY_DEPTH;
import static io.werval.modules.xml.internal.Internal.ACCESS_EXTERNAL_ALL;
import static io.werval.modules.xml.internal.Internal.ACCESS_EXTERNAL_NONE;
import static io.werval.modules.xml.internal.Internal.LOG;

/**
 * XMLInputFactory (StAX) implementation for XMLPlugin.
 */
public final class XMLInputFactoryImpl
    extends XMLInputFactory
{
    // Aalto
    // private final XMLInputFactory delegate = new com.fasterxml.aalto.stax.InputFactoryImpl();
    // Woodstox
    private final XMLInputFactory delegate = new com.ctc.wstx.stax.WstxInputFactory();

    public XMLInputFactoryImpl()
    {
        super();
        // Aalto & Woodstox
        delegate.setProperty( XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false );
        // Woodstox
        delegate.setProperty( WstxInputProperties.P_MAX_ATTRIBUTES_PER_ELEMENT, DEFAULT_MAX_ATTRIBUTES_PER_ELEMENT );
        delegate.setProperty( WstxInputProperties.P_MAX_ATTRIBUTE_SIZE, DEFAULT_MAX_ATTRIBUTE_LENGTH );
        delegate.setProperty( WstxInputProperties.P_MAX_ELEMENT_DEPTH, DEFAULT_MAX_ELEMENT_DEPTH );
        delegate.setProperty( WstxInputProperties.P_MAX_ENTITY_COUNT, DEFAULT_MAX_ENTITY_COUNT );
        delegate.setProperty( WstxInputProperties.P_MAX_ENTITY_DEPTH, DEFAULT_MAX_ENTITY_DEPTH );
        // delegate.setProperty( WstxInputProperties.P_MAX_CHARACTERS, 42 );
        // delegate.setProperty( WstxInputProperties.P_MAX_CHILDREN_PER_ELEMENT, 42 );
        // delegate.setProperty( WstxInputProperties.P_MAX_ELEMENT_COUNT, 42 );
        // delegate.setProperty( WstxInputProperties.P_MAX_TEXT_LENGTH, 42 );
        // None
        try
        {
            delegate.setProperty(
                XMLConstants.ACCESS_EXTERNAL_DTD,
                Internal.EXTERNAL_ENTITIES.get() ? ACCESS_EXTERNAL_ALL : ACCESS_EXTERNAL_NONE
            );
        }
        catch( IllegalArgumentException ex )
        {
            LOG.trace( "JAXP<1.5 - {} on {}", ex.getMessage(), delegate );
        }
        delegate.setXMLResolver( Internal.RESOLVER.get() );
        delegate.setXMLReporter( Errors.INSTANCE );
    }

    @Override
    public XMLStreamReader createXMLStreamReader( Reader reader )
        throws XMLStreamException
    {
        return delegate.createXMLStreamReader( reader );
    }

    @Override
    public XMLStreamReader createXMLStreamReader( Source source )
        throws XMLStreamException
    {
        return delegate.createXMLStreamReader( source );
    }

    @Override
    public XMLStreamReader createXMLStreamReader( InputStream stream )
        throws XMLStreamException
    {
        return delegate.createXMLStreamReader( stream );
    }

    @Override
    public XMLStreamReader createXMLStreamReader( InputStream stream, String encoding )
        throws XMLStreamException
    {
        return delegate.createXMLStreamReader( stream, encoding );
    }

    @Override
    public XMLStreamReader createXMLStreamReader( String systemId, InputStream stream )
        throws XMLStreamException
    {
        return delegate.createXMLStreamReader( systemId, stream );
    }

    @Override
    public XMLStreamReader createXMLStreamReader( String systemId, Reader reader )
        throws XMLStreamException
    {
        return delegate.createXMLStreamReader( systemId, reader );
    }

    @Override
    public XMLEventReader createXMLEventReader( Reader reader )
        throws XMLStreamException
    {
        return delegate.createXMLEventReader( reader );
    }

    @Override
    public XMLEventReader createXMLEventReader( String systemId, Reader reader )
        throws XMLStreamException
    {
        return delegate.createXMLEventReader( systemId, reader );
    }

    @Override
    public XMLEventReader createXMLEventReader( XMLStreamReader reader )
        throws XMLStreamException
    {
        return delegate.createXMLEventReader( reader );
    }

    @Override
    public XMLEventReader createXMLEventReader( Source source )
        throws XMLStreamException
    {
        return delegate.createXMLEventReader( source );
    }

    @Override
    public XMLEventReader createXMLEventReader( InputStream stream )
        throws XMLStreamException
    {
        return delegate.createXMLEventReader( stream );
    }

    @Override
    public XMLEventReader createXMLEventReader( InputStream stream, String encoding )
        throws XMLStreamException
    {
        return delegate.createXMLEventReader( stream, encoding );
    }

    @Override
    public XMLEventReader createXMLEventReader( String systemId, InputStream stream )
        throws XMLStreamException
    {
        return delegate.createXMLEventReader( systemId, stream );
    }

    @Override
    public XMLStreamReader createFilteredReader( XMLStreamReader reader, StreamFilter filter )
        throws XMLStreamException
    {
        return delegate.createFilteredReader( reader, filter );
    }

    @Override
    public XMLEventReader createFilteredReader( XMLEventReader reader, EventFilter filter )
        throws XMLStreamException
    {
        return delegate.createFilteredReader( reader, filter );
    }

    @Override
    public XMLResolver getXMLResolver()
    {
        return delegate.getXMLResolver();
    }

    @Override
    public void setXMLResolver( XMLResolver resolver )
    {
        delegate.setXMLResolver( resolver );
    }

    @Override
    public XMLReporter getXMLReporter()
    {
        return delegate.getXMLReporter();
    }

    @Override
    public void setXMLReporter( XMLReporter reporter )
    {
        delegate.setXMLReporter( reporter );
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

    @Override
    public void setEventAllocator( XMLEventAllocator allocator )
    {
        delegate.setEventAllocator( allocator );
    }

    @Override
    public XMLEventAllocator getEventAllocator()
    {
        return delegate.getEventAllocator();
    }
}
