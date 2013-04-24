package org.qiweb.spi.dev;

import java.net.URL;

public interface DevShellSPI
{

    URL[] classPath();

    boolean isSourceChanged();

    void rebuild();
}
