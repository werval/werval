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
package org.qiweb.doc;

import org.qiweb.api.controllers.Classpath;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.api.routes.ReverseRoute;

import static org.qiweb.api.context.CurrentContext.outcomes;
import static org.qiweb.api.context.CurrentContext.reverseRoutes;

/**
 * Core Documentations Controller.
 */
public class CoreDocumentations
{
    public Outcome index()
    {
        ReverseRoute redirect = reverseRoutes().get( getClass(), c -> c.catchAll( "index.html" ) );
        return outcomes().seeOther( redirect.httpUrl() ).build();
    }

    public Outcome catchAll( String path )
    {
        return new Classpath().resource( "org/qiweb/doc/html/" + path );
    }
}
