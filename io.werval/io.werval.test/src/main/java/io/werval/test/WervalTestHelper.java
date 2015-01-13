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
package io.werval.test;

import io.werval.api.Config;
import io.werval.api.Errors;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;

import static io.werval.runtime.ConfigKeys.WERVAL_HTTP_ADDRESS;
import static io.werval.runtime.ConfigKeys.WERVAL_HTTP_PORT;

/**
 * Internal Werval Test Helper.
 */
/* package */ final class WervalTestHelper
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
                .append( " Errors recorded by the Werval Application during the test:\n" );
            for( io.werval.api.Error error : errors.asList() )
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
        Class<?> restAssured;
        try
        {
            restAssured = Class.forName( "com.jayway.restassured.RestAssured" );
        }
        catch( ClassNotFoundException noRestAssured )
        {
            // RestAssured is not present, we simply don't configure it.
            restAssured = null;
        }
        if( restAssured != null )
        {
            try
            {
                restAssured.getField( "port" ).set( null, config.intNumber( WERVAL_HTTP_PORT ) );
                restAssured.getField( "baseURI" ).set( null, "http://" + config.string( WERVAL_HTTP_ADDRESS ) );
                restAssured.getMethod( "enableLoggingOfRequestAndResponseIfValidationFails" ).invoke( null );
            }
            catch( NoSuchFieldException | NoSuchMethodException | InvocationTargetException |
                   IllegalArgumentException | IllegalAccessException ex )
            {
                System.err.println( "Unable to setup rest-assured" );
                ex.printStackTrace( System.err );
            }
        }
    }

    private WervalTestHelper()
    {
    }
}
