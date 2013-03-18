package org.qiweb.spi.dev;

import java.io.File;
import java.net.URL;
import java.util.Set;

public class DevShellSPIWrapper
    implements DevShellSPI
{

    protected final DevShellSPI wrapped;

    public DevShellSPIWrapper( DevShellSPI wrapped )
    {
        this.wrapped = wrapped;
    }

    @Override
    public String name()
    {
        return wrapped.name();
    }

    @Override
    public File rootDir()
    {
        return wrapped.rootDir();
    }

    @Override
    public File buildDir()
    {
        return wrapped.buildDir();
    }

    @Override
    public Set<File> mainSources()
    {
        return wrapped.mainSources();
    }

    @Override
    public File mainOutput()
    {
        return wrapped.mainOutput();
    }

    @Override
    public URL[] mainClassPath()
    {
        return wrapped.mainClassPath();
    }

    @Override
    public boolean hasMainChanged()
    {
        return wrapped.hasMainChanged();
    }

    @Override
    public void rebuildMain()
    {
        wrapped.rebuildMain();
    }

    @Override
    public Set<File> testSources()
    {
        return wrapped.testSources();
    }

    @Override
    public File testOutput()
    {
        return wrapped.testOutput();
    }

    @Override
    public URL[] testClassPath()
    {
        return wrapped.testClassPath();
    }

    @Override
    public boolean hasTestChanged()
    {
        return wrapped.hasTestChanged();
    }

    @Override
    public void rebuildTest()
    {
        wrapped.rebuildTest();
    }
}
