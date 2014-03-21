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
package org.qiweb.modules.spring;

import org.qiweb.api.Application;
import org.qiweb.api.Plugin;
import org.qiweb.api.exceptions.ActivationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * SpringPlugin.
 */
public class SpringPlugin
    implements Plugin<ApplicationContext>
{
    private ApplicationContext springContext;

    @Override
    public void onActivate( Application application )
        throws ActivationException
    {
        springContext = new ClassPathXmlApplicationContext( application.config().string( "spring.components" ) );
    }

    @Override
    public void onPassivate( Application application )
    {
        springContext = null;
    }

    @Override
    public Class<ApplicationContext> apiType()
    {
        return ApplicationContext.class;
    }

    @Override
    public ApplicationContext api()
    {
        return springContext;
    }
}
