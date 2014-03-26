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
package org.qiweb.api.routes;

/**
 * Controller Call Recorder functional interface for route building and reverse routing fluent APIs.
 *
 * @param <ControllerType> Parameterized type of the Controller
 */
@FunctionalInterface
public interface ControllerCallRecorder<ControllerType>
{
    /**
     * Record Controller call.
     *
     * @param controller Controller to call
     *
     * @throws Exception if something go wrong, allow to record calls to Controller methods that throws exceptions
     */
    void recordCall( ControllerType controller )
        throws Exception;
}
