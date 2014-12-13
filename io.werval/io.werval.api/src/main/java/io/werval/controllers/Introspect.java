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

import io.werval.api.outcomes.Outcome;

import static io.werval.api.BuildVersion.COMMIT;
import static io.werval.api.BuildVersion.DATE;
import static io.werval.api.BuildVersion.DETAILED_VERSION;
import static io.werval.api.BuildVersion.DIRTY;
import static io.werval.api.BuildVersion.VERSION;
import static io.werval.api.context.CurrentContext.application;
import static io.werval.api.context.CurrentContext.outcomes;

/**
 * Introspect Werval Runtime.
 * <p>
 * Used by the Werval DevShell.
 * <p>
 * When using in application code, know that theses methods disclose internal data.
 */
public class Introspect
{
    /**
     * Render Werval Config as JSON.
     *
     * @return Werval Config as JSON
     */
    public Outcome config()
    {
        return outcomes().ok( application().config().toString() ).asJson().build();
    }

    /**
     * Render Werval Version information as JSON.
     * <p>
     * Here is a sample:
     * <pre>
     * {
     *   "version": "0",
     *   "commit": "b5f8727",
     *   "dirty": true,
     *   "date": "Tue, 03 Sep 2013 16:01:24 GMT",
     *   "detail": "io.werval:io.werval.api:0 b5f8727 (dirty) Tue, 03 Sep 2013 16:01:24 GMT"
     * }
     * </pre>
     *
     * @return Werval Version information as JSON
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
            + "}\n"
        ).asJson().build();
    }
}
