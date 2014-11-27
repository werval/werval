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
package org.qiweb.api.i18n;

import java.util.List;

/**
 * Langs.
 *
 * @navcomposed 1 default 1 Lang
 * @navcomposed 1 availables * Lang
 */
public interface Langs
{
    /**
     * Default Lang.
     *
     * @return Default Lang
     */
    Lang defaultLang();

    /**
     * Available Langs.
     *
     * @return Langs available in Application configuration
     */
    List<Lang> availables();

    /**
     * Guess the preferred Lang from the given langs.
     *
     * @param langs Candidate langs
     *
     * @return The first Lang that satisfies an available Lang, or the first available Lang, or the default Lang
     */
    Lang preferred( List<Lang> langs );

    /**
     * Create a Lang from code such as {@literal fr} or {@literal en-gb}.
     *
     * @param code Code
     *
     * @return Lang
     *
     * @throws IllegalArgumentException if the code is invalid
     */
    Lang fromCode( String code )
        throws IllegalArgumentException;
}
