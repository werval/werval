/*
 * Copyright (c) 2015 the original author or authors
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
package io.werval.modules.jose;

import io.werval.api.outcomes.Outcome;

import static io.werval.api.context.CurrentContext.application;
import static io.werval.api.context.CurrentContext.outcomes;
import static io.werval.api.context.CurrentContext.plugin;
import static io.werval.api.context.CurrentContext.request;

/**
 * Renewal Controller.
 */
public class Renewal
{
    public Outcome renew()
    {
        String jwtHeader = application().config().string( JWT.HTTP_HEADER_CONFIG_KEY );
        if( !request().headers().has( jwtHeader ) )
        {
            return outcomes().unauthorized().build();
        }
        String renewed = plugin( JWT.class ).renewToken( request().headers().singleValue( jwtHeader ) );
        return outcomes().ok().withHeader( jwtHeader, renewed ).build();
    }
}
