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
package io.werval.runtime.outcomes;

import java.io.Serializable;

import io.werval.api.http.ResponseHeader;
import io.werval.api.outcomes.Outcome;

/**
 * Base Outcome class.
 *
 * @param <T> Parameterized self type for easy typesafe builder-style methods in descendents
 */
/* package */ abstract class AbstractOutcome<T extends AbstractOutcome<?>>
    implements Outcome, Serializable
{
    protected final ResponseHeader response;

    /* package */ AbstractOutcome( ResponseHeader response )
    {
        this.response = response;
    }

    @Override
    public ResponseHeader responseHeader()
    {
        return response;
    }

    @Override
    public String toString()
    {
        return response.toString();
    }
}
