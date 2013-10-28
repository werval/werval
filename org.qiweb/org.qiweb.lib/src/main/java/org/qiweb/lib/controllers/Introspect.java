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
package org.qiweb.lib.controllers;

import org.qiweb.api.outcomes.Outcome;

import static org.qiweb.api.BuildVersion.COMMIT;
import static org.qiweb.api.BuildVersion.DATE;
import static org.qiweb.api.BuildVersion.DETAILED_VERSION;
import static org.qiweb.api.BuildVersion.DIRTY;
import static org.qiweb.api.BuildVersion.VERSION;
import static org.qiweb.api.context.CurrentContext.application;
import static org.qiweb.api.context.CurrentContext.outcomes;
import static org.qiweb.api.mime.MimeTypesNames.APPLICATION_JSON;

/**
 * Controller with methods to introspect QiWeb Runtime.
 * <p>Used by the QiWeb DevShell.</p>
 * <p>When using in application code, know that theses methods disclose internal data.</p>
 */
public class Introspect
{

    /**
     * Render QiWeb Config as JSON.
     * @return QiWeb Config as JSON
     */
    public Outcome config()
    {
        return outcomes().ok( application().config().toString() ).as( APPLICATION_JSON ).build();
    }

    /**
     * Render QiWeb Version information as JSON.
     * <p>Here is a sample:</p>
     * <pre>
     * {
     *   "version": "0",
     *   "commit": "b5f8727",
     *   "dirty": true,
     *   "date": "Tue, 03 Sep 2013 16:01:24 GMT",
     * }
     * </pre>
     * @return QiWeb Version information as JSON
     */
    public Outcome version()
    {
        return outcomes().ok(
            "{\n"
            + "  \"version\": \"" + VERSION + "\",\n"
            + "  \"commit\": \"" + COMMIT + "\",\n"
            + "  \"dirty\": " + String.valueOf( DIRTY ) + ",\n"
            + "  \"date\": \"" + DATE + "\",\n"
            + "  \"detail\": \"" + DETAILED_VERSION + "\"\n"
            + "}\n" ).
            as( APPLICATION_JSON ).build();
    }

}
