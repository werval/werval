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
package org.qiweb.api.filters;

import org.qiweb.api.context.Context;
import org.qiweb.api.outcomes.Outcome;

/**
 * Chain of Filters.
 */
public interface FilterChain
{

    /**
     * Pass the Context to the next Filter in the chain.
     *
     * @param context Request Context
     * @return Next Outcome
     */
    Outcome next( Context context );

}
