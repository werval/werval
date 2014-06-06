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
package org.qiweb.api.filters;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.qiweb.api.context.Context;
import org.qiweb.api.outcomes.Outcome;

/**
 * Controller invocation Filter.
 *
 * @param <T> Configuration Type
 */
public interface Filter<T>
{
    /**
     * Filter a request.
     *
     * @param chain        Filter Chain
     * @param context      Request Context
     * @param filterConfig Optional filter configuration Annotation
     *
     * @return Filtered Future Outcome
     */
    CompletableFuture<Outcome> filter( FilterChain chain, Context context, Optional<T> filterConfig );
}
