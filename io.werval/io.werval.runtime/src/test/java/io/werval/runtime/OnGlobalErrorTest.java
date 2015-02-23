/*
 * Copyright (c) 2014-2015 the original author or authors
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
import io.werval.api.Application;
import io.werval.api.Global;
import io.werval.api.Plugin;
import io.werval.api.context.Context;
import io.werval.api.filters.Filter;
import io.werval.api.filters.FilterChain;
import io.werval.api.filters.FilterWith;
import io.werval.api.http.RequestHeader;
import io.werval.api.outcomes.Outcome;
import io.werval.api.outcomes.Outcomes;
import io.werval.runtime.routes.RoutesParserProvider;
import io.werval.test.WervalHttpTest;
import io.werval.test.util.Slf4jRule;
import io.werval.util.Stacktraces;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.ClassRule;
import org.junit.Test;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.restassured.RestAssured.expect;
import static io.werval.api.context.CurrentContext.outcomes;
import static io.werval.api.http.Headers.Names.CONNECTION;
import static io.werval.api.http.Headers.Names.X_WERVAL_REQUEST_ID;
import static io.werval.api.http.Headers.Values.CLOSE;
import static io.werval.api.http.Headers.Values.KEEP_ALIVE;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class OnGlobalErrorTest
{
    @ClassRule
    public static final Slf4jRule SLF4J = new Slf4jRule()
    {
        {
            record( Level.WARN );
            recordForType( ApplicationInstance.class );
        }
    };

    public static class TestFilter
        implements Filter<Void>
    {
        @Override
        public CompletableFuture<Outcome> filter( FilterChain chain, Context context, Optional<Void> annotation )
        {
            return chain.next( context );
        }
    }

    public static class TestController
    {
        @FilterWith( TestFilter.class )
        public Outcome action()
        {
            return outcomes().ok().build();
        }

        public Outcome error()
        {
            throw new RuntimeException( "Error in Controller action" );
        }
    }

    //
    //                                        d8888          888    d8b                   888    d8b
    //                                       d88888          888    Y8P                   888    Y8P
    //                                      d88P888          888                          888
    //                                     d88P 888  .d8888b 888888 888 888  888  8888b.  888888 888  .d88b.  88888b.
    //                                    d88P  888 d88P"    888    888 888  888     "88b 888    888 d88""88b 888 "88b
    //                                   d88P   888 888      888    888 Y88  88P .d888888 888    888 888  888 888  888
    //                                  d8888888888 Y88b.    Y88b.  888  Y8bd8P  888  888 Y88b.  888 Y88..88P 888  888
    //                                 d88P     888  "Y8888P  "Y888 888   Y88P   "Y888888  "Y888 888  "Y88P"  888  888
    //
    public static class OnActivate
        extends Global
    {
        @Override
        public void onActivate( Application application )
        {
            throw new RuntimeException( "onActivate" );
        }
    }

    @Test( expected = RuntimeException.class )
    public void onActivate()
    {
        WervalHttpTest werval = new WervalHttpTest( "global-errors-test_onActivate.conf" );
        try
        {
            werval.beforeEachTestMethod();
        }
        finally
        {
            werval.afterEachTestMethod();
        }
    }

    public static class BeforeHttpBind
        extends Global
    {
        @Override
        public void beforeHttpBind( Application application )
        {
            throw new RuntimeException( "beforeHttpBind" );
        }
    }

    @Test( expected = RuntimeException.class )
    public void beforeHttpBind()
    {
        WervalHttpTest werval = new WervalHttpTest( "global-errors-test_beforeHttpBind.conf" );
        try
        {
            werval.beforeEachTestMethod();
        }
        finally
        {
            werval.afterEachTestMethod();
        }
    }

    public static class AfterHttpBind
        extends Global
    {
        @Override
        public void afterHttpBind( Application application )
        {
            throw new RuntimeException( "afterHttpBind" );
        }
    }

    @Test( expected = RuntimeException.class )
    public void afterHttpBind()
    {
        WervalHttpTest werval = new WervalHttpTest( "global-errors-test_afterHttpBind.conf" );
        try
        {
            werval.beforeEachTestMethod();
        }
        finally
        {
            werval.afterEachTestMethod();
        }
    }

    //
    //                        8888888b.                             d8b                   888    d8b
    //                        888   Y88b                            Y8P                   888    Y8P
    //                        888    888                                                  888
    //                        888   d88P  8888b.  .d8888b  .d8888b  888 888  888  8888b.  888888 888  .d88b.  88888b.
    //                        8888888P"      "88b 88K      88K      888 888  888     "88b 888    888 d88""88b 888 "88b
    //                        888        .d888888 "Y8888b. "Y8888b. 888 Y88  88P .d888888 888    888 888  888 888  888
    //                        888        888  888      X88      X88 888  Y8bd8P  888  888 Y88b.  888 Y88..88P 888  888
    //                        888        "Y888888  88888P'  88888P' 888   Y88P   "Y888888  "Y888 888  "Y88P"  888  888
    //
    public static class OnPassivate
        extends Global
    {
        @Override
        public void onPassivate( Application application )
        {
            throw new RuntimeException( "onPassivate" );
        }
    }

    @Test
    public void onPassivate()
    {
        WervalHttpTest werval = new WervalHttpTest( "global-errors-test_onPassivate.conf" );
        try
        {
            werval.beforeEachTestMethod();
        }
        finally
        {
            werval.afterEachTestMethod();
        }
    }

    public static class BeforeHttpUnbind
        extends Global
    {
        @Override
        public void beforeHttpUnbind( Application application )
        {
            throw new RuntimeException( "beforeHttpUnbind" );
        }
    }

    @Test
    public void beforeHttpUnbind()
    {
        WervalHttpTest werval = new WervalHttpTest( "global-errors-test_beforeHttpUnbind.conf" );
        try
        {
            werval.beforeEachTestMethod();
        }
        finally
        {
            werval.afterEachTestMethod();
        }
    }

    public static class AfterHttpUnbind
        extends Global
    {
        @Override
        public void afterHttpUnbind( Application application )
        {
            throw new RuntimeException( "afterHttpUnbind" );
        }
    }

    @Test
    public void afterHttpUnbind()
    {
        WervalHttpTest werval = new WervalHttpTest( "global-errors-test_afterHttpUnbind.conf" );
        try
        {
            werval.beforeEachTestMethod();
        }
        finally
        {
            werval.afterEachTestMethod();
        }
    }

    //
    //                                                          8888888b.  888                   d8b
    //                                                          888   Y88b 888                   Y8P
    //                                                          888    888 888
    //                                                          888   d88P 888 888  888  .d88b.  888 88888b.  .d8888b
    //                                                          8888888P"  888 888  888 d88P"88b 888 888 "88b 88K
    //                                                          888        888 888  888 888  888 888 888  888 "Y8888b.
    //                                                          888        888 Y88b 888 Y88b 888 888 888  888      X88
    //                                                          888        888  "Y88888  "Y88888 888 888  888  88888P'
    //                                                                                       888
    //                                                                                  Y8b d88P
    //                                                                                   "Y88P"
    //
    public static class ExtraPlugins
        extends Global
    {
        @Override
        public List<Plugin<?>> extraPlugins()
        {
            throw new RuntimeException( "extraPlugins" );
        }
    }

    @Test( expected = RuntimeException.class )
    public void extraPlugins()
    {
        WervalHttpTest werval = new WervalHttpTest( "global-errors-test_extraPlugins.conf" );
        try
        {
            werval.beforeEachTestMethod();
        }
        finally
        {
            werval.afterEachTestMethod();
        }
    }

    public static class GetPluginInstance
        extends Global
    {
        @Override
        public <T> T getPluginInstance( Application application, Class<T> pluginType )
        {
            throw new RuntimeException( "getPluginInstance" );
        }
    }

    @Test( expected = RuntimeException.class )
    public void getPluginInstance()
    {
        WervalHttpTest werval = new WervalHttpTest( "global-errors-test_getPluginInstance.conf" );
        try
        {
            werval.beforeEachTestMethod();
        }
        finally
        {
            werval.afterEachTestMethod();
        }
    }

    //
    //           8888888                   888                               d8b          888    d8b
    //             888                     888                               Y8P          888    Y8P
    //             888                     888                                            888
    //             888   88888b.  .d8888b  888888  8888b.  88888b.   .d8888b 888  8888b.  888888 888  .d88b.  88888b.
    //             888   888 "88b 88K      888        "88b 888 "88b d88P"    888     "88b 888    888 d88""88b 888 "88b
    //             888   888  888 "Y8888b. 888    .d888888 888  888 888      888 .d888888 888    888 888  888 888  888
    //             888   888  888      X88 Y88b.  888  888 888  888 Y88b.    888 888  888 Y88b.  888 Y88..88P 888  888
    //           8888888 888  888  88888P'  "Y888 "Y888888 888  888  "Y8888P 888 "Y888888  "Y888 888  "Y88P"  888  888
    //
    public static class GetFilterInstance
        extends Global
    {
        @Override
        public <T> T getFilterInstance( Application application, Class<T> filterType )
        {
            throw new RuntimeException( "getFilterInstance" );
        }
    }

    @Test
    public void getFilterInstance()
    {
        WervalHttpTest werval = new WervalHttpTest(
            "global-errors-test_getFilterInstance.conf",
            new RoutesParserProvider( "GET / io.werval.runtime.OnGlobalErrorTest$TestController.action" )
        );
        try
        {
            werval.beforeEachTestMethod();
            Response response = expect()
                .statusCode( 500 )
                .header( X_WERVAL_REQUEST_ID, notNullValue() )
                .header( CONNECTION, CLOSE )
                .when()
                .get( "/" );
            assertThat( werval.application().errors().count(), is( 1 ) );
            assertThat(
                werval.application().errors().lastOfRequest( response.header( X_WERVAL_REQUEST_ID ) ).get().message(),
                equalTo( "getFilterInstance" )
            );
        }
        finally
        {
            werval.afterEachTestMethod();
        }
    }

    public static class GetControllerInstance
        extends Global
    {
        @Override
        public <T> T getControllerInstance( Application application, Class<T> controllerType )
        {
            throw new RuntimeException( "getControllerInstance" );
        }
    }

    @Test
    public void getControllerInstance()
    {
        WervalHttpTest werval = new WervalHttpTest(
            "global-errors-test_getControllerInstance.conf",
            new RoutesParserProvider( "GET / io.werval.runtime.OnGlobalErrorTest$TestController.action" )
        );
        try
        {
            werval.beforeEachTestMethod();
            Response response = expect()
                .statusCode( 500 )
                .header( X_WERVAL_REQUEST_ID, notNullValue() )
                .header( CONNECTION, CLOSE )
                .when()
                .get( "/" );
            assertThat( werval.application().errors().count(), is( 1 ) );
            assertThat(
                werval.application().errors().lastOfRequest( response.header( X_WERVAL_REQUEST_ID ) ).get().message(),
                equalTo( "getControllerInstance" )
            );
        }
        finally
        {
            werval.afterEachTestMethod();
        }
    }

    //
    //                               8888888                                              888    d8b
    //                                 888                                                888    Y8P
    //                                 888                                                888
    //                                 888   88888b.  888  888  .d88b.   .d8888b  8888b.  888888 888  .d88b.  88888b.
    //                                 888   888 "88b 888  888 d88""88b d88P"        "88b 888    888 d88""88b 888 "88b
    //                                 888   888  888 Y88  88P 888  888 888      .d888888 888    888 888  888 888  888
    //                                 888   888  888  Y8bd8P  Y88..88P Y88b.    888  888 Y88b.  888 Y88..88P 888  888
    //                               8888888 888  888   Y88P    "Y88P"   "Y8888P "Y888888  "Y888 888  "Y88P"  888  888
    //
    public static class InvokeControllerMethod
        extends Global
    {
        @Override
        public Outcome invokeControllerMethod( Context context, Object controller )
        {
            throw new RuntimeException( "invokeControllerMethod" );
        }
    }

    @Test
    public void invokeControllerMethod()
    {
        WervalHttpTest werval = new WervalHttpTest(
            "global-errors-test_invokeControllerMethod.conf",
            new RoutesParserProvider( "GET / io.werval.runtime.OnGlobalErrorTest$TestController.action" )
        );
        try
        {
            werval.beforeEachTestMethod();
            Response response = expect()
                .statusCode( 500 )
                .header( X_WERVAL_REQUEST_ID, notNullValue() )
                .header( CONNECTION, CLOSE )
                .when()
                .get( "/" );
            assertThat( werval.application().errors().count(), is( 1 ) );
            assertThat(
                werval.application().errors().lastOfRequest( response.header( X_WERVAL_REQUEST_ID ) ).get().message(),
                equalTo( "invokeControllerMethod" )
            );
        }
        finally
        {
            werval.afterEachTestMethod();
        }
    }

    //
    //                             .d8888b.                                  888          888    d8b
    //                            d88P  Y88b                                 888          888    Y8P
    //                            888    888                                 888          888
    //                            888         .d88b.  88888b.d88b.  88888b.  888  .d88b.  888888 888  .d88b.  88888b.
    //                            888        d88""88b 888 "888 "88b 888 "88b 888 d8P  Y8b 888    888 d88""88b 888 "88b
    //                            888    888 888  888 888  888  888 888  888 888 88888888 888    888 888  888 888  888
    //                            Y88b  d88P Y88..88P 888  888  888 888 d88P 888 Y8b.     Y88b.  888 Y88..88P 888  888
    //                             "Y8888P"   "Y88P"  888  888  888 88888P"  888  "Y8888   "Y888 888  "Y88P"  888  888
    //                                                              888
    //                                                              888
    //                                                              888
    //
    public static class OnHttpRequestComplete
        extends Global
    {
        @Override
        public void onHttpRequestComplete( Application application, RequestHeader requestHeader )
        {
            throw new RuntimeException( "onHttpRequestComplete" );
        }
    }

    @Test
    public void onHttpRequestComplete()
    {
        WervalHttpTest werval = new WervalHttpTest(
            "global-errors-test_onHttpRequestComplete.conf",
            new RoutesParserProvider( "GET / io.werval.runtime.OnGlobalErrorTest$TestController.action" )
        );
        try
        {
            werval.beforeEachTestMethod();
            expect()
                .statusCode( 200 )
                .header( X_WERVAL_REQUEST_ID, notNullValue() )
                .header( CONNECTION, KEEP_ALIVE )
                .when()
                .get( "/" );
            await().until( () -> SLF4J.contains( "onHttpRequestComplete" ) );
            assertThat( werval.application().errors().count(), is( 0 ) );
        }
        finally
        {
            werval.afterEachTestMethod();
        }
    }

    //
    //                                                            8888888888
    //                                                            888
    //                                                            888
    //                                                            8888888    888d888 888d888  .d88b.  888d888 .d8888b
    //                                                            888        888P"   888P"   d88""88b 888P"   88K
    //                                                            888        888     888     888  888 888     "Y8888b.
    //                                                            888        888     888     Y88..88P 888          X88
    //                                                            8888888888 888     888      "Y88P"  888      88888P'
    //
    public static class GetRootCause
        extends Global
    {
        @Override
        public Throwable getRootCause( Throwable throwable )
        {
            throw new RuntimeException( "getRootCause" );
        }
    }

    @Test
    public void getRootCause()
    {
        WervalHttpTest werval = new WervalHttpTest(
            "global-errors-test_getRootCause.conf",
            new RoutesParserProvider( "GET / io.werval.runtime.OnGlobalErrorTest$TestController.error" )
        );
        try
        {
            werval.beforeEachTestMethod();
            expect()
                .statusCode( 500 )
                .header( X_WERVAL_REQUEST_ID, notNullValue() )
                .header( CONNECTION, CLOSE )
                .when()
                .get( "/" );
            assertThat( werval.application().errors().count(), is( 1 ) );
            assertThat(
                Stacktraces.containsMessage( "getRootCause" ).test(
                    werval.application().errors().last().get().cause()
                ),
                is( true )
            );
        }
        finally
        {
            werval.afterEachTestMethod();
        }
    }

    public static class OnRequestError
        extends Global
    {
        @Override
        public Outcome onRequestError(
            Application application, RequestHeader request, Outcomes outcomes, Throwable cause
        )
        {
            throw new RuntimeException( "onRequestError" );
        }
    }

    @Test
    public void onRequestError()
    {
        WervalHttpTest werval = new WervalHttpTest(
            "global-errors-test_onRequestError.conf",
            new RoutesParserProvider( "GET / io.werval.runtime.OnGlobalErrorTest$TestController.error" )
        );
        try
        {
            werval.beforeEachTestMethod();
            expect()
                .statusCode( 500 )
                .header( X_WERVAL_REQUEST_ID, notNullValue() )
                .header( CONNECTION, CLOSE )
                .when()
                .get( "/" );
            assertThat( werval.application().errors().count(), is( 1 ) );
            assertThat(
                Stacktraces.containsMessage( "onRequestError" ).test(
                    werval.application().errors().last().get().cause()
                ),
                is( true )
            );
        }
        finally
        {
            werval.afterEachTestMethod();
        }
    }
}
