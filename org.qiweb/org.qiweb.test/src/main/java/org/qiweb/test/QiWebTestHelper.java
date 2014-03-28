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
package org.qiweb.test;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.qiweb.api.Errors;

/**
 * Internal QiWeb Test Helper.
 */
/* package */ class QiWebTestHelper
{
    /**
     * Print Errors traces to STDERR.
     *
     * @param errors Application's Errors
     */
    /* package */ static void printErrorsTrace( Errors errors )
    {
        if( errors.count() > 0 )
        {
            StringWriter writer = new StringWriter();
            PrintWriter pw = new PrintWriter( writer );
            pw.append( String.valueOf( errors.count() ) )
                .append( " Errors recorded by the QiWeb Application during the test:\n" );
            for( org.qiweb.api.Error error : errors.asList() )
            {
                pw.append( "\n" )
                    .append( "Error ID  :" ).append( error.errorId() ).append( "\n" )
                    .append( "Request ID:" ).append( error.requestId() ).append( "\n" )
                    .append( "Timestamp :" ).append( String.valueOf( error.timestamp() ) ).append( "\n" )
                    .append( "Message   :" ).append( error.message() ).append( "\n" )
                    .append( "Cause     :" ).append( error.cause().getMessage() ).append( "\n" );
                error.cause().printStackTrace( pw );
            }
            pw.flush();
            System.err.println( writer.toString() );
        }
    }
}
