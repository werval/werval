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
package io.werval.modules.cache;

import io.werval.api.cache.Cache;
import io.werval.api.outcomes.Outcome;
import io.werval.runtime.routes.RoutesParserProvider;
import io.werval.test.WervalHttpRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import io.werval.modules.metrics.Tools;

import static com.jayway.restassured.RestAssured.expect;
import static io.werval.api.context.CurrentContext.plugin;
import static io.werval.api.mime.MimeTypes.APPLICATION_JSON;
import static io.werval.test.util.Assume.assumeConnectivity;
import static org.hamcrest.CoreMatchers.is;

/**
 * Memcache Plugin Metrics Test.
 */
public class MemcacheCacheMetricsTest
{
    @BeforeClass
    public static void beforeMemcacheTests()
    {
        assumeConnectivity( "localhost", 11211 );
    }

    @ClassRule
    public static WervalHttpRule WERVAL = new WervalHttpRule( new RoutesParserProvider(
        "GET / " + Controller.class.getName() + ".interaction"
    ) );

    public static class Controller
    {
        public Outcome interaction()
            throws Exception
        {
            Cache cache = plugin( Cache.class );

            // get & miss
            cache.has( "foo" );
            cache.get( "foo" );

            // get & miss & put
            cache.getOrSetDefault( "foo", "bar" );

            // put
            cache.set( "foo", "bazar" );

            // get & hit
            cache.get( "foo" );

            // get & hit
            cache.getOrSetDefault( "foo", "bar" );

            // remove
            cache.remove( "foo" );

            // get & miss
            cache.get( "foo" );

            return new Tools().metrics();
        }
    }

    @Test
    public void ehCacheMetrics()
        throws Exception
    {
        expect()
            .statusCode( 200 )
            .contentType( APPLICATION_JSON )
            .body( "meters.'io.werval.modules.cache.memcache.werval-cache.hits'.count", is( 2 ) )
            .body( "meters.'io.werval.modules.cache.memcache.werval-cache.misses'.count", is( 4 ) )
            .body( "timers.'io.werval.modules.cache.memcache.werval-cache.gets'.count", is( 6 ) )
            .body( "timers.'io.werval.modules.cache.memcache.werval-cache.sets'.count", is( 2 ) )
            .body( "timers.'io.werval.modules.cache.memcache.werval-cache.removes'.count", is( 1 ) )
            .when()
            .get( "/" );
    }
}
