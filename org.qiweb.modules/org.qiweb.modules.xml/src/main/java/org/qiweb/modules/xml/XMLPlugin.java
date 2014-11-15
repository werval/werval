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
package org.qiweb.modules.xml;

import java.util.Arrays;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPathFactory;
import org.qiweb.api.Application;
import org.qiweb.api.Config;
import org.qiweb.api.Plugin;
import org.qiweb.api.exceptions.ActivationException;
import org.qiweb.api.exceptions.QiWebException;
import org.qiweb.modules.xml.internal.CatalogResolver;
import org.qiweb.modules.xml.internal.EmptyResolver;
import org.qiweb.modules.xml.internal.Internal;
import org.qiweb.modules.xml.internal.ThrowingResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.EMPTY_LIST;
import static org.qiweb.util.Strings.NEWLINE;

/**
 * XML Plugin.
 * <p>
 * See https://docs.fedoraproject.org/en-US/Fedora_Security_Team/html/Defensive_Coding/sect-Defensive_Coding-Tasks-Serialization-XML.html
 */
// https://xerces.apache.org/xerces2-j/features.html
// TODO http://apache.org/xml/features/nonvalidating/load-external-dtd
// TODO http://apache.org/xml/features/disallow-doctype-decl
// TODO http://apache.org/xml/features/xinclude
public final class XMLPlugin
    implements Plugin<XML>
{
    private static final Logger LOG = LoggerFactory.getLogger( XMLPlugin.class );

    static
    {
        System.out.println( System.getProperty( "javax.xml.stream.allocator" ) );
        if( LOG.isTraceEnabled() )
        {
            System.setProperty( "jaxp.debug", "yes" );
        }

        // Woodstox for XML Streaming (StAX, aka pull parsing, and SAX[2], aka push parsing)
        System.setProperty(
            "javax.xml.stream.XMLEventFactory",
            org.qiweb.modules.xml.internal.XMLEventFactoryImpl.class.getName()
        );
        System.setProperty(
            "javax.xml.stream.XMLInputFactory",
            org.qiweb.modules.xml.internal.XMLInputFactoryImpl.class.getName()
        );
        System.setProperty(
            "javax.xml.stream.XMLOutputFactory",
            org.qiweb.modules.xml.internal.XMLOutputFactoryImpl.class.getName()
        );
        System.setProperty(
            "javax.xml.parsers.SAXParserFactory",
            org.qiweb.modules.xml.internal.SAXParserFactoryImpl.class.getName()
        );

        // Xerces for DOM and Schema validation
        System.setProperty(
            "javax.xml.datatype.DatatypeFactory",
            org.qiweb.modules.xml.internal.DatatypeFactoryImpl.class.getName()
        );
        System.setProperty(
            "javax.xml.parsers.DocumentBuilderFactory",
            org.qiweb.modules.xml.internal.DocumentBuilderFactoryImpl.class.getName()
        );
        System.setProperty(
            "javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema",
            org.qiweb.modules.xml.internal.SchemaFactoryImpl.class.getName()
        );

        // Saxon for XSLT, XSLT2, XPath and XQuery
        System.setProperty(
            "javax.xml.transform.TransformerFactory",
            org.qiweb.modules.xml.internal.TransformerFactoryImpl.class.getName()
        );
        System.setProperty(
            "javax.xml.xpath.XPathFactory:http://java.sun.com/jaxp/xpath/dom",
            org.qiweb.modules.xml.internal.XPathFactoryImpl.class.getName()
        );
    }

    private XML xml;

    @Override
    public Class<XML> apiType()
    {
        return XML.class;
    }

    @Override
    public XML api()
    {
        return xml;
    }

    @Override
    public void onActivate( Application application )
        throws ActivationException
    {
        Config config = application.config().object( "xml" );
        List<String> catalogs = config.stringList( "external_entities.catalogs" );
        boolean catalogsPreferPublic = config.bool( "external_entities.catalogs_prefer_public" );
        switch( config.string( "external_entities.resolver" ) )
        {
            case "throw":
                Internal.EXTERNAL_ENTITIES.set( false );
                Internal.RESOLVER.set( ThrowingResolver.INSTANCE );
                break;
            case "empty":
                Internal.EXTERNAL_ENTITIES.set( true );
                Internal.RESOLVER.set( EmptyResolver.INSTANCE );
                break;
            case "catalogs-only":
                Internal.EXTERNAL_ENTITIES.set( true );
                Internal.RESOLVER.set( new CatalogResolver( true, catalogs, catalogsPreferPublic ) );
                break;
            case "catalogs-first-unsafe":
                Internal.EXTERNAL_ENTITIES.set( true );
                Internal.RESOLVER.set( new CatalogResolver( false, catalogs, catalogsPreferPublic ) );
                break;
            case "no-catalog-unsafe":
                Internal.EXTERNAL_ENTITIES.set( true );
                Internal.RESOLVER.set( new CatalogResolver( false, EMPTY_LIST, catalogsPreferPublic ) );
                break;
            default:
                throw new QiWebException(
                    String.format(
                        "Uknown value for 'xml.external_entities' config property: %s",
                        config.string( "external_entities" )
                    )
                );
        }
        ensureXmlApisImplementations( true );
        xml = new XML( application.defaultCharset() );
    }

    @Override
    public void onPassivate( Application application )
    {
        xml = null;
        Internal.EXTERNAL_ENTITIES.set( false );
        Internal.RESOLVER.set( ThrowingResolver.INSTANCE );
    }

    private static void ensureXmlApisImplementations( boolean failOnError )
    {
        try
        {
            Class<?> xmlEventFactory = XMLEventFactory.newInstance().getClass();
            Class<?> xmlInputFactory = XMLInputFactory.newInstance().getClass();
            Class<?> xmlOutputFactory = XMLOutputFactory.newInstance().getClass();
            Class<?> saxParserFactory = SAXParserFactory.newInstance().getClass();
            Class<?> datatypeFactory = DatatypeFactory.newInstance().getClass();
            Class<?> docBuilderFactory = DocumentBuilderFactory.newInstance().getClass();
            Class<?> schemaFactory = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI ).getClass();
            Class<?> transformerFactory = TransformerFactory.newInstance().getClass();
            Class<?> xPathFactory = XPathFactory.newInstance().getClass();

            boolean wrong = Arrays.asList(
                xmlEventFactory, xmlInputFactory, xmlOutputFactory, saxParserFactory,
                datatypeFactory, docBuilderFactory, schemaFactory,
                transformerFactory, xPathFactory
            ).stream().anyMatch( c -> !c.getPackage().equals( Internal.class.getPackage() ) );

            if( LOG.isTraceEnabled() || wrong )
            {
                StringBuilder sb = new StringBuilder();
                sb.append( "  StAX & SAX" ).append( NEWLINE );
                sb.append( "    XMLEventFactory        " ).append( xmlEventFactory ).append( NEWLINE );
                sb.append( "    XMLInputFactory        " ).append( xmlInputFactory ).append( NEWLINE );
                sb.append( "    XMLOutputFactory       " ).append( xmlOutputFactory ).append( NEWLINE );
                sb.append( "    SAXParserFactory       " ).append( saxParserFactory ).append( NEWLINE );
                sb.append( "  DOM & Schema" ).append( NEWLINE );
                sb.append( "    DatatypeFactory        " ).append( datatypeFactory ).append( NEWLINE );
                sb.append( "    DocumentBuilderFactory " ).append( docBuilderFactory ).append( NEWLINE );
                sb.append( "    SchemaFactory          " ).append( schemaFactory ).append( NEWLINE );
                sb.append( "  XSLT & XPath/XQuery" ).append( NEWLINE );
                sb.append( "    TransformerFactory     " ).append( transformerFactory ).append( NEWLINE );
                sb.append( "    XPathFactory           " ).append( xPathFactory ).append( NEWLINE );
                sb.append( "  Entity Resolving" ).append( NEWLINE );
                sb.append( "    XML.Resolver           " ).append( Internal.RESOLVER.get().getClass() );
                sb.append( NEWLINE ).append( NEWLINE );
                if( wrong )
                {
                    sb.append( NEWLINE );
                    sb.append( "  All implementations should be under the " )
                        .append( XMLPlugin.class.getPackage().getName() )
                        .append( " package." ).append( NEWLINE );
                    sb.append( "  Watch out for superfluous XML libraries in your classpath!" )
                        .append( NEWLINE );
                }
                String report = sb.toString();
                if( wrong && failOnError )
                {
                    throw new QiWebException( "XML Plugin JAXP setup failure!\n\n" + report );
                }
                if( wrong )
                {
                    LOG.warn( "XML Plugin JAXP setup failure!\n\n{}", report );
                }
                else
                {
                    LOG.trace( "XML Plugin Runtime Summary\n\n{}", report );
                }
            }
        }
        catch( DatatypeConfigurationException ex )
        {
            throw new UncheckedXMLException( ex );
        }
    }
}
