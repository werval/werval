/**
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
package org.qiweb.runtime.outcomes;

import java.io.InputStream;
import org.qiweb.api.http.ResponseHeader;

/**
 * {@link InputStream} based Outcome for chunked output.
 */
public class ChunkedInputOutcome
    extends AbstractOutcome<ChunkedInputOutcome>
{
    private final InputStream input;
    private final int chunkSize;

    /* package */
    ChunkedInputOutcome( ResponseHeader response, InputStream input, int chunkSize )
    {
        super( response );
        this.input = input;
        this.chunkSize = chunkSize;
    }

    public InputStream inputStream()
    {
        return input;
    }

    public int chunkSize()
    {
        return chunkSize;
    }
}
