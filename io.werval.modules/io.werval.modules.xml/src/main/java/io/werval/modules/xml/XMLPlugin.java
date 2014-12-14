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
package io.werval.modules.xml;

import io.werval.api.Application;
import io.werval.api.Config;
import io.werval.api.Plugin;
import io.werval.api.exceptions.ActivationException;
import io.werval.api.exceptions.WervalException;
import io.werval.modules.xml.internal.CatalogResolver;
import io.werval.modules.xml.internal.EmptyResolver;
import io.werval.modules.xml.internal.Internal;
import io.werval.modules.xml.internal.ThrowingResolver;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import static io.werval.util.Strings.NEWLINE;
import static java.util.Collections.EMPTY_LIST;

/**
 * XML Plugin.
 */
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

        if( false )
        {
            // Woodstox for stream pull parsing (StAX)
            System.setProperty(
                "javax.xml.stream.XMLEventFactory",
                io.werval.modules.xml.internal.XMLEventFactoryImpl.class.getName()
            );
            System.setProperty(
                "javax.xml.stream.XMLInputFactory",
                io.werval.modules.xml.internal.XMLInputFactoryImpl.class.getName()
            );
            System.setProperty(
                "javax.xml.stream.XMLOutputFactory",
                io.werval.modules.xml.internal.XMLOutputFactoryImpl.class.getName()
            );

            // Xerces for stream push parsing (SAX), DOM and Schema validation
            System.setProperty(
                "javax.xml.parsers.SAXParserFactory",
                io.werval.modules.xml.internal.SAXParserFactoryImpl.class.getName()
            );
            System.setProperty(
                "javax.xml.datatype.DatatypeFactory",
                io.werval.modules.xml.internal.DatatypeFactoryImpl.class.getName()
            );
            System.setProperty(
                "javax.xml.parsers.DocumentBuilderFactory",
                io.werval.modules.xml.internal.DocumentBuilderFactoryImpl.class.getName()
            );
            System.setProperty(
                "javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema",
                io.werval.modules.xml.internal.SchemaFactoryXSD.class.getName()
            );
            System.setProperty(
                "javax.xml.validation.SchemaFactory:http://www.w3.org/XML/XMLSchema/v1.1",
                io.werval.modules.xml.internal.SchemaFactoryXSD.class.getName()
            );
            System.setProperty(
                "javax.xml.validation.SchemaFactory:http://relaxng.org/ns/structure/1.",
                io.werval.modules.xml.internal.SchemaFactoryRelaxNG.class.getName()
            );

            // Saxon for XSLT, XSLT2, XPath and XQuery
            System.setProperty(
                "javax.xml.transform.TransformerFactory",
                io.werval.modules.xml.internal.TransformerFactoryImpl.class.getName()
            );
            System.setProperty(
                "javax.xml.xpath.XPathFactory:http://java.sun.com/jaxp/xpath/dom",
                io.werval.modules.xml.internal.XPathFactoryImpl.class.getName()
            );
        }
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
        List<String> catalogs = new ArrayList<>();
        for( String catalog : config.stringList( "external_entities.catalogs" ) )
        {
            try
            {
                catalogs.add( new URL( catalog ).toExternalForm() );
            }
            catch( MalformedURLException ex )
            {
                catalogs.add( application.classLoader().getResource( catalog ).toExternalForm() );
            }
        }
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
                throw new WervalException(
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
        throws ActivationException
    {
        try
        {
            XMLEventFactory xmlEventFactory = XMLEventFactory.newInstance();
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            SchemaFactory schemaFactory = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            XPathFactory xPathFactory = XPathFactory.newInstance();

            boolean wrong = Arrays.asList(
                xmlEventFactory, xmlInputFactory, xmlOutputFactory, saxParserFactory,
                datatypeFactory, docBuilderFactory, schemaFactory,
                transformerFactory, xPathFactory
            ).stream().anyMatch( c -> !c.getClass().getPackage().equals( Internal.class.getPackage() ) );

            if( LOG.isTraceEnabled() || wrong )
            {
                StringBuilder sb = new StringBuilder();
                sb.append( "  Support for XML 1.0" );
                if( saxParserFactory.getFeature( SAX.Features.XML_1_1 ) )
                {
                    sb.append( " & XML 1.1" );
                }
                sb.append( NEWLINE ).append( NEWLINE );
                sb.append( "  StAX" ).append( NEWLINE );
                sb.append( "    XMLEventFactory        " ).append( xmlEventFactory.getClass() ).append( NEWLINE );
                sb.append( "    XMLInputFactory        " ).append( xmlInputFactory.getClass() ).append( NEWLINE );
                sb.append( "    XMLOutputFactory       " ).append( xmlOutputFactory.getClass() ).append( NEWLINE );
                sb.append( "  SAX, DOM & Schema" ).append( NEWLINE );
                sb.append( "    SAXParserFactory       " ).append( saxParserFactory.getClass() ).append( NEWLINE );
                sb.append( "    DatatypeFactory        " ).append( datatypeFactory.getClass() ).append( NEWLINE );
                sb.append( "    DocumentBuilderFactory " ).append( docBuilderFactory.getClass() ).append( NEWLINE );
                sb.append( "    SchemaFactory          " ).append( schemaFactory.getClass() ).append( NEWLINE );
                sb.append( "  XSLT & XPath/XQuery" ).append( NEWLINE );
                sb.append( "    TransformerFactory     " ).append( transformerFactory.getClass() ).append( NEWLINE );
                sb.append( "    XPathFactory           " ).append( xPathFactory.getClass() ).append( NEWLINE );
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
                    throw new WervalException( "XML Plugin JAXP setup failure!\n\n" + report );
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
        catch( DatatypeConfigurationException | ParserConfigurationException | SAXException ex )
        {
            throw new ActivationException( ex );
        }
    }
}
