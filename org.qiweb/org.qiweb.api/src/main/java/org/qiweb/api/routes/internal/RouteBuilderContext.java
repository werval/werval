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
package org.qiweb.api.routes.internal;

import java.util.LinkedHashMap;
import java.util.Map;
import org.qiweb.api.routes.ControllerParams;

/**
 * RouteBuilder Context, used internally by Routebuilder, do not use in Application code.
 */
public final class RouteBuilderContext
{
    private static final ThreadLocal<Map<String, ControllerParams.Param>> CTRL_PARAM_RECORD;

    static
    {
        CTRL_PARAM_RECORD = new ThreadLocal<Map<String, ControllerParams.Param>>()
        {
            @Override
            protected Map<String, ControllerParams.Param> initialValue()
            {
                return new LinkedHashMap<>();
            }
        };
    }

    public static final void recordMethodParameter( String name, Class<?> type )
    {
        CTRL_PARAM_RECORD.get().put( name, new ControllerParams.Param( name, type ) );
    }

    public static final <T> void recordMethodParameter( String name, Class<T> type, T forcedValue )
    {
        CTRL_PARAM_RECORD.get().put( name, new ControllerParams.Param( name, type, forcedValue ) );
    }

    public static Map<String, ControllerParams.Param> recordedControllerParams()
    {
        return CTRL_PARAM_RECORD.get();
    }

    public static void clear()
    {
        CTRL_PARAM_RECORD.remove();
    }
}
