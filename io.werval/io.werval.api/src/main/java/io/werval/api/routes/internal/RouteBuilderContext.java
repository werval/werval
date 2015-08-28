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
package io.werval.api.routes.internal;

import java.util.LinkedHashMap;
import java.util.Map;

import io.werval.api.routes.ControllerParams.Param;
import io.werval.api.routes.ControllerParams.ParamValue;

/**
 * RouteBuilder Context, used internally by Routebuilder, do not use in Application code.
 */
public final class RouteBuilderContext
{
    private static final ThreadLocal<Map<String, Param>> CTRL_PARAM_RECORD;

    static
    {
        CTRL_PARAM_RECORD = new ThreadLocal<Map<String, Param>>()
        {
            @Override
            protected Map<String, Param> initialValue()
            {
                return new LinkedHashMap<>();
            }
        };
    }

    public static void recordMethodParameter( String name, Class<?> type )
    {
        CTRL_PARAM_RECORD.get().put( name, new Param( name, type ) );
    }

    public static <T> void recordMethodParameter( String name, Class<T> type, ParamValue valueKind, T value )
    {
        CTRL_PARAM_RECORD.get().put( name, new Param( name, type, valueKind, value ) );
    }

    public static Map<String, Param> recordedControllerParams()
    {
        return CTRL_PARAM_RECORD.get();
    }

    public static void clear()
    {
        CTRL_PARAM_RECORD.remove();
    }

    private RouteBuilderContext()
    {
    }
}
