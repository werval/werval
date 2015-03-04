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
package io.werval.runtime.http;

import io.werval.api.http.QueryString;
import io.werval.runtime.exceptions.BadRequestException;
import io.werval.util.MultiValueMapMultiValued;
import io.werval.util.Strings;
import io.werval.util.TreeMultiValueMap;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.werval.runtime.util.Comparators.LOWER_CASE;
import static io.werval.util.IllegalArguments.ensureNotEmpty;
import static java.util.Collections.emptyMap;

public class QueryStringInstance
    extends MultiValueMapMultiValued<String, String>
    implements QueryString, Serializable
{
    public static final QueryString EMPTY = new QueryStringInstance();

    public QueryStringInstance()
    {
        this( emptyMap() );
    }

    public QueryStringInstance( Map<String, List<String>> values )
    {
        super( new TreeMultiValueMap<>( LOWER_CASE ) );
        if( values != null )
        {
            values.entrySet().stream().forEach(
                val -> this.mvmap.put( val.getKey(), new ArrayList<>( val.getValue() ) )
            );
        }
    }

    @Override
    public String singleValue( String name )
    {
        ensureNotEmpty( "Query String Parameter Name", name );
        if( !mvmap.containsKey( name ) )
        {
            return Strings.EMPTY;
        }
        List<String> values = mvmap.get( name );
        if( values.size() != 1 )
        {
            // TODO Do not rely on BadRequestException for single value enforcment
            // This whole method can be removed after that.
            throw new BadRequestException( "QueryString Parameter '" + name + "' has multiple values" );
        }
        return values.get( 0 );
    }

}
