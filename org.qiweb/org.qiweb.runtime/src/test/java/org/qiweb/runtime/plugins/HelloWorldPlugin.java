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
package org.qiweb.runtime.plugins;

import org.qiweb.api.Application;
import org.qiweb.api.Plugin;

/**
 * Plugin that register {@link HelloWorld} as a Plugin.
 */
public class HelloWorldPlugin
    implements Plugin<HelloWorld>
{

    private int activations = 0;
    private int passivations = 0;
    private final HelloWorld api = new HelloWorld()
    {
        @Override
        public int activations()
        {
            return activations;
        }

        @Override
        public int passivations()
        {
            return passivations;
        }

        @Override
        public String sayHello( String name )
        {
            return "Hello " + name + "!";
        }
    };

    @Override
    public Class<HelloWorld> apiType()
    {
        return HelloWorld.class;
    }

    @Override
    public HelloWorld api()
    {
        return api;
    }

    @Override
    public boolean enabled()
    {
        return true;
    }

    @Override
    public void onActivate( Application application )
    {
        activations++;
    }

    @Override
    public void onPassivate( Application application )
    {
        passivations++;
    }
}
