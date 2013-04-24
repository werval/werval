package org.qiweb.spi.dev;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Set;
import org.qiweb.spi.dev.Watcher.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter for DevShellSPI that listen to changes but has NOOP rebuild methods.
 * <p>Extend and override to your will.</p>
 * <p>Note that this is not the DevShellSPI responsibility to trigger rebuilds.</p>
 */
public class DevShellSPIAdapter
    implements DevShellSPI
{

    private static final Logger LOG = LoggerFactory.getLogger( DevShellSPIAdapter.class );
    private final URL[] classPath;
    private boolean sourceChanged = true;

    public DevShellSPIAdapter( URL[] classPath, Set<File> sources, Watcher watcher )
    {
        this.classPath = Arrays.copyOf( classPath, classPath.length );
        // TODO Unwatch sources on DevShell passivation
        watcher.watch( sources, new ChangeListener()
        {
            @Override
            public void onChange()
            {
                LOG.info( "Source changed!" );
                sourceChanged = true;
            }
        } );
    }

    @Override
    @SuppressWarnings( "ReturnOfCollectionOrArrayField" )
    public final URL[] classPath()
    {
        return classPath;
    }

    @Override
    public final boolean isSourceChanged()
    {
        return sourceChanged;
    }

    @Override
    public final synchronized void rebuild()
    {
        if( sourceChanged )
        {
            doRebuild();
            sourceChanged = false;
        }
    }

    /**
     * No operation.
     */
    protected void doRebuild()
    {
        // NOOP
    }
}
