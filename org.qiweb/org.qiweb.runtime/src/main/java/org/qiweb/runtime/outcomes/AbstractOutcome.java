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
package org.qiweb.runtime.outcomes;

import org.qiweb.api.http.Headers;
import org.qiweb.api.http.MutableHeaders;
import org.qiweb.api.http.StatusClass;
import org.qiweb.api.outcomes.Outcome;

/**
 * Base Outcome class.
 * @param <T> Parameterized self type for easy typesafe builder-style methods in descendents
 */
/* package */ abstract class AbstractOutcome<T extends AbstractOutcome<?>>
    implements Outcome
{
    protected final MutableHeaders headers;
    private final int status;

    /* package */ AbstractOutcome( int status, MutableHeaders headers )
    {
        this.headers = headers;
        this.status = status;
    }

    @Override
    public final int status()
    {
        return status;
    }

    @Override
    public StatusClass statusClass()
    {
        return StatusClass.valueOf( status );
    }

    @Override
    public final Headers headers()
    {
        return headers;
    }

    @Override
    public String toString()
    {
        return status + ", " + headers;
    }
}
