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

import io.werval.api.context.CurrentContext;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import static io.werval.util.Maps.fromMap;

/**
 * XML.
 * <p>
 * Manage configured instances of XML APIs providing utility methods for frequent use cases.
 * Application configuration allow to set up thoses managed instances.
 */
public class XML
{
    /**
     * Active XML Plugin API.
     *
     * @return Active XML Plugin API
     *
     * @throws IllegalArgumentException if no {@literal XML Plugin} is found
     * @throws IllegalStateException    if the {@literal Application} is not active
     */
    public static XML xml()
    {
        return CurrentContext.plugin( XML.class );
    }

    private final Charset defaultCharset;

    public XML( Charset defaultCharset )
    {
        this.defaultCharset = defaultCharset;
    }

    // TODO Add shorthands to the Plugin's API for every XML APIs, like this one, or not
    public XMLInputFactory newXMLInputFactory()
    {
        return XMLInputFactory.newFactory();
    }

    public String asString( Document document )
    {
        return asString( document, defaultCharset );
    }

    public String asString( Document document, Charset charset )
    {
        return new String( asBytes( document ), charset );
    }

    public byte[] asBytes( Document document )
    {
        return asBytes(
            document,
            fromMap( new HashMap<String, String>() )
            .put( OutputKeys.ENCODING, defaultCharset.name() )
            .put( OutputKeys.METHOD, "xml" )
            .put( OutputKeys.OMIT_XML_DECLARATION, "no" )
            .put( OutputKeys.INDENT, "yes" )
            .toMap()
        );
    }

    public String asString( Node node )
    {
        return asString( node, defaultCharset );
    }

    public String asString( Node node, Charset charset )
    {
        return new String( asBytes( node ), charset );
    }

    public byte[] asBytes( Node node )
    {
        return asBytes(
            node,
            fromMap( new HashMap<String, String>() )
            .put( OutputKeys.ENCODING, defaultCharset.name() )
            .put( OutputKeys.METHOD, "xml" )
            .put( OutputKeys.OMIT_XML_DECLARATION, "yes" )
            .put( OutputKeys.INDENT, "no" )
            .toMap()
        );
    }

    private byte[] asBytes( Node node, Map<String, String> outputProperties )
    {
        try
        {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            outputProperties.forEach(
                (name, value) ->
                {
                    transformer.setOutputProperty( name, value );
                }
            );
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            transformer.transform( new DOMSource( node ), new StreamResult( output ) );
            return output.toByteArray();
        }
        catch( TransformerException ex )
        {
            throw new UncheckedXMLException( ex );
        }
    }

    //  ______
    // |   __ \.---.-.----.-----.-----.
    // |    __/|  _  |   _|__ --|  -__|
    // |___|   |___._|__| |_____|_____|
    // _________________________________________________________________________________________________________________
    //
    public XMLEventReader staxEvents( InputStream input )
    {
        try
        {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            return factory.createXMLEventReader( input );
        }
        catch( XMLStreamException ex )
        {
            throw new UncheckedXMLException( ex );
        }
    }

    public XMLStreamReader staxStream( InputStream input )
    {
        try
        {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            return factory.createXMLStreamReader( input );
        }
        catch( XMLStreamException ex )
        {
            throw new UncheckedXMLException( ex );
        }
    }

    public void sax( InputStream input, DefaultHandler handler )
    {
        sax( input, handler, false );
    }

    public void sax( InputStream input, DefaultHandler handler, boolean dtdValidation )
    {
        try
        {
            SAXParserFactory saxFactory = SAXParserFactory.newInstance();
            saxFactory.setValidating( dtdValidation );
            saxFactory.newSAXParser().parse( input, handler );
        }
        catch( ParserConfigurationException | SAXException ex )
        {
            throw new UncheckedXMLException( ex );
        }
        catch( IOException ex )
        {
            throw new UncheckedIOException( ex );
        }
    }

    public Document document( String string )
    {
        return document( string, defaultCharset );
    }

    public Document document( String string, Charset charset )
    {
        return document( string.getBytes( charset ) );
    }

    public Document document( byte[] bytes )
    {
        try( InputStream xmlDocStream = new ByteArrayInputStream( bytes ) )
        {
            return document( xmlDocStream );
        }
        catch( IOException ex )
        {
            throw new UncheckedIOException( ex );
        }
    }

    public Document document( File file )
    {
        try( InputStream xmlDocStream = new FileInputStream( file ) )
        {
            return document( xmlDocStream );
        }
        catch( IOException ex )
        {
            throw new UncheckedIOException( ex );
        }
    }

    public Document document( Path path )
    {
        try( InputStream xmlDocStream = Files.newInputStream( path ) )
        {
            return document( xmlDocStream );
        }
        catch( IOException ex )
        {
            throw new UncheckedIOException( ex );
        }
    }

    public Document document( InputStream input )
    {
        return document( input, false, true, null );
    }

    public Document document( InputStream input, boolean dtdValidation )
    {
        return document( input, dtdValidation, false, null );
    }

    public Document document( InputStream input, Schema schema )
    {
        return document( input, false, true, schema );
    }

    private Document document( InputStream input, boolean dtdValidation, boolean namespaceAware, Schema schema )
    {
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating( dtdValidation );
            factory.setNamespaceAware( namespaceAware );
            factory.setSchema( schema );
            return factory.newDocumentBuilder().parse( input );
        }
        catch( ParserConfigurationException | SAXException ex )
        {
            throw new UncheckedXMLException( ex );
        }
        catch( IOException ex )
        {
            throw new UncheckedIOException( ex );
        }
    }

    public Node node( InputStream input )
    {
        return document( input, null );
    }

    public Node node( InputStream input, Schema schema )
    {
        return document( input, schema );
    }

    public Schema xsd( InputStream input )
    {
        return schema( input, XMLConstants.W3C_XML_SCHEMA_NS_URI );
    }

    public Schema xsd_1_1( InputStream input )
    {
        return schema( input, "http://www.w3.org/XML/XMLSchema/v1.1" );
    }

    public Schema relaxNg( InputStream input )
    {
        return schema( input, XMLConstants.RELAXNG_NS_URI );
    }

    public Schema schema( InputStream input, String schemaLanguage )
    {
        try
        {
            return SchemaFactory.newInstance( schemaLanguage ).newSchema( new StreamSource( input ) );
        }
        catch( SAXException ex )
        {
            throw new UncheckedXMLException( ex );
        }
    }

    public Transformer xslt( String string )
    {
        return xslt( string, defaultCharset );
    }

    public Transformer xslt( String string, Charset charset )
    {
        return xslt( new ByteArrayInputStream( string.getBytes( charset ) ) );
    }

    public Transformer xslt( InputStream input )
    {
        try
        {
            return TransformerFactory.newInstance().newTransformer( new StreamSource( input ) );
        }
        catch( TransformerConfigurationException ex )
        {
            throw new UncheckedXMLException( ex );
        }
    }

    //
    //  _______                          ___
    // |_     _|.----.---.-.-----.-----.'  _|.-----.----.--------.
    //   |   |  |   _|  _  |     |__ --|   _||  _  |   _|        |
    //   |___|  |__| |___._|__|__|_____|__|  |_____|__| |__|__|__|
    // _________________________________________________________________________________________________________________
    //
    public String toText( Document document, String xsltString )
    {
        return toText( document, xsltString, defaultCharset );
    }

    public String toText( Document document, String xsltString, Charset xsltCharset )
    {
        return toText( document, xsltString.getBytes( xsltCharset ) );
    }

    public String toText( Document document, byte[] xsltBytes )
    {
        return toText( document, new ByteArrayInputStream( xsltBytes ) );
    }

    public String toText( Document document, InputStream xsltStream )
    {
        return toText( document, xslt( xsltStream ) );
    }

    public String toText( Document document, Transformer transformer )
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        to( document, transformer, output );
        return new String( output.toByteArray() );
    }

    public Document toDocument( Document document, InputStream xsltStream )
    {
        return (Document) toNode( document, xsltStream );
    }

    public Node toNode( Node node, InputStream xsltStream )
    {
        DOMResult domResult = new DOMResult();
        to( new DOMSource( node ), xslt( xsltStream ), domResult );
        return domResult.getNode();
    }

    public void to( Node node, Transformer transformer, OutputStream output )
    {
        to( new DOMSource( node ), transformer, new StreamResult( output ) );
    }

    public void to( InputStream input, Transformer transformer, OutputStream output )
    {
        to( new StreamSource( input ), transformer, new StreamResult( output ) );
    }

    private void to( Source input, Transformer transformer, Result output )
    {
        try
        {
            transformer.transform( input, output );
        }
        catch( TransformerException ex )
        {
            throw new UncheckedXMLException( ex );
        }
    }
}
