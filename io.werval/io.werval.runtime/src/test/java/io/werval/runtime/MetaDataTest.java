/*
 * Copyright (c) 2013-2015 the original author or authors
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

import io.werval.api.MetaData;
import java.util.ArrayList;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class MetaDataTest
{
    @Test
    public void givenMetaDataWhenUsingTypedGetterExpectCorrectResults()
    {
        MetaData meta = new MetaData();
        meta.put( "foo", "bar" );
        meta.put( "integer", new Integer( 42 ) );

        assertThat( (String) meta.get( "foo" ), equalTo( "bar" ) );
        assertThat( meta.get( String.class, "foo" ), equalTo( "bar" ) );

        assertThat( (Integer) meta.get( "integer" ), equalTo( 42 ) );
        assertThat( meta.get( Integer.class, "integer" ), equalTo( 42 ) );

        assertNull( meta.get( "bazar" ) );
        assertFalse( meta.getOptional( ArrayList.class, "bazar" ).isPresent() );
    }
}
