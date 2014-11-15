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
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.qiweb.api.context.CurrentContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import static org.qiweb.util.Maps.fromMap;

/**
 * XML.
 * <p>
 * Manage configured instances of XML APIs providing utility methods for frequent use cases.
 * Application configuration allow to set up thoses managed instances.
 */
public final class XML
{
    /**
     * Current XML Plugin API.
     *
     * @return Current XML Plugin API
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

    public Document parse( String xmlDocString )
    {
        return parse( xmlDocString, defaultCharset );
    }

    public Document parse( String xmlDocString, Charset charset )
    {
        return parse( xmlDocString.getBytes( charset ) );
    }

    public Document parse( byte[] xmlDocBytes )
    {
        return parse( new ByteArrayInputStream( xmlDocBytes ) );
    }

    public Document parse( File xmlDocFile )
    {
        try( InputStream xmlDocStream = new FileInputStream( xmlDocFile ) )
        {
            return parse( xmlDocStream );
        }
        catch( IOException ex )
        {
            throw new UncheckedIOException( ex );
        }
    }

    public Document parse( Path xmlDocPath )
    {
        try( InputStream xmlDocStream = Files.newInputStream( xmlDocPath ) )
        {
            return parse( xmlDocStream );
        }
        catch( IOException ex )
        {
            throw new UncheckedIOException( ex );
        }
    }

    public Document parse( InputStream xmlDocStream )
    {
        try
        {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( xmlDocStream );
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

    public void validate( Document document )
    {

    }

    public String transformToText( Document document, String xsltString )
    {
        return transformToText( document, xsltString, defaultCharset );
    }

    public String transformToText( Document document, String xsltString, Charset xsltCharset )
    {
        return transformToText( document, xsltString.getBytes( xsltCharset ) );
    }

    public String transformToText( Document document, byte[] xsltBytes )
    {
        return transformToText( document, new ByteArrayInputStream( xsltBytes ) );
    }

    public String transformToText( Document document, InputStream xsltStream )
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        transformTo( document, xsltStream, output );
        return new String( output.toByteArray() );
    }

    public Document transformToDocument( Document document, InputStream xsltStream )
    {
        return (Document) transformToNode( document, xsltStream );
    }

    public Node transformToNode( Document document, InputStream xsltStream )
    {
        DOMResult domResult = new DOMResult();
        transformTo( new DOMSource( document ), xsltStream, domResult );
        return domResult.getNode();
    }

    public void transformTo( Document document, InputStream xsltStream, OutputStream output )
    {
        transformTo( new DOMSource( document ), xsltStream, new StreamResult( output ) );
    }

    public void transformTo( InputStream docStream, InputStream xsltStream, OutputStream output )
    {
        transformTo( new StreamSource( docStream ), xsltStream, new StreamResult( output ) );
    }

    private void transformTo( Source input, InputStream xsltStream, Result output )
    {
        try
        {
            Transformer transformer = TransformerFactory.newInstance().newTransformer( new StreamSource( xsltStream ) );
            transformer.transform( input, output );
        }
        catch( TransformerException ex )
        {
            throw new UncheckedXMLException( ex );
        }
    }
}
