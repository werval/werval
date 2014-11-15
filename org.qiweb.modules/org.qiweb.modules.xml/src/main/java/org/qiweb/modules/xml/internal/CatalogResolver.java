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
import java.lang.reflect.Field;
import java.util.List;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import org.apache.xerces.util.XMLCatalogResolver;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xml.resolver.CatalogManager;
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
    private static final class Debug
        extends org.apache.xml.resolver.helpers.Debug
    {
        @Override
        public void message( int level, String message )
        {
            if( level <= 2 )
            {
                LOG.trace( "CatalogResolver({}) {}", level, message );
            }
            else
            {
                LOG.debug( "CatalogResolver({}) {}", level, message );
            }
        }

        @Override
        public void message( int level, String message, String spec )
        {
            if( level <= 2 )
            {
                LOG.trace( "CatalogResolver({}) {}: {}", level, message, spec );
            }
            else
            {
                LOG.debug( "CatalogResolver({}) {}: {}", level, message, spec );
            }
        }

        @Override
        public void message( int level, String message, String spec1, String spec2 )
        {
            if( level <= 2 )
            {
                LOG.trace( "CatalogResolver({}) {}: {}\n    {}", level, message, spec1, spec2 );
            }
            else
            {
                LOG.debug( "CatalogResolver({}) {}: {}\n    {}", level, message, spec1, spec2 );
            }
        }
    }

    private final XMLCatalogResolver delegate = new org.apache.xerces.util.XMLCatalogResolver();

    private final boolean throwIfNotFound;

    public CatalogResolver( boolean throwIfNotFound, List<String> catalogs, boolean preferPublic )
    {
        this.throwIfNotFound = throwIfNotFound;
        delegate.setCatalogList( catalogs.toArray( new String[ catalogs.size() ] ) );
        delegate.setPreferPublic( preferPublic );
        if( LOG.isDebugEnabled() )
        {
            try
            {
                Field catalogManagerField = XMLCatalogResolver.class.getDeclaredField( "fResolverCatalogManager" );
                catalogManagerField.setAccessible( true );
                CatalogManager catalogManager = (CatalogManager) catalogManagerField.get( delegate );
                catalogManager.setVerbosity( Integer.MAX_VALUE );
                catalogManager.debug = new Debug();
                catalogManagerField.setAccessible( false );
            }
            catch( NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex )
            {
                LOG.warn(
                    "Unable to setup XML-Catalog resolution logging, something is broken, please report the issue!",
                    ex
                );
            }
        }
    }

    // StAX or SAX
    @Override
    public InputSource resolveEntity( String name, String publicId, String baseURI, String systemId )
    {
        try
        {
            InputSource source = delegate.resolveEntity( name, publicId, baseURI, systemId );
            if( throwIfNotFound && source == null )
            {
                throw new UncheckedXMLException(
                    String.format(
                        "Entity catalog resolution failure, remote blocked (StAX or SAX): "
                        + "name=%s publicId=%s baseURI=%s systemId=%s",
                        name, publicId, baseURI, systemId
                    )
                );
            }
            if( source == null )
            {
                LOG.warn(
                    "Entity catalog resolution failure, remote allowed (StAX or SAX): "
                    + "name={} publicId={} baseURI={} systemId={}",
                    name, publicId, baseURI, systemId
                );
            }
            else
            {
                LOG.debug(
                    "Entity catalog resolution success (StAX or SAX): name={} publicId={} baseURI={} systemId={}",
                    name, publicId, baseURI, systemId
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
            throw new UncheckedXMLException(
                String.format(
                    "Entity catalog resolution failure, remote blocked (SAX): publicId=%s systemId=%s",
                    publicId, systemId
                )
            );
        }
        if( source == null )
        {
            LOG.warn(
                "Entity catalog resolution failure, remote allowed (SAX): publicId={} systemId={}",
                publicId, systemId
            );
        }
        else
        {
            LOG.debug(
                "Entity catalog resolution success (SAX): publicId={} systemId={}",
                publicId, systemId
            );
        }
        return source;
    }

    // SAX2
    @Override
    public InputSource getExternalSubset( String name, String baseURI )
        throws SAXException, IOException
    {
        // No remote check here as a null return won't trigger another lookup
        InputSource source = delegate.getExternalSubset( name, baseURI );
        if( source == null )
        {
            LOG.trace( "External subset catalog resolution failure (SAX): name={} baseURI={}", name, baseURI );
        }
        else
        {
            LOG.trace( "External subset catalog resolution success (SAX): name={} baseURI={}", name, baseURI );
        }
        return source;
    }

    // DOM
    @Override
    public LSInput resolveResource( String type, String namespaceURI, String publicId, String systemId, String baseURI )
    {
        LSInput source = delegate.resolveResource( type, namespaceURI, publicId, systemId, baseURI );
        if( throwIfNotFound && source == null )
        {
            throw new UncheckedXMLException(
                String.format(
                    "Resource catalog resolution failure, remote blocked (DOM): "
                    + "type=%s namespace=%s publicId=%s systemId=%s baseURI=%s",
                    type, namespaceURI, publicId, systemId, baseURI
                )
            );
        }
        if( source == null )
        {
            LOG.warn(
                "Resource catalog resolution failure, remote allowed (DOM): "
                + "type={} namespace={} publicId={} systemId={} baseURI={}",
                type, namespaceURI, publicId, systemId, baseURI
            );
        }
        else
        {
            LOG.warn(
                "Resource catalog resolution success (DOM): type={} namespace={} publicId={} systemId={} baseURI={}",
                type, namespaceURI, publicId, systemId, baseURI
            );
        }
        return source;
    }

    // XSLT, xsl:include, xsl:import, or document() function
    @Override
    public Source resolve( String href, String base )
        throws TransformerException
    {
        try
        {
            InputSource source = delegate.resolveEntity( href, base );
            if( throwIfNotFound && source == null )
            {
                throw new UncheckedXMLException(
                    String.format(
                        "URI catalog resolution failure, remote blocked (XSLT): href=%s base=%s",
                        href, base
                    )
                );
            }
            if( source == null )
            {
                LOG.warn( "URI catalog resolution failure, remote allowed (XSLT): href={} base={}", href, base );
                return null;
            }
            LOG.warn( "URI catalog resolution success (XSLT): href={} base={}", href, base );
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
        XMLInputSource source = delegate.resolveEntity( resourceIdentifier );
        if( throwIfNotFound && source == null )
        {
            throw new UncheckedXMLException(
                String.format(
                    "Entity catalog resolution failure, remote blocked (Xerces XNI): resourceIdentifier=%s",
                    resourceIdentifier
                )
            );
        }
        if( source == null )
        {
            LOG.warn(
                "Entity catalog resolution failure, remote allowed (Xerces XNI): resourceIdentifier={}",
                resourceIdentifier
            );
        }
        else
        {
            LOG.debug( "Entity catalog resolution success (Xerces XNI): resourceIdentifier={}", resourceIdentifier );
        }
        return source;
    }
}
