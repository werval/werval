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

import javax.xml.transform.Source;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.parser.XMLInputSource;
import io.werval.modules.xml.UncheckedXMLException;
import org.w3c.dom.ls.LSInput;
import org.xml.sax.InputSource;

import static io.werval.modules.xml.internal.Internal.LOG;

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
        throw new UncheckedXMLException(
            String.format(
                "Entity resolution blocked (StAX or SAX): name=%s publicId=%s baseURI=%s systemId=%s",
                name, publicId, baseURI, systemId
            )
        );
    }

    // SAX
    @Override
    public InputSource resolveEntity( String publicId, String systemId )
    {
        throw new UncheckedXMLException(
            String.format( "Entity resolution blocked (SAX): publicId=%s systemId=%s", publicId, systemId )
        );
    }

    // SAX2
    @Override
    public InputSource getExternalSubset( String name, String baseURI )
    {
        LOG.trace( "External subset resolved to none (SAX2): name={} baseURI={}", name, baseURI );
        // No external subset
        return null;
    }

    // DOM
    @Override
    public LSInput resolveResource( String type, String namespaceURI, String publicId, String systemId, String baseURI )
    {
        throw new UncheckedXMLException(
            String.format(
                "Resource resolution blocked (DOM): type=%s namespace=%s publicId=%s systemId=%s baseURI=%s",
                type, namespaceURI, publicId, systemId, baseURI
            )
        );
    }

    // XSLT, xsl:include, xsl:import, or document() function
    @Override
    public Source resolve( String href, String base )
    {
        throw new UncheckedXMLException(
            String.format( "URI resolution blocked (XSLT): href=%s base=%s", href, base )
        );
    }

    // Xerces XNI
    @Override
    public XMLInputSource resolveEntity( XMLResourceIdentifier resourceIdentifier )
    {
        throw new UncheckedXMLException(
            String.format( "Entity resolution blocked (Xerces XNI): resourceIdentifier=%s", resourceIdentifier )
        );
    }
}
