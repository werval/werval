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

import org.qiweb.api.Application.Mode;
import org.qiweb.api.outcomes.Outcome;

import static org.qiweb.api.context.CurrentContext.application;
import static org.qiweb.api.context.CurrentContext.outcomes;
import static org.qiweb.api.mime.MimeTypesNames.TEXT_HTML;

/**
 * Welcome Controller.
 * <p>Set to default route of new Applications created by the QiWeb CLI 'new' command.</p>
 * <p>The served QiWeb Welcome Page depends on the Application's {@link Mode}.</p>
 * <p>In production and test modes, the page display a simple welcome message.</p>
 * <p>
 *     In development mode, links are provided to the DevShell embedded services like hosted documentation and
 *     debugging tools.
 * </p>
 */
public class Welcome
{
    /**
     * @return The QiWeb Welcome Page according to the Application's {@link Mode}.
     */
    public Outcome welcome()
    {
        String path = application().mode() == Mode.DEV
                      ? "org/qiweb/runtime/controllers/welcome_dev.html"
                      : "org/qiweb/runtime/controllers/welcome.html";
        return outcomes().
            ok().
            as( application().mimeTypes().withCharsetOfTextual( TEXT_HTML ) ).
            withBody( application().classLoader().getResourceAsStream( path ) ).
            build();
    }
}
