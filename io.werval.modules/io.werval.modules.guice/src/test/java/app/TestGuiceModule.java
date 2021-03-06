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
package app;

import com.google.inject.AbstractModule;

/**
 * TestGuiceModule.
 */
public class TestGuiceModule
    extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind( TestSayer.class );
        bind( TestFilter.class ).asEagerSingleton();
        bind( TestController.class );
    }
}
