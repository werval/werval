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
package org.qiweb.runtime;

import org.qiweb.api.Application;
import org.qiweb.api.Error;
import org.qiweb.api.Global;

public class TestGlobal
    extends Global
{

    public static TestGlobal ofApplication( Application application )
    {
        return (TestGlobal) ( (ApplicationInstance) application ).global();
    }
    public int httpRequestErrorCount = 0;
    public Error lastError = null;

    @Override
    public synchronized void onHttpRequestError( Application application, Error error )
    {
        httpRequestErrorCount++;
        lastError = error;
    }
}
