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
 * (De)Serialize parameters.
 *
 * @param <T> Parameter type
 */
public interface ParameterBinder<T>
{
    /**
     * Check if this ParameterBinder accept the given type.
     *
     * @param type Parameter type
     *
     * @return TRUE if this ParameterBinder accept the given type, otherwise return FALSE
     */
    boolean accept( Class<?> type );

    /**
     * Bind a parameter value.
     *
     * @param name  Parameter name
     * @param value Parameter raw value
     *
     * @return The bound value
     */
    T bind( String name, String value );

    /**
     * Unbind a parameter value.
     *
     * @param name  Parameter name
     * @param value Parameter value
     *
     * @return The unbound raw value
     */
    String unbind( String name, T value );
}
