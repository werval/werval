package org.qiweb.spi.dev;

import java.net.URL;

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
    public URL[] classPath()
    {
        return wrapped.classPath();
    }

    @Override
    public boolean isSourceChanged()
    {
        return wrapped.isSourceChanged();
    }

    @Override
    public void rebuild()
    {
        wrapped.rebuild();
    }
}
