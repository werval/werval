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
package io.werval.runtime;

import com.jayway.restassured.response.Response;
import io.werval.api.Error;
import io.werval.api.context.CurrentContext;
import io.werval.api.outcomes.Outcome;
import io.werval.runtime.routes.RoutesParserProvider;
import io.werval.test.WervalHttpRule;
import java.io.IOException;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.expect;
import static io.werval.api.http.Headers.Names.CONNECTION;
import static io.werval.api.http.Headers.Names.X_QIWEB_REQUEST_ID;
import static io.werval.api.http.Headers.Values.CLOSE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

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
    public final WervalHttpRule werval = new WervalHttpRule( new RoutesParserProvider(
        "GET /success io.werval.runtime.OnApplicationErrorTest$Ctrl.success\n"
        + "GET /internalServerError io.werval.runtime.OnApplicationErrorTest$Ctrl.internalServerError\n"
        + "GET /exception io.werval.runtime.OnApplicationErrorTest$Ctrl.exception" )
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
        assertThat(werval.application().errors().count(), is( 0 ) );
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
        assertThat(werval.application().errors().count(), is( 0 ) );
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

        assertThat(werval.application().errors().count(), is( 1 ) );

        String requestId = response.header( X_QIWEB_REQUEST_ID );
        assertThat( requestId, notNullValue() );

        List<Error> requestErrors = werval.application().errors().ofRequest( requestId );
        assertThat( requestErrors.size(), is( 1 ) );

        Error requestError = requestErrors.get( 0 );
        assertThat( requestError.cause().getClass().getName(), equalTo( IOException.class.getName() ) );
        assertThat( requestError.cause().getMessage(), equalTo( "Wow an exception!" ) );
        assertThat( requestError.cause().getCause().getClass().getName(), equalTo( RuntimeException.class.getName() ) );
        assertThat( requestError.cause().getCause().getMessage(), equalTo( "This is a crash" ) );
    }
}
