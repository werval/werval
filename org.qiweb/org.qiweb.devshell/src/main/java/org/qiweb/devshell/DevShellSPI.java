package org.qiweb.devshell;

import java.io.File;
import java.net.URL;
import java.util.Set;

public interface DevShellSPI
{

    String name();

    File rootDir();

    File buildDir();

    Set<File> mainSources();

    File mainOutput();

    URL[] mainClassPath();

    boolean hasMainChanged();

    void rebuildMain();

    Set<File> testSources();

    File testOutput();

    URL[] testClassPath();

    boolean hasTestChanged();

    void rebuildTest();
}
