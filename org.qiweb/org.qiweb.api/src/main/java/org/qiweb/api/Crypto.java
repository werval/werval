/*
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
package org.qiweb.api;

/**
 * Cryptography service.
 */
public interface Crypto
{
    /**
     * Generate a 256 bits long secret encoded in hexadecimal.
     *
     * @return The generated secret encoded in hexadecimal as String
     */
    String genNew256bitsHexSecret();

    /**
     * Sign given message with the {@link Application}'s secret using HMAC SHA-256.
     *
     * @param message Message to sign
     *
     * @return Message signature
     */
    String hexHmacSha256( String message );

    /**
     * Sign given message with the given secret using HMAC SHA-256.
     *
     * @param message Message to sign
     * @param secret  Secret to use
     *
     * @return Message signature
     */
    String hexHmacSha256( String message, String secret );
}
