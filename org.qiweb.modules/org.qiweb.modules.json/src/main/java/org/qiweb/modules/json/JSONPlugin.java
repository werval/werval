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
package org.qiweb.modules.json;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import io.werval.api.Application;
import io.werval.api.Plugin;
import io.werval.api.exceptions.ActivationException;

/**
 * JsonPlugin.
 */
public class JSONPlugin
    implements Plugin<JSON>
{
    private JacksonJSON json;

    @Override
    public Class<JSON> apiType()
    {
        return JSON.class;
    }

    @Override
    public JSON api()
    {
        return json;
    }

    @Override
    public void onActivate( Application application )
        throws ActivationException
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure( MapperFeature.DEFAULT_VIEW_INCLUSION, false );
        mapper.setPropertyNamingStrategy( PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES );
        json = new JacksonJSON( mapper );
    }

    @Override
    public void onPassivate( Application application )
    {
        json = null;
    }
}
