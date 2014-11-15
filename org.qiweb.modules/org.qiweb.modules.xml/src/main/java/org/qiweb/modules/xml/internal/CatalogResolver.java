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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.qiweb.modules.xml.UncheckedXMLException;
import org.w3c.dom.ls.LSInput;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import static org.qiweb.modules.xml.internal.Internal.LOG;

/**
 * XML-Catalog Resolver.
 */
public final class CatalogResolver
    implements Resolver
{
    private final org.apache.xerces.util.XMLCatalogResolver delegate = new org.apache.xerces.util.XMLCatalogResolver();

    private final boolean throwIfNotFound;

    public CatalogResolver( boolean throwIfNotFound, List<String> catalogs, boolean preferPublic )
    {
        this.throwIfNotFound = throwIfNotFound;
        delegate.setCatalogList( catalogs.toArray( new String[ catalogs.size() ] ) );
        delegate.setPreferPublic( preferPublic );
    }

    // StAX or SAX
    @Override
    public InputSource resolveEntity( String name, String publicId, String baseURI, String systemId )
    {
        LOG.debug(
            "Entity resolution attempt (StAX or SAX): name={} publicId={} baseURI={} systemId={}",
            name, publicId, baseURI, systemId
        );
        try
        {
            InputSource source = delegate.resolveEntity( name, publicId, baseURI, systemId );
            if( throwIfNotFound && source == null )
            {
                throw new UnsupportedOperationException(
                    String.format(
                        "Entity resolution attempt (StAX or SAX): name=%s publicId=%s baseURI=%s systemId=%s",
                        name, publicId, baseURI, systemId
                    )
                );
            }
            return source;
        }
        catch( SAXException ex )
        {
            throw new UncheckedXMLException( ex );
        }
        catch( IOException ex )
        {
            throw new UncheckedIOException( ex );
        }
    }

    // SAX
    @Override
    public InputSource resolveEntity( String publicId, String systemId )
        throws SAXException, IOException
    {
        LOG.debug(
            "Entity resolution attempt (SAX): publicId={} systemId={}",
            publicId, systemId
        );
        InputSource source = delegate.resolveEntity( publicId, systemId );
        if( throwIfNotFound && source == null )
        {
            throw new UnsupportedOperationException(
                String.format(
                    "Entity resolution attempt (SAX): publicId=%s systemId=%s",
                    publicId, systemId
                )
            );
        }
        return source;
    }

    // SAX2
    @Override
    public InputSource getExternalSubset( String name, String baseURI )
        throws SAXException, IOException
    {
        // No check here as a null return won't trigger another lookup
        return delegate.getExternalSubset( name, baseURI );
    }

    // DOM
    @Override
    public LSInput resolveResource( String type, String namespaceURI, String publicId, String systemId, String baseURI )
    {
        LOG.debug(
            "Resource resolution attempt (DOM): type={} namespace={} publicId={} systemId={} baseURI={}",
            type, namespaceURI, publicId, systemId, baseURI
        );
        LSInput source = delegate.resolveResource( type, namespaceURI, publicId, systemId, baseURI );
        if( throwIfNotFound && source == null )
        {
            throw new UnsupportedOperationException(
                String.format(
                    "Resource resolution attempt (DOM): type=%s namespace=%s publicId=%s systemId=%s baseURI=%s",
                    type, namespaceURI, publicId, systemId, baseURI
                )
            );
        }
        return source;
    }

    // XSLT, xsl:include, xsl:import, or document() function
    @Override
    public Source resolve( String href, String base )
        throws TransformerException
    {
        LOG.debug(
            "URI resolution attempt (XSLT): href={} base={}",
            href, base
        );
        try
        {
            InputSource source = delegate.resolveEntity( href, base );
            if( throwIfNotFound && source == null )
            {
                throw new UnsupportedOperationException(
                    String.format(
                        "URI resolution attempt (XSLT): href=%s base=%s",
                        href, base
                    )
                );
            }
            StreamSource result = new StreamSource( source.getSystemId() );
            result.setPublicId( source.getPublicId() );
            result.setReader( source.getCharacterStream() );
            result.setInputStream( source.getByteStream() );
            return result;
        }
        catch( SAXException | IOException ex )
        {
            throw new TransformerException( ex );
        }
    }

    // Xerces XNI
    @Override
    public XMLInputSource resolveEntity( XMLResourceIdentifier resourceIdentifier )
        throws XNIException, IOException
    {
        LOG.debug(
            "Entity resolution attempt (Xerces XNI): resourceIdentifier={}",
            resourceIdentifier
        );
        XMLInputSource source = delegate.resolveEntity( resourceIdentifier );
        if( throwIfNotFound && source == null )
        {
            throw new UnsupportedOperationException(
                String.format(
                    "Entity resolution attempt (Xerces XNI): resourceIdentifier=%s",
                    resourceIdentifier
                )
            );
        }
        return source;
    }
}
