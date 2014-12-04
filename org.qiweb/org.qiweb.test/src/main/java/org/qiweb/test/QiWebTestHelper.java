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
import java.lang.reflect.Field;
import org.qiweb.api.Config;
import org.qiweb.api.Errors;

import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_ADDRESS;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_PORT;

/**
 * Internal QiWeb Test Helper.
 */
/* package */ final class QiWebTestHelper
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

    /* package */ static void setupRestAssuredDefaults( Config config )
    {
        // Setup RestAssured defaults if present
        try
        {
            Field restAssuredPortField = Class.forName( "com.jayway.restassured.RestAssured" ).getField( "port" );
            restAssuredPortField.set( null, config.intNumber( QIWEB_HTTP_PORT ) );
            Field restAssuredBaseURLField = Class.forName( "com.jayway.restassured.RestAssured" ).getField( "baseURL" );
            restAssuredBaseURLField.set( null, "http://" + config.string( QIWEB_HTTP_ADDRESS ) );
        }
        catch( ClassNotFoundException | NoSuchFieldException |
               IllegalArgumentException | IllegalAccessException noRestAssured )
        {
            // RestAssured is not present, we simply don't configure it.
        }
    }

    private QiWebTestHelper()
    {
    }
}
