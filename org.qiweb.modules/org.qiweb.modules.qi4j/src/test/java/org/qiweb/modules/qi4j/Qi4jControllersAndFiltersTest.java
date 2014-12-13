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
package org.qiweb.modules.qi4j;

import io.werval.api.Application;
import io.werval.api.context.Context;
import io.werval.api.filters.Filter;
import io.werval.api.filters.FilterChain;
import io.werval.api.filters.FilterWith;
import io.werval.api.outcomes.Outcome;
import io.werval.runtime.routes.RoutesParserProvider;
import io.werval.test.WervalHttpRule;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.ClassRule;
import org.junit.Test;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;

import static com.jayway.restassured.RestAssured.expect;
import static io.werval.api.context.CurrentContext.outcomes;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;

/**
 * Assert that using Qi4j Composites as Controllers and Filters work as expected.
 *
 * Controller under test is a Qi4j Service Composite.
 * <p>
 * Filter under test is a Qi4j Transient Composite.
 */
public class Qi4jControllersAndFiltersTest
{
    /**
     * QiWeb JUnit Class Rule.
     */
    @ClassRule
    public static final WervalHttpRule WERVAL = new WervalHttpRule(
        "qi4j-controllers-and-filters.conf",
        new RoutesParserProvider( "GET / org.qiweb.modules.qi4j.Qi4jControllersAndFiltersTest$TestController.index" )
    );
    /**
     * Name of the Qi4j Layer that contains the Controllers and Filters module.
     */
    private static final String LAYER = "Layer 1";
    /**
     * Name of the Qi4j Module in which Services Controller and Transients Filters are assembled.
     */
    private static final String MODULE = "Module 1";
    /**
     * Test state switch activated by the Filter.
     */
    private static boolean filtered = false;

    /**
     * QiWeb Controller as a Qi4j Service Composite.
     */
    @Mixins( TestController.Mixin.class )
    public static interface TestController
    {
        @FilterWith( TestFilter.class )
        Outcome index();

        public static class Mixin
            implements TestController
        {
            @Override
            public Outcome index()
            {
                return outcomes().ok( "Qi4j, new Energy for Java!" ).build();
            }
        }
    }

    /**
     * QiWeb Filter as a Qi4j Transient Composite.
     */
    @Mixins( TestFilter.Mixin.class )
    public static interface TestFilter
        extends Filter<Void>
    {
        public static class Mixin
            implements Filter<Void>
        {
            @Override
            public CompletableFuture<Outcome> filter( FilterChain chain, Context context, Optional<Void> filterConfig )
            {
                filtered = true;
                return chain.next( context );
            }
        }
    }

    /**
     * Qi4j Application Assembler.
     *
     * Assemble QiWeb Controller under test as a Service Composite.
     * <p>
     * Assemble QiWeb Filter under test as a Transient Composite.
     */
    public static class AppAssembler
        implements ApplicationAssembler
    {
        @Override
        public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
            throws AssemblyException
        {
            ApplicationAssembly assembly = applicationFactory.newApplicationAssembly();
            ModuleAssembly module = assembly.layer( LAYER ).module( MODULE );
            module.services( TestController.class );
            module.transients( TestFilter.class );
            return assembly;
        }
    }

    /**
     * QiWeb Global Object.
     *
     * Get QiWeb Controllers instances from Qi4j Services.
     * <p>
     * Get QiWeb Filters instances from Qi4j Transients.
     */
    public static class Global
        extends io.werval.api.Global
    {
        @Override
        public <T> T getControllerInstance( Application application, Class<T> controllerType )
        {
            return application.
                plugin( org.qi4j.api.structure.Application.class ).
                findModule( LAYER, MODULE ).
                findService( controllerType ).get();
        }

        @Override
        public <T> T getFilterInstance( Application application, Class<T> filterType )
        {
            return application.
                plugin( org.qi4j.api.structure.Application.class ).
                findModule( LAYER, MODULE ).
                newTransient( filterType );
        }
    }

    /**
     * Assert.
     */
    @Test
    public void test()
    {
        expect().
            statusCode( 200 ).
            body( containsString( "Qi4j, new Energy for Java!" ) ).
            when().
            get( "/" );
        assertThat( filtered, is( true ) );
    }
}
