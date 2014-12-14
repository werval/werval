/*
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
package io.werval.api.routes;

/**
 * Parameter Binders.
 *
 * @composed 1 - * ParameterBinder
 */
public interface ParameterBinders
{
    /**
     * Bind a parameter value.
     *
     * @param <T>   Parameter parameterized type
     * @param type  Parameter type
     * @param name  Parameter name
     * @param value Parameter value
     *
     * @return The bound value
     */
    <T> T bind( Class<T> type, String name, String value );

    /**
     * Unbind a parameter value.
     *
     * @param <T>   Parameter parameterized type
     * @param type  Parameter type
     * @param name  Parameter name
     * @param value Parameter value
     *
     * @return The unbound raw value
     */
    <T> String unbind( Class<T> type, String name, T value );
}
