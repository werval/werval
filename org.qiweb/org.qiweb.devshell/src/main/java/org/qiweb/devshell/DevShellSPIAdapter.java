package org.qiweb.devshell;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import org.qiweb.devshell.Watcher.ChangeListener;

/**
 * Adapter for DevShellSPI that listen to changes but has NOOP rebuild methods.
 * <p>Extend and override to your will.</p>
 * <p>Note that this is not the DevShellSPI responsibility to trigger rebuilds.</p>
 */
public class DevShellSPIAdapter
    implements DevShellSPI
{

    private final String name;
    private final File rootDir;
    private final File buildDir;
    private final Set<File> mainSources;
    private final File mainOutput;
    private final URL[] mainClassPath;
    private final Set<File> testSources;
    private final File testOutput;
    private final URL[] testClassPath;
    private final Watcher watcher;
    private boolean mainChanged = false;
    private boolean testChanged = false;

    public DevShellSPIAdapter( String name, File rootDir, File buildDir,
                               Set<File> mainSources, File mainOutput, URL[] mainClassPath,
                               Set<File> testSources, File testOutput, URL[] testClassPath,
                               Watcher watcher )
    {
        this.name = name;
        this.rootDir = rootDir;
        this.buildDir = buildDir;
        this.mainSources = mainSources;
        this.mainOutput = mainOutput;
        this.mainClassPath = mainClassPath;
        this.testSources = testSources;
        this.testOutput = testOutput;
        this.testClassPath = testClassPath;
        this.watcher = watcher;
        activateWatcher();
    }

    private void activateWatcher()
    {
        watcher.watch( mainSources, new ChangeListener()
        {
            @Override
            public void onChange()
            {
                QiWebDevShell.success( "Main source changed!" );
                mainChanged = true;
            }
        } );
        watcher.watch( testSources, new ChangeListener()
        {
            @Override
            public void onChange()
            {
                QiWebDevShell.warn( "Test source changed!" );
                testChanged = true;
            }
        } );
    }

    @Override
    public final String name()
    {
        return name;
    }

    @Override
    public final File rootDir()
    {
        return rootDir;
    }

    @Override
    public final File buildDir()
    {
        return buildDir;
    }

    @Override
    public final Set<File> mainSources()
    {
        return Collections.unmodifiableSet( mainSources );
    }

    @Override
    public final File mainOutput()
    {
        return mainOutput;
    }

    @Override
    public final URL[] mainClassPath()
    {
        return mainClassPath;
    }

    @Override
    public final boolean hasMainChanged()
    {
        return mainChanged;
    }

    /**
     * No operation.
     */
    @Override
    public void rebuildMain()
    {
        // NOOP
    }

    @Override
    public final Set<File> testSources()
    {
        return Collections.unmodifiableSet( testSources );
    }

    @Override
    public final File testOutput()
    {
        return testOutput;
    }

    @Override
    public final URL[] testClassPath()
    {
        return testClassPath;
    }

    @Override
    public final boolean hasTestChanged()
    {
        return testChanged;
    }

    /**
     * No operation.
     */
    @Override
    public void rebuildTest()
    {
        // NOOP
    }
}
