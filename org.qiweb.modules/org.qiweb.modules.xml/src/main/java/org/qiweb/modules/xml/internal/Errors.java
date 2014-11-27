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

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import static org.qiweb.util.Strings.SPACE;

/**
 * XML Parsing Errors Handler.
 * <p>
 * That is:
 * <ul>
 * <li>{@link javax.xml.stream.XMLReporter} for StAX</li>
 * <li>{@link org.xml.sax.ErrorHandler} for SAX, DOM and Validation</li>
 * <li>{@link javax.xml.transform.ErrorListener} for XSLT and Transformation</li>
 * </ul>
 */
/* package */ class Errors
    implements javax.xml.stream.XMLReporter, org.xml.sax.ErrorHandler, javax.xml.transform.ErrorListener
{
    /* package */ static final Errors INSTANCE = new Errors();

    @Override
    public void report( String message, String errorType, Object relatedInformation, Location location )
        throws XMLStreamException
    {
        Internal.LOG.warn( message + SPACE + errorType + SPACE + relatedInformation + SPACE + location );
    }

    @Override
    public void warning( SAXParseException exception )
        throws SAXException
    {
        Internal.LOG.debug( logMessage( exception ) );
    }

    @Override
    public void error( SAXParseException exception )
        throws SAXException
    {
        throw exception;
    }

    @Override
    public void fatalError( SAXParseException exception )
        throws SAXException
    {
        throw exception;
    }

    private static String logMessage( SAXParseException exception )
    {
        return exception.getMessage() + SPACE
               + exception.getSystemId() + SPACE + exception.getPublicId()
               + SPACE + exception.getLineNumber() + ':' + exception.getColumnNumber();
    }

    @Override
    public void warning( TransformerException exception )
        throws TransformerException
    {
        Internal.LOG.debug( exception.getMessageAndLocation() );
    }

    @Override
    public void error( TransformerException exception )
        throws TransformerException
    {
        throw exception;
    }

    @Override
    public void fatalError( TransformerException exception )
        throws TransformerException
    {
        throw exception;
    }
}
