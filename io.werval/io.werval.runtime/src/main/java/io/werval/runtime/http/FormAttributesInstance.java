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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.werval.api.http.FormAttributes;
import io.werval.runtime.exceptions.BadRequestException;
import io.werval.util.MultiValueMapMultiValued;
import io.werval.util.TreeMultiValueMap;

import static io.werval.runtime.util.Comparators.LOWER_CASE;

public class FormAttributesInstance
    extends MultiValueMapMultiValued<String, String>
    implements FormAttributes
{
    public FormAttributesInstance( Map<String, List<String>> values )
    {
        super( new TreeMultiValueMap<>( LOWER_CASE ), BadRequestException.BUILDER );
        if( values != null )
        {
            values.entrySet().stream().forEach(
                val -> this.mvmap.put( val.getKey(), new ArrayList<>( val.getValue() ) )
            );
        }
    }
}
