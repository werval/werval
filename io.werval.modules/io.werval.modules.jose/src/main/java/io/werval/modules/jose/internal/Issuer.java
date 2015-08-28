/*
 * Copyright (c) 2015 the original author or authors
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
package io.werval.modules.jose.internal;

import java.security.Key;
import java.util.Optional;

/**
 * Issuer.
 * <p>
 * This type is internal to the Werval Jose module, you shouldn't be using it.
 */
public final class Issuer
{
    private final String dn;
    private final Key key;
    private final String keyId;
    private final String algorithm;
    private final Optional<Long> notBeforeSeconds;
    private final Optional<Long> expirationSeconds;

    public Issuer(
        String dn, Key key, String keyId, String algorithm,
        Optional<Long> notBeforeSeconds, Optional<Long> expirationSeconds
    )
    {
        this.dn = dn;
        this.key = key;
        this.keyId = keyId;
        this.algorithm = algorithm;
        this.notBeforeSeconds = notBeforeSeconds;
        this.expirationSeconds = expirationSeconds;
    }

    public String dn()
    {
        return dn;
    }

    public Key key()
    {
        return key;
    }

    public String keyId()
    {
        return keyId;
    }

    public String algorithm()
    {
        return algorithm;
    }

    public Optional<Long> notBeforeSeconds()
    {
        return notBeforeSeconds;
    }

    public Optional<Long> expirationSeconds()
    {
        return expirationSeconds;
    }
}
