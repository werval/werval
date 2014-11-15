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

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Base XML Test.
 */
public abstract class TestBase
{
    protected static final String SIMPLE_DOC_XML = "<foo>bar</foo>";
    protected static final String SIMPLE_DOC_XSLT_TEXT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                                                         + "<xsl:stylesheet \n"
                                                         + "    version=\"1.0\"\n"
                                                         + "    xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n"
                                                         + "  <xsl:output method=\"text\" encoding=\"UTF-8\"/>\n"
                                                         + " \n"
                                                         + "  <!-- simply copy the foo text to the result tree -->\n"
                                                         + "  <xsl:template match=\"/\">\n"
                                                         + "    <xsl:value-of select=\"foo\"/>\n"
                                                         + "  </xsl:template>\n"
                                                         + "</xsl:stylesheet> ";

    /**
     * XML Plugin API lookup.
     *
     * @return XML Plugin API
     */
    protected abstract XML xml();

    @Test
    public void parseThenAsString()
    {
        Document doc = xml().parse( SIMPLE_DOC_XML );
        assertThat( xml().asString( doc ), allOf( containsString( SIMPLE_DOC_XML ), startsWith( "<?xml version=\"1" ) ) );
        assertThat( xml().asString( (Node) doc ), equalTo( SIMPLE_DOC_XML ) );
    }

    @Test
    public void transformDocumentToString()
    {
        String result = xml().transformToText( xml().parse( SIMPLE_DOC_XML ), SIMPLE_DOC_XSLT_TEXT );
        assertThat( result, equalTo( "bar" ) );
    }

    @Test
    public void booksNoValidation()
    {
        Document books = xml().parse( getClass().getResourceAsStream( "books.xml" ) );
        System.out.println( xml().asString( books ) );
        Document invalidBooks = xml().parse( getClass().getResourceAsStream( "books_invalid.xml" ) );
    }

    @Test
    public void booksWithValidation()
        throws Exception
    {
        Schema schema = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI )
            .newSchema( getClass().getResource( "books.xsd" ) );

        DocumentBuilderFactory docBuilder = DocumentBuilderFactory.newInstance();
        docBuilder.setValidating( true );
        docBuilder.setNamespaceAware( true );
        docBuilder.setSchema( schema );
        docBuilder.newDocumentBuilder().parse( getClass().getResourceAsStream( "books.xml" ) );
        try
        {
            docBuilder.newDocumentBuilder().parse( getClass().getResourceAsStream( "books_invalid.xml" ) );
            fail( "Invalid books should not validate!" );
        }
        catch( SAXException expected )
        {

        }
    }

    @Test
    public void whoup()
        throws ParserConfigurationException, SAXException
    {
        SAXParserFactory.newInstance().newSAXParser().getXMLReader();
    }
}
