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

import javax.xml.validation.Schema;
import java.io.InputStream;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Base XML Test.
 */
// TODO Validate!
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
     * <p>
     * Each concrete test lookup this in their dedicated Application.
     *
     * @return XML Plugin API
     */
    protected abstract XML xml();

    @Test
    public void parseThenAsString()
    {
        Document doc = xml().document( SIMPLE_DOC_XML );
        assertThat( xml().asString( doc ), allOf( containsString( SIMPLE_DOC_XML ), startsWith( "<?xml version=\"1" ) ) );
        assertThat( xml().asString( (Node) doc ), equalTo( SIMPLE_DOC_XML ) );
    }

    @Test
    public void transformDocumentToString()
    {
        String result = xml().toText( xml().document( SIMPLE_DOC_XML ), xml().xslt( SIMPLE_DOC_XSLT_TEXT ) );
        assertThat( result, equalTo( "bar" ) );
    }

    @Test
    public void booksNoValidation()
    {
        xml().document( books( "books.xml" ) );
        xml().document( books( "books_invalid.xml" ) );
    }

    @Test
    public void booksWithValidation()
        throws Exception
    {
        Schema schema = xml().xsd( books( "books.xsd" ) );
        xml().document( books( "books.xml" ), schema );
        try
        {
            xml().document( books( "books_invalid.xml" ), schema );
            fail( "Invalid books should not validate!" );
        }
        catch( UncheckedXMLException expected )
        {
        }
    }

    // ----------------------------------------------------------------------------------------------------------------
    // ----------------------------------------------------------------------------------------------------------------
    // Defensive Coding    
    // ----------------------------------------------------------------------------------------------------------------
    @Ignore( "DoS - ~1 second" )
    @Test
    public void internalEntityExponential_DOM()
    {
        try
        {
            xml().document( defensive( "XML-Parser-Internal_Entity_Exponential.xml" ), true );
            fail( "XML-Parser-Internal_Entity_Exponential.xml should not parse!" );
        }
        catch( UncheckedXMLException ex )
        {
            assertThat( ex.getMessage(), allOf( containsString( "parser" ), containsString( "limit" ) ) );
        }
    }

    @Ignore( "DoS - ~2 seconds" )
    @Test
    public void internalEntityExponentialAttribute_DOM()
    {
        try
        {
            xml().document( defensive( "XML-Parser-Internal_Entity_Exponential_Attribute.xml" ), true );
            fail( "XML-Parser-Internal_Entity_Exponential_Attribute.xml should not parse!" );
        }
        catch( UncheckedXMLException ex )
        {
            assertThat( ex.getMessage(), allOf( containsString( "parser" ), containsString( "limit" ) ) );
        }
    }

    @Ignore( "DoS - ~500 ms" )
    @Test
    public void internalEntityPolynomial_DOM()
    {
        try
        {
            xml().document( defensive( "XML-Parser-Internal_Entity_Polynomial.xml" ), true );
            fail( "XML-Parser-Internal_Entity_Polynomial.xml should not parse!" );
        }
        catch( UncheckedXMLException ex )
        {
            assertThat( ex.getMessage(), allOf( containsString( "parser" ), containsString( "limit" ) ) );
        }
    }

    @Ignore( "DoS - ~3 MINUTES !!!" )
    @Test
    public void internalEntityPolynomialAttribute_DOM()
    {
        try
        {
            xml().document( defensive( "XML-Parser-Internal_Entity_Polynomial_Attribute.xml" ), true );
            fail( "XML-Parser-Internal_Entity_Polynomial_Attribute.xml should not parse!" );
        }
        catch( UncheckedXMLException ex )
        {
            assertThat( ex.getMessage(), allOf( containsString( "parser" ), containsString( "limit" ) ) );
        }
    }

    @Ignore( "DoS - OutOfMemoryError !!!" )
    @Test
    public void internalRegexp1_DOM()
    {
        xml().document( defensive( "XML-Parser-Internal_Regexp_1.xml" ), true );
    }

    @Test
    public void internalRegexp2_DOM()
    {
        xml().document( defensive( "XML-Parser-Internal_Regexp_2.xml" ), true );
    }

    @Test
    public void internalRegexp3_DOM()
    {
        try
        {
            xml().document( defensive( "XML-Parser-Internal_Regexp_3.xml" ), true );
            fail( "XML-Parser-Internal_Regexp_3.xml should not parse!" );
        }
        catch( UncheckedXMLException ex )
        {
            assertThat( ex.getMessage(), containsString( "cannot occur within" ) );
        }
    }

    @Test
    public void dtdPublic_DOM()
    {
        xml().document( defensive( "XML-Parser-DTD_Public.xml" ), true );
    }

    @Test
    public void dtdPublicURL_DOM()
    {
        xml().document( defensive( "XML-Parser-DTD_Public_URL.xml" ), true );
    }

    @Test
    public void dtdSystem_DOM()
    {
        xml().document( defensive( "XML-Parser-DTD_System.xml" ), true );
    }

    @Test
    public void dtdSystemURL_DOM()
    {
        xml().document( defensive( "XML-Parser-DTD_System_URL.xml" ), true );
    }

    @Test
    public void notationPublic_DOM()
    {
        xml().document( defensive( "XML-Parser-Notation_Public.xml" ), true );
    }

    @Test
    public void notationPublicURL_DOM()
    {
        xml().document( defensive( "XML-Parser-Notation_Public_URL.xml" ), true );
    }

    @Test
    public void notationSystem_DOM()
    {
        xml().document( defensive( "XML-Parser-Notation_System.xml" ), true );
    }

    @Test
    public void notationSystemURL_DOM()
    {
        xml().document( defensive( "XML-Parser-Notation_System_URL.xml" ), true );
    }

    @Test
    public void xsdFile_DOM()
    {
        xml().document( defensive( "XML-Parser-XSD-File.xml" ) );
    }

    @Test
    public void xsdURL_DOM()
    {
        xml().document( defensive( "XML-Parser-XSD-URL.xml" ) );
    }

    @Test
    public void xsdIncludeFile_DOM()
    {
        xml().document( defensive( "XML-Parser-XSD-Include_File.xml" ) );
    }

    @Test
    public void xsdIncludeURL_DOM()
    {
        xml().document( defensive( "XML-Parser-XSD-Include_URL.xml" ) );
    }

    @Test
    public void xIncludeFile_DOM()
    {
        xml().document( defensive( "XML-Parser-XInclude-File.xml" ) );
    }

    @Test
    public void xIncludeURL_DOM()
    {
        xml().document( defensive( "XML-Parser-XInclude-URL.xml" ) );
    }

    @Test
    public void externalEntityPublic_DOM()
    {
        xml().document( defensive( "XML-Parser-External_Entity_Public.xml" ), true );
    }

    @Test
    public void externalEntityPublicURL_DOM()
    {
        xml().document( defensive( "XML-Parser-External_Entity_Public_URL.xml" ), true );
    }

    @Test
    public void externalEntitySystem_DOM()
    {
        xml().document( defensive( "XML-Parser-External_Entity_System.xml" ), true );
    }

    @Test
    public void externalEntitySystemURL_DOM()
    {
        xml().document( defensive( "XML-Parser-External_Entity_System_URL.xml" ), true );
    }

    @Test
    public void externalEntityRegexp3_DOM()
    {
        xml().document( defensive( "XML-Parser-External_Regexp_3.xml" ), true );
    }

    @Test
    public void validateRegexp1DTD_DOM()
    {
        try
        {
            xml().document( defensive( "XML-Parser-Validate-Regexp_1.xml" ), true );
        }
        catch( UncheckedXMLException ex )
        {
            assertThat( ex.getMessage(), containsString( "must match DOCTYPE" ) );
        }
    }

    @Ignore( "DoS - ~1 second" )
    @Test
    public void validateRegexp1XSD_DOM()
    {
        try
        {
            xml().document(
                defensive( "XML-Parser-Validate-Regexp_1.xml" ),
                xml().xsd( defensive( "XML-Parser-Validate-Regexp_1.xsd" ) )
            );
        }
        catch( UncheckedXMLException ex )
        {
            assertThat( ex.getMessage(), containsString( "Unique Particle Attribution" ) );
        }
    }

    @Test
    public void validateRegexp4XSD_DOM()
    {
        try
        {
            xml().document(
                defensive( "XML-Parser-Validate-Regexp_1.xml" ),
                xml().xsd( defensive( "XML-Parser-Validate-Regexp_4.xsd" ) )
            );
        }
        catch( UncheckedXMLException ex )
        {
            assertThat( ex.getMessage(), containsString( "expansion" ) );
        }
    }

    @Ignore( "DoS - ~1 second" )
    @Test
    public void validateRegexp1RelaxNG_DOM()
    {
        xml().document(
            defensive( "XML-Parser-Validate-Regexp_1.xml" ),
            xml().relaxNg( defensive( "XML-Parser-Validate-Regexp_1.rng" ) )
        );
    }

    private static InputStream books( String filename )
    {
        return TestBase.class.getClassLoader().getResourceAsStream( "books/" + filename );
    }

    private static InputStream defensive( String filename )
    {
        return TestBase.class.getClassLoader().getResourceAsStream( "defensive/" + filename );
    }
}
