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

import org.xml.sax.SAXParseException;

/**
 * Unchecked XML Exception.
 */
public class UncheckedXMLException
    extends RuntimeException
{
    public UncheckedXMLException( String message )
    {
        super( message );
    }

    public UncheckedXMLException( Exception ex )
    {
        super( message( ex ), ex );
    }

    private static String message( Exception ex )
    {
        if( ex instanceof SAXParseException )
        {
            // Preserve error location if any
            return ex.toString();
        }
        return ex.getMessage();
    }
}
