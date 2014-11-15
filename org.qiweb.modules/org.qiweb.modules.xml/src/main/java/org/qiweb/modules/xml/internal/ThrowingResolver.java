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

import javax.xml.transform.Source;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.w3c.dom.ls.LSInput;
import org.xml.sax.InputSource;

/**
 * Throwing Resolver.
 */
public final class ThrowingResolver
    implements Resolver
{
    public static final ThrowingResolver INSTANCE = new ThrowingResolver();

    private ThrowingResolver()
    {
    }

    // StAX or SAX
    @Override
    public InputSource resolveEntity( String name, String publicId, String baseURI, String systemId )
    {
        throw new UnsupportedOperationException(
            String.format(
                "Entity resolution attempt (StAX or SAX): name=%s publicId=%s baseURI=%s systemId=%s",
                name, publicId, baseURI, systemId
            )
        );
    }

    // SAX
    @Override
    public InputSource resolveEntity( String publicId, String systemId )
    {
        throw new UnsupportedOperationException(
            String.format(
                "Entity resolution attempt (SAX): publicId=%s systemId=%s",
                publicId, systemId
            )
        );
    }

    // SAX2
    @Override
    public InputSource getExternalSubset( String name, String baseURI )
    {
        Internal.LOG.debug( "External subset resolution attempt (SAX2): name={} baseURI={}", name, baseURI );
        // No external subset
        return null;
    }

    // DOM
    @Override
    public LSInput resolveResource( String type, String namespaceURI, String publicId, String systemId, String baseURI )
    {
        throw new UnsupportedOperationException(
            String.format(
                "Resource resolution attempt (DOM): type=%s namespace=%s publicId=%s systemId=%s baseURI=%s",
                type, namespaceURI, publicId, systemId, baseURI
            )
        );
    }

    // XSLT, xsl:include, xsl:import, or document() function
    @Override
    public Source resolve( String href, String base )
    {
        throw new UnsupportedOperationException(
            String.format(
                "URI resolution attempt (XSLT): href=%s base=%s",
                href, base
            )
        );
    }

    // Xerces XNI
    @Override
    public XMLInputSource resolveEntity( XMLResourceIdentifier resourceIdentifier )
    {
        throw new UnsupportedOperationException(
            String.format(
                "Entity resolution attempt (Xerces XNI): resourceIdentifier=%s",
                resourceIdentifier
            )
        );
    }
}
