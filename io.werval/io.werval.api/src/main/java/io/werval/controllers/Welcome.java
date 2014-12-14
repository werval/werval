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

import io.werval.api.Mode;
import io.werval.api.outcomes.Outcome;

import static io.werval.api.context.CurrentContext.application;
import static io.werval.api.context.CurrentContext.outcomes;

/**
 * Welcome Controller.
 * <p>
 * Set to default route of new Applications created by the werval cli 'new' command.
 * <p>
 * The served werval welcome page depends on the Application's {@link Mode}.
 * <p>
 * In production and test modes, the page display a simple welcome message.
 * <p>
 * In development mode, links are provided to the DevShell embedded services like hosted documentation and
 * debugging tools.
 */
public class Welcome
{
    /**
     * @return The werval welcome page according to the Application's {@link Mode}.
     */
    public Outcome welcome()
    {
        String path = application().mode() == Mode.DEV
                      ? "io/werval/controllers/welcome_dev.html"
                      : "io/werval/controllers/welcome.html";
        return outcomes()
            .ok()
            .asHtml()
            .withBody( application().classLoader().getResourceAsStream( path ) )
            .build();
    }
}
