package com.acme.app;

import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;

public class MainModuleAssembler
    implements Assembler
{

    @Override
    public void assemble( ModuleAssembly ma )
        throws AssemblyException
    {
        ma.services( FakeController.class );
    }
}
