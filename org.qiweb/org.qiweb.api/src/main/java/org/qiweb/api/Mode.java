/*
 * Copyright (c) 2013-2014 the original author or authors
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
package org.qiweb.api;

import org.qiweb.util.Reflectively;

/**
 * {@link Application} Mode.
 * <p>
 * Default is {@link #TEST} Mode.
 */
@Reflectively.Loaded( by = "DevShell" )
public enum Mode
{
    /**
     * Development Mode.
     * <p>
     * Source is watched, Application is restarted on-demand, stacktraces are disclosed in responses.
     */
    @Reflectively.Get( by = "DevShell" )
    DEV( "Development" ),
    /**
     * Test Mode.
     * <p>
     * Intended to be used in unit tests, almost equivalent to {@link #PROD} Mode.
     */
    TEST( "Test" ),
    /**
     * Production Mode.
     * <p>
     * Run from binaries only, nothing gets reloaded.
     */
    PROD( "Production" );
    private final String displayName;

    private Mode( String displayName )
    {
        this.displayName = displayName;
    }

    /**
     * Display name of this Application Mode.
     *
     * @return The display name of this Application Mode
     */
    @Override
    public String toString()
    {
        return displayName;
    }
}
