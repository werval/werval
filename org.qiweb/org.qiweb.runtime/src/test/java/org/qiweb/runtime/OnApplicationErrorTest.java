/*
 * Copyright (c) 2013-2014 the original author or authors
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

import com.jayway.restassured.response.Response;
import java.io.IOException;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.qiweb.api.Error;
import org.qiweb.api.context.CurrentContext;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.runtime.routes.RoutesParserProvider;
import org.qiweb.test.QiWebHttpRule;

import static com.jayway.restassured.RestAssured.expect;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.qiweb.api.http.Headers.Names.CONNECTION;
import static org.qiweb.api.http.Headers.Names.X_QIWEB_REQUEST_ID;
import static org.qiweb.api.http.Headers.Values.CLOSE;

/**
 * Assert that Application errors triggers the right code paths.
 */
public class OnApplicationErrorTest
{
    public static class Ctrl
    {
        public Outcome success()
        {
            return CurrentContext.outcomes().ok().build();
        }

        public Outcome internalServerError()
        {
            return CurrentContext.outcomes().internalServerError().build();
        }

        public Outcome exception()
            throws IOException
        {
            throw new IOException( "Wow an exception!", new RuntimeException( "This is a crash" ) );
        }
    }

    @Rule
    public final QiWebHttpRule qiweb = new QiWebHttpRule( new RoutesParserProvider(
        "GET /success org.qiweb.runtime.OnApplicationErrorTest$Ctrl.success\n"
        + "GET /internalServerError org.qiweb.runtime.OnApplicationErrorTest$Ctrl.internalServerError\n"
        + "GET /exception org.qiweb.runtime.OnApplicationErrorTest$Ctrl.exception" )
    );

    @Test
    public void given_200_then_no_error()
        throws IOException
    {
        expect().
            statusCode( 200 ).
            header( X_QIWEB_REQUEST_ID, notNullValue() ).
            when().
            get( "/success" );
        assertThat( qiweb.application().errors().count(), is( 0 ) );
    }

    @Test
    public void given_500_from_controller_then_no_error()
        throws IOException
    {
        expect().
            statusCode( 500 ).
            header( X_QIWEB_REQUEST_ID, notNullValue() ).
            header( CONNECTION, CLOSE ).
            when().
            get( "/internalServerError" );
        assertThat( qiweb.application().errors().count(), is( 0 ) );
    }

    @Test
    public void given_exception_in_controller_then_500_and_error_recorded()
        throws IOException
    {
        Response response = expect().
            statusCode( 500 ).
            header( CONNECTION, CLOSE ).
            when().
            get( "/exception" );

        assertThat( qiweb.application().errors().count(), is( 1 ) );

        String requestId = response.header( X_QIWEB_REQUEST_ID );
        assertThat( requestId, notNullValue() );

        List<Error> requestErrors = qiweb.application().errors().ofRequest( requestId );
        assertThat( requestErrors.size(), is( 1 ) );

        Error requestError = requestErrors.get( 0 );
        assertThat( requestError.cause().getClass().getName(), equalTo( IOException.class.getName() ) );
        assertThat( requestError.cause().getMessage(), equalTo( "Wow an exception!" ) );
        assertThat( requestError.cause().getCause().getClass().getName(), equalTo( RuntimeException.class.getName() ) );
        assertThat( requestError.cause().getCause().getMessage(), equalTo( "This is a crash" ) );
    }
}
