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
package io.werval.api;

import io.werval.util.Hashids;

/**
 * Cryptography service.
 */
public interface Crypto
{
    /**
     * Application secret.
     *
     * @return Application secret bytes.
     */
    byte[] secret();

    /**
     * Generate a 256 bits long secret.
     *
     * @return The generated secret bytes
     */
    byte[] newSecret();

    /**
     * Generate a 256 bits long secret encoded in hexadecimal.
     *
     * @return The generated secret encoded in hexadecimal as String
     */
    String newSecretHex();

    /**
     * Generate a 256 bits long secret encoded in Base64.
     *
     * @return The generated secret encoded in Base64, {@literal Basic} profile, as String
     */
    String newSecretBase64();

    /**
     * Sign given message with the Application's secret using HMAC SHA-256.
     *
     * @param message Message to sign
     *
     * @return Message signature bytes
     */
    byte[] hmacSha256( byte[] message );

    /**
     * Sign given message with the given secret using HMAC SHA-256.
     *
     * @param message Message to sign
     * @param secret  Secret to use
     *
     * @return Message signature bytes
     */
    byte[] hmacSha256( byte[] message, byte[] secret );

    /**
     * Sign given message with the Application's secret using HMAC SHA-256.
     *
     * @param message Message to sign
     *
     * @return Message signature encoded in hexadecimal as String
     */
    String hmacSha256Hex( CharSequence message );

    /**
     * Sign given message with the given secret using HMAC SHA-256.
     *
     * @param message Message to sign
     * @param secret  Secret to use
     *
     * @return Message signature encoded in hexadecimal as String
     */
    String hmacSha256Hex( CharSequence message, String secret );

    /**
     * Generate a hash of the given message using SHA-256.
     *
     * @param message Message to hash
     *
     * @return Message hash
     */
    byte[] sha256( byte[] message );

    /**
     * Generate a hash of the given message using SHA-256.
     *
     * @param message Message to hash
     *
     * @return Message hash
     */
    byte[] sha256( CharSequence message );

    /**
     * Generate a hash of the given message using SHA-256.
     *
     * @param message Message to hash
     *
     * @return Message hash encoded in hexadecimal as String
     */
    String sha256Hex( CharSequence message );

    /**
     * Generate a hash of the given message using SHA-256.
     *
     * @param message Message to hash
     *
     * @return Message hash encoded in Base64, {@literal Basic} profile, as String
     */
    String sha256Base64( CharSequence message );

    /**
     * Hashids salted using the application's secret.
     *
     * @return Hashids salted using the application's secret
     */
    Hashids hashids();
}
