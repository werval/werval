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

import io.werval.api.http.Headers.Names;
import io.werval.api.http.ResponseHeader;
import java.io.InputStream;

/**
 * {@link InputStream} based Outcome for streamed output.
 */
public class InputStreamOutcome
    extends AbstractOutcome<InputStreamOutcome>
{
    private final InputStream bodyInputStream;
    private final long contentLength;

    /* package */ InputStreamOutcome( ResponseHeader response, InputStream bodyInputStream, long contentLength )
    {
        super( response );
        this.bodyInputStream = bodyInputStream;
        this.contentLength = contentLength;
        this.response.headers().with( Names.CONTENT_LENGTH, String.valueOf( contentLength ) );
    }

    public final InputStream bodyInputStream()
    {
        return bodyInputStream;
    }

    public final long contentLength()
    {
        return contentLength;
    }
}
