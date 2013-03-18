package org.qiweb.devshell.assembly;

import org.qi4j.bootstrap.*;
import org.qiweb.runtime.http.server.HttpServer;
import org.qiweb.runtime.http.server.HttpServerInstance;

public class DevShellAssembler
    implements ApplicationAssembler
{

    @Override
    public ApplicationAssembly assemble( ApplicationAssemblyFactory aaf )
        throws AssemblyException
    {
        ApplicationAssembly assembly = aaf.newApplicationAssembly();

        LayerAssembly console = createConsoleLayer( assembly );
        LayerAssembly http = createHttpLayer( assembly );
        LayerAssembly config = createConfigurationLayer( assembly );

        console.uses( http, config );
        http.uses( config );

        return assembly;
    }

    private LayerAssembly createConsoleLayer( ApplicationAssembly assembly )
    {
        LayerAssembly console = assembly.layer( "console" );
        return console;
    }

    private LayerAssembly createHttpLayer( ApplicationAssembly assembly )
    {
        LayerAssembly http = assembly.layer( "http" );

        ModuleAssembly server = http.module( "server" );
        server.services( HttpServer.class ).withMixins( HttpServerInstance.class ).instantiateOnStartup();

        return http;
    }

    private LayerAssembly createConfigurationLayer( ApplicationAssembly assembly )
    {
        LayerAssembly config = assembly.layer( "configuration" );
        return config;
    }
}
