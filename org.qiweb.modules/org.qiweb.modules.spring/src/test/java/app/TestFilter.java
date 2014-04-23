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

import org.qiweb.api.context.Context;
import org.qiweb.api.filters.Filter;
import org.qiweb.api.filters.FilterChain;
import org.qiweb.api.outcomes.Outcome;
import org.springframework.stereotype.Service;

/**
 * TestFilter.
 */
@Service
public class TestFilter
    implements Filter<Void>
{
    public static int invocations = 0;

    @Override
    public Outcome filter( Void filterConfig, FilterChain chain, Context context )
    {
        invocations++;
        return chain.next( context );
    }
}
