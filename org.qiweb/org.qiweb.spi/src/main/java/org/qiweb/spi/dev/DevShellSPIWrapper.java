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
package org.qiweb.spi.dev;

import java.net.URL;

/**
 * Development Shell SPI Wrapper.
 */
public class DevShellSPIWrapper
    implements DevShellSPI
{
    private final DevShellSPI wrapped;

    public DevShellSPIWrapper( DevShellSPI wrapped )
    {
        this.wrapped = wrapped;
    }

    public DevShellSPI wrapped()
    {
        return wrapped;
    }

    @Override
    public URL[] applicationClassPath()
    {
        return wrapped.applicationClassPath();
    }

    @Override
    public URL[] runtimeClassPath()
    {
        return wrapped.runtimeClassPath();
    }

    @Override
    public String sourceURL( String packageName, String fileName, int lineNumber )
    {
        return wrapped.sourceURL( packageName, fileName, lineNumber );
    }

    @Override
    public boolean isSourceChanged()
    {
        return wrapped.isSourceChanged();
    }

    @Override
    public void rebuild()
        throws DevShellRebuildException
    {
        wrapped.rebuild();
    }

    @Override
    public void stop()
    {
        wrapped.stop();
    }
}
