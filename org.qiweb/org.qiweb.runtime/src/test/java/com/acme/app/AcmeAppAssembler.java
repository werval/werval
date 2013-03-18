package com.acme.app;

import org.qi4j.api.structure.Application.Mode;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;

public class AcmeAppAssembler
    implements ApplicationAssembler
{

    @Override
    public ApplicationAssembly assemble( ApplicationAssemblyFactory aaf )
        throws AssemblyException
    {
        ApplicationAssembly assembly = aaf.newApplicationAssembly();
        assembly.setName( "Acme Application" );
        assembly.setVersion( "1.0" );
        assembly.setMode( Mode.test );

        LayerAssembly mainLayer = assembly.layer( "MAIN_LAYER" );
        ModuleAssembly mainModule = mainLayer.module( "MAIN_MODULE" );

        new MainModuleAssembler().assemble( mainModule );

        return assembly;
    }
}
