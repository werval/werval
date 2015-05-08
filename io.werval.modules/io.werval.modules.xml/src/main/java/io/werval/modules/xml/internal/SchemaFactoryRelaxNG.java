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

import java.io.File;
import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.validation.ValidatorHandler;

import io.werval.modules.xml.UncheckedXMLException;

import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import static io.werval.modules.xml.internal.Internal.ACCESS_EXTERNAL_ALL;
import static io.werval.modules.xml.internal.Internal.ACCESS_EXTERNAL_NONE;
import static io.werval.modules.xml.internal.Internal.LOG;

/**
 * RelaxNG SchemaFactory implementation for XMLPlugin.
 * <p>
 * Factory that creates {@link Schema} objects.
 * Entry-point to the validation API.
 *
 * @see SchemaFactory
 */
public final class SchemaFactoryRelaxNG
    extends SchemaFactory
{
    // Jing for RelaxNG
    private final SchemaFactory delegate = new com.thaiopensource.relaxng.jaxp.XMLSyntaxSchemaFactory();

    public SchemaFactoryRelaxNG()
        throws SAXException
    {
        super();
        delegate.setFeature( XMLConstants.FEATURE_SECURE_PROCESSING, true );
        try
        {
            delegate.setProperty(
                XMLConstants.ACCESS_EXTERNAL_DTD,
                Internal.EXTERNAL_ENTITIES.get() ? ACCESS_EXTERNAL_ALL : ACCESS_EXTERNAL_NONE
            );
        }
        catch( SAXException ex )
        {
            LOG.trace( "JAXP<1.5 - {} on {}", ex.getMessage(), delegate );
        }
        try
        {
            delegate.setProperty(
                XMLConstants.ACCESS_EXTERNAL_SCHEMA,
                Internal.EXTERNAL_ENTITIES.get() ? ACCESS_EXTERNAL_ALL : ACCESS_EXTERNAL_NONE
            );
        }
        catch( SAXException ex )
        {
            LOG.trace( "JAXP<1.5 - {} on {}", ex.getMessage(), delegate );
        }
        delegate.setResourceResolver( Internal.RESOLVER.get() );
        delegate.setErrorHandler( Errors.INSTANCE );
    }

    @Override
    public boolean isSchemaLanguageSupported( String schemaLanguage )
    {
        return delegate.isSchemaLanguageSupported( schemaLanguage );
    }

    @Override
    public boolean getFeature( String name )
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        return delegate.getFeature( name );
    }

    @Override
    public void setFeature( String name, boolean value )
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        delegate.setFeature( name, value );
    }

    @Override
    public void setProperty( String name, Object object )
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        delegate.setProperty( name, object );
    }

    @Override
    public Object getProperty( String name )
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        return delegate.getProperty( name );
    }

    @Override
    public void setErrorHandler( ErrorHandler errorHandler )
    {
        delegate.setErrorHandler( errorHandler );
    }

    @Override
    public ErrorHandler getErrorHandler()
    {
        return delegate.getErrorHandler();
    }

    @Override
    public void setResourceResolver( LSResourceResolver resourceResolver )
    {
        delegate.setResourceResolver( resourceResolver );
    }

    @Override
    public LSResourceResolver getResourceResolver()
    {
        return delegate.getResourceResolver();
    }

    @Override
    public Schema newSchema( Source schema )
        throws SAXException
    {
        return new XSDSchemaImpl( delegate.newSchema( schema ) );
    }

    @Override
    public Schema newSchema( File schema )
        throws SAXException
    {
        return new XSDSchemaImpl( delegate.newSchema( schema ) );
    }

    @Override
    public Schema newSchema( URL schema )
        throws SAXException
    {
        return new XSDSchemaImpl( delegate.newSchema( schema ) );
    }

    @Override
    public Schema newSchema( Source[] schemas )
        throws SAXException
    {
        return new XSDSchemaImpl( delegate.newSchema( schemas ) );
    }

    @Override
    public Schema newSchema()
        throws SAXException
    {
        return new XSDSchemaImpl( delegate.newSchema() );
    }

    private static final class XSDSchemaImpl
        extends Schema
    {
        private final Schema schema;

        private XSDSchemaImpl( Schema schema )
        {
            this.schema = schema;
        }

        @Override
        public Validator newValidator()
        {
            Validator validator = schema.newValidator();
            try
            {
                validator.setFeature( XMLConstants.FEATURE_SECURE_PROCESSING, true );
            }
            catch( SAXException ex )
            {
                throw new UncheckedXMLException( ex );
            }
            try
            {
                validator.setProperty(
                    XMLConstants.ACCESS_EXTERNAL_DTD,
                    Internal.EXTERNAL_ENTITIES.get() ? ACCESS_EXTERNAL_ALL : ACCESS_EXTERNAL_NONE
                );
            }
            catch( SAXException ex )
            {
                LOG.trace( "JAXP<1.5 - {} on {}", ex.getMessage(), validator );
            }
            try
            {
                validator.setProperty(
                    XMLConstants.ACCESS_EXTERNAL_SCHEMA,
                    Internal.EXTERNAL_ENTITIES.get() ? ACCESS_EXTERNAL_ALL : ACCESS_EXTERNAL_NONE
                );
            }
            catch( SAXException ex )
            {
                LOG.trace( "JAXP<1.5 - {} on {}", ex.getMessage(), validator );
            }
            validator.setResourceResolver( Internal.RESOLVER.get() );
            validator.setErrorHandler( Errors.INSTANCE );
            return validator;
        }

        @Override
        public ValidatorHandler newValidatorHandler()
        {
            ValidatorHandler handler = schema.newValidatorHandler();
            try
            {
                handler.setFeature( XMLConstants.FEATURE_SECURE_PROCESSING, true );
            }
            catch( SAXException ex )
            {
                throw new UncheckedXMLException( ex );
            }
            try
            {
                handler.setProperty(
                    XMLConstants.ACCESS_EXTERNAL_DTD,
                    Internal.EXTERNAL_ENTITIES.get() ? ACCESS_EXTERNAL_ALL : ACCESS_EXTERNAL_NONE
                );
            }
            catch( SAXException ex )
            {
                LOG.trace( "JAXP<1.5 - {} on {}", ex.getMessage(), handler );
            }
            try
            {
                handler.setProperty(
                    XMLConstants.ACCESS_EXTERNAL_SCHEMA,
                    Internal.EXTERNAL_ENTITIES.get() ? ACCESS_EXTERNAL_ALL : ACCESS_EXTERNAL_NONE
                );
            }
            catch( SAXException ex )
            {
                LOG.trace( "JAXP<1.5 - {} on {}", ex.getMessage(), handler );
            }
            handler.setResourceResolver( Internal.RESOLVER.get() );
            handler.setErrorHandler( Errors.INSTANCE );
            return handler;
        }
    }

}
