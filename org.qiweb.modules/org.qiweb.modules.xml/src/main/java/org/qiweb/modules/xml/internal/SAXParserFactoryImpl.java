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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import org.qiweb.modules.xml.SAX;
import org.xml.sax.HandlerBase;
import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import static org.qiweb.modules.xml.internal.Internal.ACCESS_EXTERNAL_ALL;
import static org.qiweb.modules.xml.internal.Internal.ACCESS_EXTERNAL_NONE;

/**
 * SAXParserFactory implementation for XMLPlugin.
 * <p>
 * Factory API that enables applications to configure and obtain a SAX based parser to parse XML documents.
 *
 * @see SAXParserFactory
 */
public final class SAXParserFactoryImpl
    extends SAXParserFactory
{
    // Aalto
    // private final SAXParserFactory delegate = new com.fasterxml.aalto.sax.SAXParserFactoryImpl();
    // Woodstox
    // private final SAXParserFactory delegate = new com.ctc.wstx.sax.WstxSAXParserFactory();
    // Xerces
    private final SAXParserFactory delegate = new org.apache.xerces.jaxp.SAXParserFactoryImpl();

    public SAXParserFactoryImpl()
        throws ParserConfigurationException, SAXException
    {
        // Aalto & Woodstox & Xerces
        delegate.setFeature( SAX.Features.EXTERNAL_GENERAL_ENTITIES, Internal.EXTERNAL_ENTITIES.get() );
        delegate.setFeature( SAX.Features.EXTERNAL_PARAMETER_ENTITIES, Internal.EXTERNAL_ENTITIES.get() );
        // Xerces
        // delegate.setFeature( XMLConstants.FEATURE_SECURE_PROCESSING, true );
        // delegate.setFeature( "http://apache.org/xml/features/disallow-doctype-decl", true );
        // No support but should be disabled anyway, belt'n braces
        delegate.setXIncludeAware( false );
    }

    @Override
    public void setNamespaceAware( boolean namespaceAware )
    {
        delegate.setNamespaceAware( namespaceAware );
    }

    @Override
    public void setValidating( boolean validating )
    {
        delegate.setValidating( validating );
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
    public void setXIncludeAware( boolean xIncludeAware )
    {
        delegate.setXIncludeAware( xIncludeAware );
    }

    @Override
    public boolean isXIncludeAware()
    {
        return delegate.isXIncludeAware();
    }

    @Override
    public SAXParser newSAXParser()
        throws ParserConfigurationException, SAXException
    {
        return new SAXParserImpl( delegate.newSAXParser() );
    }

    @Override
    public void setFeature( String name, boolean value )
        throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException
    {
        delegate.setFeature( name, value );
    }

    @Override
    public boolean getFeature( String name )
        throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException
    {
        return delegate.getFeature( name );
    }

    private static final class SAXParserImpl
        extends SAXParser
    {
        private final SAXParser parser;

        protected SAXParserImpl( SAXParser saxParser )
            throws SAXException
        {
            this.parser = saxParser;
            try
            {
                this.parser.setProperty(
                    XMLConstants.ACCESS_EXTERNAL_DTD,
                    Internal.EXTERNAL_ENTITIES.get() ? ACCESS_EXTERNAL_ALL : ACCESS_EXTERNAL_NONE
                );
            }
            catch( SAXException ex )
            {
                Internal.LOG.trace( "WARNING - JAXP<1.5 - {} on {}", ex.getMessage(), this.parser );
            }
            try
            {
                this.parser.setProperty(
                    XMLConstants.ACCESS_EXTERNAL_SCHEMA,
                    Internal.EXTERNAL_ENTITIES.get() ? ACCESS_EXTERNAL_ALL : ACCESS_EXTERNAL_NONE
                );
            }
            catch( SAXException ex )
            {
                Internal.LOG.trace( "WARNING - JAXP<1.5 - {} on {}", ex.getMessage(), this.parser );
            }
        }

        @Override
        public void reset()
        {
            parser.reset();
        }

        @Override
        public void parse( InputStream inputStream, HandlerBase handlerBase )
            throws SAXException, IOException
        {
            parser.parse( inputStream, handlerBase );
        }

        @Override
        public void parse( InputStream inputStream, HandlerBase handlerBase, String systemId )
            throws SAXException, IOException
        {
            parser.parse( inputStream, handlerBase, systemId );
        }

        @Override
        public void parse( InputStream inputStream, DefaultHandler defaultHandler )
            throws SAXException, IOException
        {
            parser.parse( inputStream, defaultHandler );
        }

        @Override
        public void parse( InputStream inputStream, DefaultHandler defaultHandler, String systemId )
            throws SAXException, IOException
        {
            parser.parse( inputStream, defaultHandler, systemId );
        }

        @Override
        public void parse( String s, HandlerBase handlerBase )
            throws SAXException, IOException
        {
            parser.parse( s, handlerBase );
        }

        @Override
        public void parse( String s, DefaultHandler defaultHandler )
            throws SAXException, IOException
        {
            parser.parse( s, defaultHandler );
        }

        @Override
        public void parse( File file, HandlerBase handlerBase )
            throws SAXException, IOException
        {
            parser.parse( file, handlerBase );
        }

        @Override
        public void parse( File file, DefaultHandler defaultHandler )
            throws SAXException, IOException
        {
            parser.parse( file, defaultHandler );
        }

        @Override
        public void parse( InputSource inputSource, HandlerBase handlerBase )
            throws SAXException, IOException
        {
            parser.parse( inputSource, handlerBase );
        }

        @Override
        public void parse( InputSource inputSource, DefaultHandler defaultHandler )
            throws SAXException, IOException
        {
            parser.parse( inputSource, defaultHandler );
        }

        @Override
        public Parser getParser()
            throws SAXException
        {
            return parser.getParser();
        }

        @Override
        public XMLReader getXMLReader()
            throws SAXException
        {
            XMLReader reader = parser.getXMLReader();
            try
            {
                reader.setFeature( XMLConstants.FEATURE_SECURE_PROCESSING, true );
            }
            catch( SAXNotRecognizedException ex )
            {
                Internal.LOG.trace( "WARNING - JAXP<1.5 - {} on {}", ex.getMessage(), reader );
            }
            reader.setFeature( SAX.Features.EXTERNAL_GENERAL_ENTITIES, Internal.EXTERNAL_ENTITIES.get() );
            reader.setFeature( SAX.Features.EXTERNAL_PARAMETER_ENTITIES, Internal.EXTERNAL_ENTITIES.get() );
            reader.setEntityResolver( Internal.RESOLVER.get() );
            reader.setErrorHandler( Errors.INSTANCE );
            return reader;
        }

        @Override
        public boolean isNamespaceAware()
        {
            return parser.isNamespaceAware();
        }

        @Override
        public boolean isValidating()
        {
            return parser.isValidating();
        }

        @Override
        public void setProperty( String name, Object value )
            throws SAXNotRecognizedException, SAXNotSupportedException
        {
            parser.setProperty( name, value );
        }

        @Override
        public Object getProperty( String name )
            throws SAXNotRecognizedException, SAXNotSupportedException
        {
            return parser.getProperty( name );
        }

        @Override
        public Schema getSchema()
        {
            return parser.getSchema();
        }

        @Override
        public boolean isXIncludeAware()
        {
            return parser.isXIncludeAware();
        }
    }
}
