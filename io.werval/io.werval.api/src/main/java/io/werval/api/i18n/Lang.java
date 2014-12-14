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
package io.werval.api.i18n;

import java.util.Locale;

/**
 * Lang.
 */
public interface Lang
{
    /**
     * Full language and country code such as {@literal fr} or {@literal en-gb}.
     *
     * @return Full language and country code
     */
    String code();

    /**
     * Language code.
     *
     * @return Language code
     */
    String language();

    /**
     * Country code.
     *
     * @return Country code
     */
    String country();

    /**
     * This Lang as a Locale.
     *
     * @return This Lang as a Locale
     */
    Locale toLocale();

    /**
     * Is this Lang satisfied by another Lang.
     *
     * If the other lang defines a country code, then this is equivalent to equals, if it doesn't, then the equals is
     * only done on language and the country of this lang is ignored.
     * <p>
     * This implements the language matching specified by RFC.2616 Section 14.4.
     * Equality is case insensitive as per Section 3.10.
     *
     * @param other Another Lang
     *
     * @return {@literal true} if is this Lang is satisfied by the other Lang
     */
    boolean satisfiedBy( Lang other );
}
