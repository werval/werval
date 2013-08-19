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
package org.qiweb.runtime.controllers;

import org.qiweb.api.controllers.Outcome;

import static org.qiweb.api.Application.Mode.dev;
import static org.qiweb.api.controllers.Controller.application;
import static org.qiweb.api.controllers.Controller.outcomes;

/**
 * Welcome Controller.
 * <p>Set to default route of new Applications created by the QiWeb CLI 'new' command.</p>
 * <p>The served QiWeb Welcome Page depends on the {@link Application.Mode}.</p>
 * <p>In production and test modes, the page display a simple welcome message.</p>
 * <p>
 *     In development mode, links are provided to the DevShell embedded services like hosted documentation and
 *     debugging tools.
 * </p>
 */
public class Welcome
{

    /**
     * @return The QiWeb Welcome Page according to the {@link Application.Mode}.
     */
    public Outcome welcome()
    {
        String path = application().mode() == dev
                      ? "org/qiweb/runtime/controllers/welcome_dev.html"
                      : "org/qiweb/runtime/controllers/welcome.html";
        return outcomes().
            ok().
            as( "text/html; charset=utf-8" ).
            withBody( application().classLoader().getResourceAsStream( path ) ).
            build();
    }
}
