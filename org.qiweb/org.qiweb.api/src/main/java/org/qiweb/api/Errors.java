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
package org.qiweb.api;

import java.util.List;

/**
 * Application errors.
 * <p>Records errors happening in the application.</p>
 * <p>
 *     To prevent OOME when errors are frequent, it is possible to limit the amount of recorded errors with the
 *     `qiweb.errors.max` configuration property. This default to 100.
 * </p>
 */
public interface Errors
    extends Iterable<org.qiweb.api.Error>
{

    Error record( String requestId, String message, Throwable cause );

    int size();

    void clear();

    Error get( String errorId );

    List<Error> ofRequest( String requestId );
}
