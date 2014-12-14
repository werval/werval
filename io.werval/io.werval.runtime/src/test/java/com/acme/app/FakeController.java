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
package com.acme.app;

import io.werval.api.outcomes.Outcome;

public interface FakeController
{
    String noOutcome();

    Outcome test();

    Outcome another( String id, Integer slug );

    Outcome index();

    Outcome foo();

    Outcome bar();

    Outcome wild( String path );

    Outcome forcedWild( String root, String path );

    Outcome customParam( CustomParam param );
}
