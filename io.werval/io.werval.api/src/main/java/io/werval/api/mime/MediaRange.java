/*
 * Copyright (c) 2014 the original author or authors
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
package io.werval.api.mime;

import java.util.List;

import io.werval.util.Couple;

/**
 * Media Range as defined in RFC 2616, sections 3.7 and 14.1.
 */
public interface MediaRange
{
    /**
     * @return Media type
     */
    String type();

    /**
     * @return Media subtype
     */
    String subtype();

    /**
     * @return {@literal type}/{@literal subtype}
     */
    String mimetype();

    /**
     * @return Media q-value
     */
    double qValue();

    /**
     * @return Accept extensions
     */
    List<Couple<String, String>> acceptExtensions();

    /**
     * Test if this MediaRange accepts the given mime type.
     *
     * @param mimeType Mime type
     *
     * @return {@literal true} if {@literal mimeType} matches the Accept header, otherwise return {@literal false}
     */
    boolean accepts( String mimeType );
}
