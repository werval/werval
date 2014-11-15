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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import org.apache.xerces.dom.DOMInputImpl;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.w3c.dom.ls.LSInput;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Empty XML Resolver.
 * <p>
 * Always resolve to empty entities.
 */
public final class EmptyResolver
    implements Resolver
{
    public static final EmptyResolver INSTANCE = new EmptyResolver();

    private EmptyResolver()
    {
    }

    // StAX or SAX
    @Override
    public InputSource resolveEntity( String name, String publicId, String baseURI, String systemId )
    {
        return new InputSource( new ByteArrayInputStream( new byte[ 0 ] ) );
    }

    // SAX
    @Override
    public InputSource resolveEntity( String publicId, String systemId )
        throws SAXException, IOException
    {
        return new InputSource( new ByteArrayInputStream( new byte[ 0 ] ) );
    }

    // SAX2
    @Override
    public InputSource getExternalSubset( String name, String baseURI )
        throws SAXException, IOException
    {
        return null;
    }

    // DOM
    @Override
    public LSInput resolveResource( String type, String namespaceURI, String publicId, String systemId, String baseURI )
    {
        return new DOMInputImpl( publicId, systemId, baseURI, "", null );
    }

    // XSLT, xsl:include, xsl:import, or document() function
    @Override
    public Source resolve( String href, String base )
        throws TransformerException
    {
        return new StreamSource( new ByteArrayInputStream( new byte[ 0 ] ) );
    }

    // Xerces XNI
    @Override
    public XMLInputSource resolveEntity( XMLResourceIdentifier resourceIdentifier )
        throws XNIException, IOException
    {
        return new XMLInputSource(
            resourceIdentifier.getPublicId(),
            resourceIdentifier.getLiteralSystemId(),
            resourceIdentifier.getBaseSystemId(),
            new ByteArrayInputStream( new byte[ 0 ] ),
            null
        );
    }
}
