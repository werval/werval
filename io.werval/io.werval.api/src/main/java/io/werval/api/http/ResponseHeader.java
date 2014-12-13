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
package io.werval.api.http;

/**
 * Response Header.
 *
 * @navcomposed 1 - 1 ProtocolVersion
 * @navcomposed 1 - 1 Status
 * @navcomposed 1 - 1 MutableHeaders
 * @navcomposed 1 - 1 MutableCookies
 */
public interface ResponseHeader
{
    /**
     * @return Response HTTP protocol version
     */
    ProtocolVersion version();

    /**
     * @return Response HTTP status
     */
    Status status();

    /**
     * @return Response mutable headers
     */
    MutableHeaders headers();

    /**
     * @return Response mutable cookies
     */
    MutableCookies cookies();

    /**
     * @return {@literal true} if and only if the {@link Headers.Names#CONNECTION} header
     *         has a single value equal to {@link Headers.Values#KEEP_ALIVE}
     */
    boolean isKeepAlive();
}
