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

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import io.werval.modules.xml.SAX;
import io.werval.modules.xml.UncheckedXMLException;

import static io.werval.modules.xml.internal.Internal.LOG;

/**
 * DocumentBuilderFactory implementation for XMLPlugin.
 * <p>
 * Factory API that enables applications to obtain a parser that produces DOM object trees from XML documents.
 *
 * @see DocumentBuilderFactory
 */
public final class DocumentBuilderFactoryImpl
    extends DocumentBuilderFactory
{
    // Xerces
    private final DocumentBuilderFactory delegate = new org.apache.xerces.jaxp.DocumentBuilderFactoryImpl();

    public DocumentBuilderFactoryImpl()
        throws ParserConfigurationException
    {
        super();
        delegate.setFeature( XMLConstants.FEATURE_SECURE_PROCESSING, true );
        delegate.setFeature( SAX.Features.EXTERNAL_GENERAL_ENTITIES, Internal.EXTERNAL_ENTITIES.get() );
        delegate.setFeature( SAX.Features.EXTERNAL_PARAMETER_ENTITIES, Internal.EXTERNAL_ENTITIES.get() );
        try
        {
            delegate.setAttribute(
                XMLConstants.ACCESS_EXTERNAL_DTD,
                Internal.EXTERNAL_ENTITIES.get() ? Internal.ACCESS_EXTERNAL_ALL : Internal.ACCESS_EXTERNAL_NONE
            );
        }
        catch( IllegalArgumentException ex )
        {
            LOG.trace( "JAXP<1.5 - {} on {}", ex.getMessage(), delegate );
        }
        try
        {
            delegate.setAttribute(
                XMLConstants.ACCESS_EXTERNAL_SCHEMA,
                Internal.EXTERNAL_ENTITIES.get() ? Internal.ACCESS_EXTERNAL_ALL : Internal.ACCESS_EXTERNAL_NONE
            );
        }
        catch( IllegalArgumentException ex )
        {
            LOG.trace( "JAXP<1.5 - {} on {}", ex.getMessage(), delegate );
        }
        setValidating( false );
        // No support but should be disabled anyway, belt'n braces
        delegate.setXIncludeAware( false );
    }

    @Override
    public DocumentBuilder newDocumentBuilder()
        throws ParserConfigurationException
    {
        DocumentBuilder docBuilder = delegate.newDocumentBuilder();
        docBuilder.setEntityResolver( Internal.RESOLVER.get() );
        docBuilder.setErrorHandler( Errors.INSTANCE );
        return docBuilder;
    }

    @Override
    public void setNamespaceAware( boolean awareness )
    {
        delegate.setNamespaceAware( awareness );
    }

    @Override
    public void setValidating( boolean dtdValidation )
    {
        try
        {
            // Xerces
            delegate.setFeature( "http://apache.org/xml/features/validation/balance-syntax-trees", dtdValidation );
            delegate.setFeature( "http://apache.org/xml/features/nonvalidating/load-dtd-grammar", dtdValidation );
            delegate.setFeature(
                "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                dtdValidation && Internal.EXTERNAL_ENTITIES.get()
            );
            delegate.setFeature( "http://apache.org/xml/features/disallow-doctype-decl", !dtdValidation );
        }
        catch( ParserConfigurationException ex )
        {
            throw new UncheckedXMLException( ex );
        }
        delegate.setValidating( dtdValidation );
        if( dtdValidation )
        {
            LOG.warn( "DocumentBuilderFactory.setValidating( true ) Unsafe DTD support enabled" );
        }
    }

    @Override
    public void setIgnoringElementContentWhitespace( boolean whitespace )
    {
        delegate.setIgnoringElementContentWhitespace( whitespace );
    }

    @Override
    public void setExpandEntityReferences( boolean expandEntityRef )
    {
        delegate.setExpandEntityReferences( expandEntityRef );
    }

    @Override
    public void setIgnoringComments( boolean ignoreComments )
    {
        delegate.setIgnoringComments( ignoreComments );
    }

    @Override
    public void setCoalescing( boolean coalescing )
    {
        delegate.setCoalescing( coalescing );
    }

    @Override
    public boolean isNamespaceAware()
    {
        return delegate.isNamespaceAware();
    }

    @Override
    public boolean isValidating()
    {
        return delegate.isValidating();
    }

    @Override
    public boolean isIgnoringElementContentWhitespace()
    {
        return delegate.isIgnoringElementContentWhitespace();
    }

    @Override
    public boolean isExpandEntityReferences()
    {
        return delegate.isExpandEntityReferences();
    }

    @Override
    public boolean isIgnoringComments()
    {
        return delegate.isIgnoringComments();
    }

    @Override
    public boolean isCoalescing()
    {
        return delegate.isCoalescing();
    }

    @Override
    public void setAttribute( String name, Object value )
        throws IllegalArgumentException
    {
        delegate.setAttribute( name, value );
    }

    @Override
    public Object getAttribute( String name )
        throws IllegalArgumentException
    {
        return delegate.getAttribute( name );
    }

    @Override
    public void setFeature( String name, boolean value )
        throws ParserConfigurationException
    {
        delegate.setFeature( name, value );
    }

    @Override
    public boolean getFeature( String name )
        throws ParserConfigurationException
    {
        return delegate.getFeature( name );
    }

    @Override
    public Schema getSchema()
    {
        return delegate.getSchema();
    }

    @Override
    public void setSchema( Schema schema )
    {
        delegate.setSchema( schema );
    }

    @Override
    public void setXIncludeAware( boolean state )
    {
        delegate.setXIncludeAware( state );
    }

    @Override
    public boolean isXIncludeAware()
    {
        return delegate.isXIncludeAware();
    }
}
