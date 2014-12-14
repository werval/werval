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
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;

/**
 * XPathFactory implementation for XMLPlugin.
 * <p>
 * An XPathFactory instance can be used to create {@link XPath} objects.
 *
 * @see XPathFactory
 */
public final class XPathFactoryImpl
    extends XPathFactory
{
    // Saxon
    private final XPathFactory delegate = new net.sf.saxon.xpath.XPathFactoryImpl();

    public XPathFactoryImpl()
        throws XPathFactoryConfigurationException
    {
        super();
        delegate.setFeature( XMLConstants.FEATURE_SECURE_PROCESSING, true );
    }

    @Override
    public boolean isObjectModelSupported( String objectModel )
    {
        return delegate.isObjectModelSupported( objectModel );
    }

    @Override
    public void setFeature( String name, boolean value )
        throws XPathFactoryConfigurationException
    {
        delegate.setFeature( name, value );
    }

    @Override
    public boolean getFeature( String name )
        throws XPathFactoryConfigurationException
    {
        return delegate.getFeature( name );
    }

    @Override
    public void setXPathVariableResolver( XPathVariableResolver resolver )
    {
        delegate.setXPathVariableResolver( resolver );
    }

    @Override
    public void setXPathFunctionResolver( XPathFunctionResolver resolver )
    {
        delegate.setXPathFunctionResolver( resolver );
    }

    @Override
    public XPath newXPath()
    {
        return delegate.newXPath();
    }
}
