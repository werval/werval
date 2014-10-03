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
package org.qiweb.api.util;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.qiweb.api.util.MapBuilder.fromMap;

/**
 * MapBuilder Test.
 */
public class MapBuilderTest
{
    @Test
    public void map()
    {
        Map<String, String> map = fromMap( new HashMap<String, String>() )
            .put( "foo", "bar" )
            .put( "bazar", "cathedral" )
            .toMap();

        assertThat( map.size(), is( 2 ) );
        assertThat( map.get( "foo" ), equalTo( "bar" ) );
        assertThat( map.get( "bazar" ), equalTo( "cathedral" ) );
    }

    @Test
    public void multiValueMap()
    {
        MultiValueMap<String, String> map = fromMap( new LinkedMultiValueMap<String, String>() )
            .add( "foo", "bar" )
            .add( "nil", "null", "undefined" )
            .toMap();

        assertThat( map.size(), is( 2 ) );
        assertThat( map.getFirst( "nil" ), equalTo( "null" ) );
        assertThat( map.getLast( "nil" ), equalTo( "undefined" ) );
        assertThat( map.getSingle( "foo" ), equalTo( "bar" ) );
    }
}
