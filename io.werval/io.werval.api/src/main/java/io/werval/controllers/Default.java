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
package io.werval.controllers;

import java.util.List;
import java.util.Map;

import io.werval.api.outcomes.Outcome;

import static io.werval.api.context.CurrentContext.outcomes;

/**
 * Default outcomes.
 * <p>
 * Intended to be used in routes directly.
 */
public class Default
{
    /**
     * @return a 200 OK Outcome, no-cache, with "pong" as text/plain body
     */
    public Outcome ping()
    {
        return outcomes().ok( "pong" ).asTextPlain().build();
    }

    /**
     * @return a 200 OK Outcome
     */
    public Outcome ok()
    {
        return outcomes().ok().build();
    }

    /**
     * @param text Response body text
     *
     * @return a 200 OK text/plain Outcome
     */
    public Outcome ok( String text )
    {
        return outcomes().ok( text ).asTextPlain().build();
    }

    /**
     * @return a 204 NO CONTENT Outcome
     */
    public Outcome noContent()
    {
        return outcomes().noContent().build();
    }

    /**
     * @return a 404 NOT FOUND Outcome
     */
    public Outcome notFound()
    {
        return outcomes().notFound().withBody( "404 Not Found" ).build();
    }

    /**
     * @param url Target URL
     *
     * @return a 302 FOUND Outcome
     */
    public Outcome found( String url )
    {
        return outcomes().found( url ).build();
    }

    /**
     * @param url         Target URL
     * @param queryString Query String elements
     *
     * @return a 302 FOUND Outcome
     */
    public Outcome found( String url, Map<String, List<String>> queryString )
    {
        return outcomes().found( url, queryString ).build();
    }

    /**
     * @param url Target URL
     *
     * @return a 303 SEE_OTHER Outcome
     */
    public Outcome seeOther( String url )
    {
        return outcomes().seeOther( url ).build();
    }

    /**
     * @param url         Target URL
     * @param queryString Query String elements
     *
     * @return a 303 SEE_OTHER Outcome
     */
    public Outcome seeOther( String url, Map<String, List<String>> queryString )
    {
        return outcomes().seeOther( url, queryString ).build();
    }

    /**
     * @return a 500 INTERNAL_SERVER_ERROR Outcome
     */
    public Outcome internalServerError()
    {
        return outcomes().internalServerError().withBody( "500 Internal Server Error" ).build();
    }

    /**
     * @return a 501 NOT_IMPLEMENTED Outcome
     */
    public Outcome notImplemented()
    {
        return outcomes().notImplemented().withBody( "501 Not Implemented" ).build();
    }
}
