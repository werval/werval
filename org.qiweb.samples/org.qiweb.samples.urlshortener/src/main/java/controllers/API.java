/**
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
package controllers;

import org.qiweb.api.controllers.Outcome;

import static org.qiweb.api.controllers.Controller.*;

public class API
{

    public Outcome list()
    {
        return outcomes().notImplemented().build();
    }

    public Outcome shorten( String longUrl )
    {
        return outcomes().notImplemented().build();
    }

    public Outcome expand( String shortUrlOrHash )
    {
        return outcomes().notImplemented().build();
    }

    public Outcome lookup( String longUrl )
    {
        return outcomes().notImplemented().build();
    }

    public Outcome redirect( String hash )
    {
        return outcomes().notImplemented().build();
    }
}
