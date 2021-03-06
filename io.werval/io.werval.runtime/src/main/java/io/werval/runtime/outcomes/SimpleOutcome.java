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

import io.werval.api.http.ResponseHeader;
import io.werval.util.ByteSource;

import static io.werval.util.ByteSource.EMPTY_BYTES;

/**
 * Simple {@link ByteSource} based Outcome.
 */
public class SimpleOutcome
    extends AbstractOutcome<SimpleOutcome>
{
    private ByteSource body = EMPTY_BYTES;

    /* serialization */ SimpleOutcome()
    {
        super( null );
    }

    /* package */ SimpleOutcome( ResponseHeader response )
    {
        super( response );
    }

    /* package */ final SimpleOutcome withEntity( ByteSource body )
    {
        this.body = body;
        return this;
    }

    public ByteSource body()
    {
        return body;
    }
}
