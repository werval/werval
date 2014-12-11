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
package org.qiweb.spi.server;

import io.werval.util.IdentityGenerator;
import io.werval.util.UUIDIdentityGenerator;

/**
 * Helper object for HttpServer implementations.
 *
 * @composed 1 - 1 IdentityGenerator
 */
public class HttpServerHelper
{
    private final IdentityGenerator idGen = new UUIDIdentityGenerator();

    /**
     * Generates a new request identity.
     * <p>
     * See {@link UUIDIdentityGenerator}.
     *
     * @return a new request identity.
     */
    public String generateNewRequestIdentity()
    {
        return idGen.newIdentity();
    }
}
