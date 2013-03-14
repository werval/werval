package org.qiweb.devshell;

import org.qi4j.api.activation.Activation;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.activation.PassivationException;
import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qiweb.devshell.assembly.DevShellAssembler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * QiWeb DevShell - Qi4j Rapid Web Application Development Environment.
 */
public class DevShell
    implements Activation
{

    private static final Logger LOG = LoggerFactory.getLogger( DevShell.class );
    private Application app;

    public DevShell()
        throws AssemblyException
    {
        app = new Energy4Java().newApplication( new DevShellAssembler() );
    }

    @Override
    public synchronized void activate()
        throws ActivationException
    {
        LOG.info( "DevShell Activation" );
        app.activate();
        LOG.info( "DevShell Activated" );
    }

    @Override
    public void passivate()
        throws PassivationException
    {
        LOG.info( "DevShell Passivation" );
        app.passivate();
        LOG.info( "DevShell Passivated" );
    }

    public static void main( String[] args )
        throws Exception
    {
        final DevShell devShell = new DevShell();
        devShell.activate();
        Runtime.getRuntime().addShutdownHook( new Thread( new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    devShell.passivate();
                }
                catch( Exception ex )
                {
                    LOG.error( "Unable to passivate DevShell: " + ex.getMessage(), ex );
                }
            }
        }, "DevShell Passivation" ) );
        Thread.sleep( 1 * 60 * 1000 );
        devShell.passivate();
    }
}
