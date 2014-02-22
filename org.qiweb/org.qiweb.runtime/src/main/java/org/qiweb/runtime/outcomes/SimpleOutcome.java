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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.qiweb.api.http.MutableHeaders;

/**
 * Simple {@link ByteBuf} based Outcome.
 */
public class SimpleOutcome
    extends AbstractOutcome<SimpleOutcome>
{
    private ByteBuf body = Unpooled.EMPTY_BUFFER;

    /* package */ SimpleOutcome( int status, MutableHeaders headers )
    {
        super( status, headers );
    }

    /* package */ final SimpleOutcome withEntity( ByteBuf body )
    {
        this.body = body;
        return this;
    }

    public ByteBuf body()
    {
        return body;
    }
}
