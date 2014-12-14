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
package io.werval.util;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Set of annotations to document reflective usage.
 * <p>
 * Retention is {@link java.lang.annotation.RetentionPolicy#SOURCE} only and usage isn't advertised in javadocs.
 * <p>
 * Make it easy to look for element use by name when working on the source.
 */
public interface Reflectively
{
    /**
     * This type is loaded reflectively.
     *
     * @hidden
     */
    @Retention( SOURCE )
    @Target( TYPE )
    public @interface Loaded
    {
        /**
         * @return Name(s) of component(s) that load this type reflectively
         */
        String[] by();
    }

    /**
     * This constructor or method is invoked reflectively.
     *
     * @hidden
     */
    @Retention( SOURCE )
    @Target( { CONSTRUCTOR, METHOD } )
    public @interface Invoked
    {
        /**
         * @return Name(s) of component(s) that invoke this element reflectively
         */
        String[] by();
    }

    /**
     * This field's value is get reflectively.
     *
     * @hidden
     */
    @Retention( SOURCE )
    @Target( FIELD )
    public @interface Get
    {
        /**
         * @return Name(s) of component(s) that get this field reflectively
         */
        String[] by();
    }

    /**
     * This field's value is set reflectively.
     *
     * @hidden
     */
    @Retention( SOURCE )
    @Target( FIELD )
    public @interface Set
    {
        /**
         * @return Name(s) of component(s) that set this field reflectively
         */
        String[] by();
    }
}
