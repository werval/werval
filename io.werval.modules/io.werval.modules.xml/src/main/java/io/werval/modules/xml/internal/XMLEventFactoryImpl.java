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

import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.DTD;
import javax.xml.stream.events.EndDocument;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.EntityDeclaration;
import javax.xml.stream.events.EntityReference;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;

/**
 * XMLEventFactory (StAX) implementation for XMLPlugin.
 * <p>
 * Utility class for creating instances of XMLEvents.
 *
 * @see XMLEventFactory
 */
public final class XMLEventFactoryImpl
    extends XMLEventFactory
{
    // Aalto
    // private final XMLEventFactory delegate = new com.fasterxml.aalto.stax.EventFactoryImpl();
    // Woodstox
    private final XMLEventFactory delegate = new com.ctc.wstx.stax.WstxEventFactory();

    public XMLEventFactoryImpl()
    {
        super();
        // No feature/property to set
    }

    @Override
    public void setLocation( Location location )
    {
        delegate.setLocation( location );
    }

    @Override
    public Attribute createAttribute( String prefix, String namespaceURI, String localName, String value )
    {
        return delegate.createAttribute( prefix, namespaceURI, localName, value );
    }

    @Override
    public Attribute createAttribute( String localName, String value )
    {
        return delegate.createAttribute( localName, value );
    }

    @Override
    public Attribute createAttribute( QName name, String value )
    {
        return delegate.createAttribute( name, value );
    }

    @Override
    public Namespace createNamespace( String namespaceURI )
    {
        return delegate.createNamespace( namespaceURI );
    }

    @Override
    public Namespace createNamespace( String prefix, String namespaceUri )
    {
        return delegate.createNamespace( prefix, namespaceUri );
    }

    @Override
    public StartElement createStartElement( QName name, Iterator attributes, Iterator namespaces )
    {
        return delegate.createStartElement( name, attributes, namespaces );
    }

    @Override
    public StartElement createStartElement( String prefix, String namespaceUri, String localName )
    {
        return delegate.createStartElement( prefix, namespaceUri, localName );
    }

    @Override
    public StartElement createStartElement(
        String prefix, String namespaceUri,
        String localName, Iterator attributes,
        Iterator namespaces
    )
    {
        return delegate.createStartElement( prefix, namespaceUri, localName, attributes, namespaces );
    }

    @Override
    public StartElement createStartElement(
        String prefix, String namespaceUri,
        String localName, Iterator attributes,
        Iterator namespaces, NamespaceContext context
    )
    {
        return delegate.createStartElement( prefix, namespaceUri, localName, attributes, namespaces, context );
    }

    @Override
    public EndElement createEndElement( QName name, Iterator namespaces )
    {
        return delegate.createEndElement( name, namespaces );
    }

    @Override
    public EndElement createEndElement( String prefix, String namespaceUri, String localName )
    {
        return delegate.createEndElement( prefix, namespaceUri, localName );
    }

    @Override
    public EndElement createEndElement( String prefix, String namespaceUri, String localName, Iterator namespaces )
    {
        return delegate.createEndElement( prefix, namespaceUri, localName, namespaces );
    }

    @Override
    public Characters createCharacters( String content )
    {
        return delegate.createCharacters( content );
    }

    @Override
    public Characters createCData( String content )
    {
        return delegate.createCData( content );
    }

    @Override
    public Characters createSpace( String content )
    {
        return delegate.createSpace( content );
    }

    @Override
    public Characters createIgnorableSpace( String content )
    {
        return delegate.createIgnorableSpace( content );
    }

    @Override
    public StartDocument createStartDocument()
    {
        return delegate.createStartDocument();
    }

    @Override
    public StartDocument createStartDocument( String encoding, String version, boolean standalone )
    {
        return delegate.createStartDocument( encoding, version, standalone );
    }

    @Override
    public StartDocument createStartDocument( String encoding, String version )
    {
        return delegate.createStartDocument( encoding, version );
    }

    @Override
    public StartDocument createStartDocument( String encoding )
    {
        return delegate.createStartDocument( encoding );
    }

    @Override
    public EndDocument createEndDocument()
    {
        return delegate.createEndDocument();
    }

    @Override
    public EntityReference createEntityReference( String name, EntityDeclaration declaration )
    {
        return delegate.createEntityReference( name, declaration );
    }

    @Override
    public Comment createComment( String text )
    {
        return delegate.createComment( text );
    }

    @Override
    public ProcessingInstruction createProcessingInstruction( String target, String data )
    {
        return delegate.createProcessingInstruction( target, data );
    }

    @Override
    public DTD createDTD( String dtd )
    {
        return delegate.createDTD( dtd );
    }
}
