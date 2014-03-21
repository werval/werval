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
package beerdb;

import java.util.HashMap;
import java.util.Map;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.lib.controllers.ClasspathResources;
import org.rythmengine.RythmEngine;

import static org.qiweb.api.context.CurrentContext.application;
import static org.qiweb.api.context.CurrentContext.outcomes;
import static org.qiweb.api.context.CurrentContext.reverseRoutes;
import static org.qiweb.api.mime.MimeTypesNames.TEXT_HTML;

public class UI
{
    public Outcome app()
    {
        Map<String, Object> params = new HashMap<>();
        params.put( "css", reverseRoutes().get( ClasspathResources.class, c -> c.resource( "assets/", "css/main.css" ) ).httpUrl() );
        params.put( "js", reverseRoutes().get( ClasspathResources.class, c -> c.resource( "assets/", "js/main.js" ) ).httpUrl() );
        String body = application().plugin( RythmEngine.class ).render( "index.html", params );
        return outcomes().ok().as( TEXT_HTML ).withBody( body ).build();
    }
}
