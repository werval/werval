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
package org.qiweb.api.http;

/**
 * Response Header.
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

    /**
     * Apply Keep-Alive headers if needed.
     * <p>Do nothing if this {@link ResponseHeader} already has a {@link Headers.Names#CONNECTION} header.</p>
     * <p>
     *     If {@link #status()} is an error or unknown status, {@link Headers.Names#CONNECTION} is set to
     *     {@link Headers.Values#CLOSE} if this {@link #version()} use Keep-Alive by default
     *     or if {@literal keepAliveWanted} is {@literal true}.
     * </p>
     * <p>
     *     Otherwise, set {@link Headers.Names#CONNECTION} to {@link Headers.Values#KEEP_ALIVE}
     *     if this {@link #version()} use Keep-Alive by default or if {@literal keepAliveWanted} is {@literal true}.
     * </p>
     *
     * @param keepAliveWanted {@literal true} if request advertised support {@link Headers.Values#KEEP_ALIVE},
     *                        {@literal false} otherwise
     * @return This very ResponseHeader
     */
    ResponseHeader withKeepAliveHeaders( boolean keepAliveWanted );
}
