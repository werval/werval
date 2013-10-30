/**
 * Copyright (c) 2013 the original author or authors
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
package org.qiweb.runtime;

import org.junit.Test;
import org.qiweb.api.Config;
import org.qiweb.api.Error;
import org.qiweb.api.Errors;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Assert that the Errors API implementation in Runtime behave correctly.
 */
public class ErrorsTest
{

    @Test
    public void testErrors()
    {
        Config config = new ConfigInstance();
        Errors errors = new ErrorsInstance( config );
        assertThat( errors.count(), is( 0 ) );
        assertThat( errors.get( "whatever" ), nullValue() );
        assertThat( errors.iterator().hasNext(), is( false ) );
        assertThat( errors.asList().isEmpty(), is( true ) );
        assertThat( errors.last(), nullValue() );
        assertThat( errors.ofRequest( "whatever" ).isEmpty(), is( true ) );
        assertThat( errors.lastOfRequest( "whatever" ), nullValue() );

        Error firstError = errors.record( "request-identity", "first-error-message", new RuntimeException( "First Recorded cause" ) );
        Error lastError = errors.record( "request-identity", "last-error-message", new RuntimeException( "Last Recorded cause" ) );
        assertThat( errors.count(), is( 2 ) );
        assertThat( errors.get( firstError.errorId() ), equalTo( firstError ) );
        assertThat( errors.get( lastError.errorId() ), equalTo( lastError ) );
        assertThat( errors.asList().isEmpty(), is( false ) );
        assertThat( errors.asList().size(), is( 2 ) );
        assertThat( errors.asList().get( 0 ), equalTo( lastError ) );
        assertThat( errors.asList().get( 1 ), equalTo( firstError ) );
        assertThat( errors.last(), equalTo( lastError ) );
        assertThat( errors.ofRequest( "request-identity" ).isEmpty(), is( false ) );
        assertThat( errors.ofRequest( "request-identity" ).get( 0 ), equalTo( lastError ) );
        assertThat( errors.ofRequest( "request-identity" ).get( 1 ), equalTo( firstError ) );
        assertThat( errors.lastOfRequest( "request-identity" ), equalTo( lastError ) );
    }

    @Test
    public void testMaxErrors()
    {
        Config config = new ConfigInstance();
        Errors errors = new ErrorsInstance( config );
        for( int idx = 1; idx <= 20; idx++ )
        {
            errors.record( "request-identity", "error-message-" + idx, new RuntimeException( "Recorded cause " + idx ) );
        }
        assertThat( errors.count(), is( 10 ) );
        assertThat( errors.last().message(), equalTo( "error-message-20" ) );
    }

}
