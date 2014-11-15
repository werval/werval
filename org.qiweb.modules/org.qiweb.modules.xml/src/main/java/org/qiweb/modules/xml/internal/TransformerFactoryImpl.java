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

import java.util.Properties;
import javax.xml.XMLConstants;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;

import static org.qiweb.modules.xml.internal.Internal.ACCESS_EXTERNAL_ALL;
import static org.qiweb.modules.xml.internal.Internal.ACCESS_EXTERNAL_NONE;

/**
 * TransformerFactory implementation for XMLPlugin.
 * <p>
 * A TransformerFactory instance can be used to create {@link Transformer} and {@link Templates} objects.
 *
 * @see TransformerFactory
 */
public final class TransformerFactoryImpl
    extends TransformerFactory
{
    // Saxon
    private final TransformerFactory delegate = new net.sf.saxon.jaxp.SaxonTransformerFactory();

    public TransformerFactoryImpl()
        throws TransformerConfigurationException
    {
        super();
        delegate.setFeature( XMLConstants.FEATURE_SECURE_PROCESSING, true );
        try
        {
            delegate.setAttribute(
                XMLConstants.ACCESS_EXTERNAL_DTD,
                Internal.EXTERNAL_ENTITIES.get() ? ACCESS_EXTERNAL_ALL : ACCESS_EXTERNAL_NONE
            );
        }
        catch( IllegalArgumentException ex )
        {
            Internal.LOG.trace( "WARNING - JAXP<1.5 - {} on {}", ex.getMessage(), delegate );
        }
        try
        {
            delegate.setAttribute(
                XMLConstants.ACCESS_EXTERNAL_STYLESHEET,
                Internal.EXTERNAL_ENTITIES.get() ? ACCESS_EXTERNAL_ALL : ACCESS_EXTERNAL_NONE
            );
        }
        catch( IllegalArgumentException ex )
        {
            Internal.LOG.trace( "WARNING - JAXP<1.5 - {} on {}", ex.getMessage(), delegate );
        }
        delegate.setURIResolver( Internal.RESOLVER.get() );
        delegate.setErrorListener( Errors.INSTANCE );
    }

    @Override
    public Transformer newTransformer( Source source )
        throws TransformerConfigurationException
    {
        Transformer transformer = delegate.newTransformer( source );
        transformer.setURIResolver( Internal.RESOLVER.get() );
        transformer.setErrorListener( Errors.INSTANCE );
        return transformer;
    }

    @Override
    public Transformer newTransformer()
        throws TransformerConfigurationException
    {
        Transformer transformer = delegate.newTransformer();
        transformer.setURIResolver( Internal.RESOLVER.get() );
        transformer.setErrorListener( Errors.INSTANCE );
        return transformer;
    }

    @Override
    public Templates newTemplates( Source source )
        throws TransformerConfigurationException
    {
        return new TemplatesImpl( delegate.newTemplates( source ) );
    }

    @Override
    public Source getAssociatedStylesheet( Source source, String media, String title, String charset )
        throws TransformerConfigurationException
    {
        return delegate.getAssociatedStylesheet( source, media, title, charset );
    }

    @Override
    public void setURIResolver( URIResolver resolver )
    {
        delegate.setURIResolver( resolver );
    }

    @Override
    public URIResolver getURIResolver()
    {
        return delegate.getURIResolver();
    }

    @Override
    public void setFeature( String name, boolean value )
        throws TransformerConfigurationException
    {
        delegate.setFeature( name, value );
    }

    @Override
    public boolean getFeature( String name )
    {
        return delegate.getFeature( name );
    }

    @Override
    public void setAttribute( String name, Object value )
    {
        delegate.setAttribute( name, value );
    }

    @Override
    public Object getAttribute( String name )
    {
        return delegate.getAttribute( name );
    }

    @Override
    public void setErrorListener( ErrorListener listener )
    {
        delegate.setErrorListener( listener );
    }

    @Override
    public ErrorListener getErrorListener()
    {
        return delegate.getErrorListener();
    }

    private static final class TemplatesImpl
        implements Templates
    {
        private final Templates templates;

        private TemplatesImpl( Templates templates )
        {
            this.templates = templates;
        }

        @Override
        public Transformer newTransformer()
            throws TransformerConfigurationException
        {
            Transformer transformer = templates.newTransformer();
            transformer.setURIResolver( Internal.RESOLVER.get() );
            transformer.setErrorListener( Errors.INSTANCE );
            return transformer;
        }

        @Override
        public Properties getOutputProperties()
        {
            return templates.getOutputProperties();
        }
    }
}
