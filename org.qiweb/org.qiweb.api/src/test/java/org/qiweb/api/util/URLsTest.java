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
package org.qiweb.api.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.qiweb.api.util.Charsets.UTF_8;

public class URLsTest
{

    @Test
    public void testAppendQueryString()
    {
        String url = "http://acme.com/foo";

        assertThat( URLs.appendQueryString( url, null, UTF_8 ),
                    equalTo( "http://acme.com/foo" ) );
        assertThat( URLs.appendQueryString( url, Collections.<String, List<String>>emptyMap(), UTF_8 ),
                    equalTo( "http://acme.com/foo" ) );

        Map<String, List<String>> data = new LinkedHashMap<>();
        data.put( "foo", new ArrayList<String>() );
        data.get( "foo" ).add( "bar" );
        assertThat( URLs.appendQueryString( url, data, UTF_8 ),
                    equalTo( "http://acme.com/foo?foo=bar" ) );

        data.get( "foo" ).add( "bazar zogzog" );
        assertThat( URLs.appendQueryString( url, data, UTF_8 ),
                    equalTo( "http://acme.com/foo?foo=bar&foo=bazar+zogzog" ) );

        url = "http://acme.com/foo?foo=bar";
        assertThat( URLs.appendQueryString( url, data, UTF_8 ),
                    equalTo( "http://acme.com/foo?foo=bar&foo=bar&foo=bazar+zogzog" ) );

        url = "http://acme.com/foo?foo=bar#cathedral";
        assertThat( URLs.appendQueryString( url, data, UTF_8 ),
                    equalTo( "http://acme.com/foo?foo=bar&foo=bar&foo=bazar+zogzog#cathedral" ) );

    }
}
