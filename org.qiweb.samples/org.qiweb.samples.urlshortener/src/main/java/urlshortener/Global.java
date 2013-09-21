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
package urlshortener;

import org.qiweb.api.Application;

/**
 * Custom Global Object that instanciate the ShortenerService and set it as application metadata.
 */
public class Global
    extends org.qiweb.api.Global
{

    @Override
    public void onStart( Application application )
    {
        application.metaData().put( "shortener", new ShortenerService() );
    }

    @Override
    public void onStop( Application application )
    {
        application.metaData().remove( "shortener" );
    }
}
