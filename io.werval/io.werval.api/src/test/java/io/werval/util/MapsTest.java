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
package io.werval.util;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import org.junit.Test;

import static io.werval.util.Maps.fromMap;
import static io.werval.util.Maps.newConcurrentHashMap;
import static io.werval.util.Maps.newConcurrentSkipListMap;
import static io.werval.util.Maps.newHashMap;
import static io.werval.util.Maps.newIdentityHashMap;
import static io.werval.util.Maps.newLinkedHashMap;
import static io.werval.util.Maps.newLinkedMultiValueMap;
import static io.werval.util.Maps.newTreeMap;
import static io.werval.util.Maps.newWeakHashMap;
import static io.werval.util.Maps.unmodifiableMultiValueMap;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Maps Utilities Test.
 */
public class MapsTest
{
    @Test
    public void mapBuilder()
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
    public void multiValueMapBuilder()
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

    @Test
    public void mapBuilderUsages()
    {
        Map<String, String> hMap = newHashMap( String.class, String.class )
            .put( "foo", "bar" )
            .toMap();
        Map<String, String> lhMap = newLinkedHashMap( String.class, String.class )
            .put( "foo", "bar" )
            .toMap();
        SortedMap<String, String> tMap = newTreeMap( String.class, String.class )
            .put( "foo", "bar" )
            .toMap();
        SortedMap<String, String> tMapC = newTreeMap( String.class,
                                                      String.class,
                                                      (left, right) -> left.compareTo( right ) )
            .put( "foo", "bar" )
            .toMap();

        Map<String, String> ihMap = newIdentityHashMap( String.class, String.class )
            .put( "foo", "bar" )
            .toMap();
        Map<String, String> whMap = newWeakHashMap( String.class, String.class )
            .put( "foo", "bar" )
            .toMap();

        Map<String, String> chMap = newConcurrentHashMap( String.class, String.class )
            .put( "foo", "bar" )
            .toMap();
        SortedMap<String, String> cslMap = newConcurrentSkipListMap( String.class, String.class )
            .put( "foo", "bar" )
            .toMap();
        SortedMap<String, String> cslMapC = newConcurrentSkipListMap( String.class,
                                                                      String.class,
                                                                      (left, right) -> left.compareTo( right ) )
            .put( "foo", "bar" )
            .toMap();

        MultiValueMap<String, String> lmvMap = newLinkedMultiValueMap( String.class, String.class )
            .add( "foo", "bar" )
            .toMap();
    }

    @Test
    public void unmodifiableMVMap()
    {
        MultiValueMap<String, String> mvmap = newLinkedMultiValueMap( String.class, String.class )
            .add( "foo", "bar", "bazar" )
            .toMap();
        MultiValueMap<String, String> unmodifiable = unmodifiableMultiValueMap( mvmap );
        try
        {
            unmodifiable.keySet().remove( unmodifiable.keySet().iterator().next() );
            fail( "UnmodifiableMultiValueMap is modifiable!" );
        }
        catch( UnsupportedOperationException expected )
        {
        }
    }
}
