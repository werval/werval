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
package io.werval.modules.spring;

import io.werval.api.Application;
import io.werval.api.Global;

import org.springframework.context.ApplicationContext;

/**
 * SpringGlobal.
 */
public class SpringGlobal
    extends Global
{
    @Override
    public <T> T getFilterInstance( Application application, Class<T> filterType )
    {
        return application.plugin( ApplicationContext.class ).getBean( filterType );
    }

    @Override
    public <T> T getControllerInstance( Application application, Class<T> controllerType )
    {
        return application.plugin( ApplicationContext.class ).getBean( controllerType );
    }
}
