package org.qiweb.devshell;

import static org.qiweb.devshell.QiWebDevShell.highlight;
import static org.qiweb.devshell.QiWebDevShell.info;

public class NewDevShell
{

    private final DevShellSPI spi;

    public NewDevShell( DevShellSPI spi )
    {
        this.spi = spi;
    }

    public void start()
    {
        highlight( ">> QiWeb DevShell for " + spi.name() + " starting..." );
        try
        {
            info( "Starting Netty..." );
            DumbNetty netty = new DumbNetty();
            netty.start();

            highlight( ">> Ready for requests!" );
            Thread.sleep( Long.MAX_VALUE );
        }
        catch( InterruptedException ex )
        {
        }
    }

    public void stop()
    {
        highlight( ">> QiWeb DevShell for " + spi.name() + " stopping..." );
    }
}
