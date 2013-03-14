package org.qiweb.devshell;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.idea.IdeaContentRoot;
import org.gradle.tooling.model.idea.IdeaModule;
import org.gradle.tooling.model.idea.IdeaProject;
import org.gradle.tooling.model.idea.IdeaSourceDirectory;

public class ProjectBuilder
{

    private final File projectDir;
    private final GradleConnector connector = GradleConnector.newConnector();

    public ProjectBuilder( File projectDir )
    {
        this.projectDir = projectDir;
        this.connector.forProjectDirectory( projectDir );
    }

    public ProjectModel buildProjectModel()
    {
        Set<File> mainSources = new LinkedHashSet<>();
        Set<File> testSources = new LinkedHashSet<>();
        ProjectConnection connection = connector.connect();
        try
        {
            IdeaProject ideaProject = connection.model( IdeaProject.class ).get();
            IdeaModule mainModule = tryFindMainModule( projectDir, ideaProject );
            if( mainModule == null )
            {
                throw new RuntimeException( "Unable to find project main module" );
            }
            // TODO Below code do not support child modules, this should be fixed!
            for( IdeaContentRoot ideaContentRoot : mainModule.getContentRoots() )
            {
                for( IdeaSourceDirectory ideaSrcDir : ideaContentRoot.getSourceDirectories() )
                {
                    mainSources.add( ideaSrcDir.getDirectory() );
                }
                for( IdeaSourceDirectory ideaTestDir : ideaContentRoot.getTestDirectories() )
                {
                    testSources.add( ideaTestDir.getDirectory() );
                }
            }
            File mainOutputDir = mainModule.getCompilerOutput().getOutputDir();
            File testOutputDir = mainModule.getCompilerOutput().getTestOutputDir();
            return new ProjectModel( ideaProject.getName(), projectDir, mainSources, mainOutputDir, testSources, testOutputDir );
        }
        finally
        {
            connection.close();
        }
    }

    private static IdeaModule tryFindMainModule( File projectDir, IdeaProject ideaModel )
    {
        for( IdeaModule module : ideaModel.getModules() )
        {
            File moduleDir = tryGetModuleDir( module );
            if( moduleDir != null && moduleDir.equals( projectDir ) )
            {
                return module;
            }
        }
        return null;
    }

    private static File tryGetModuleDir( IdeaModule module )
    {
        DomainObjectSet<? extends IdeaContentRoot> contentRoots = module.getContentRoots();
        return contentRoots.isEmpty() ? null : contentRoots.getAt( 0 ).getRootDirectory();
    }

    public void compileJava()
    {
        ProjectConnection connection = connector.connect();
        try
        {
            connection.newBuild().forTasks( "compileJava" ).run();
        }
        finally
        {
            connection.close();
        }
    }
}
