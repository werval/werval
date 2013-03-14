package org.qiweb.devshell;

import java.io.File;
import java.util.Collections;
import java.util.Set;

public class ProjectModel
{

    private final String name;
    private final File projectDir;
    private final File projectOutputDir;
    private final Set<File> mainSources;
    private final File mainOutputDir;
    private final Set<File> testSources;
    private final File testOutputDir;

    public ProjectModel( String name, File projectDir, Set<File> mainSources, File mainOutputDir, Set<File> testSources, File testOutputDir )
    {
        this.name = name;
        this.projectDir = projectDir;
        this.projectOutputDir = new File( projectDir, "build" );
        this.mainSources = mainSources;
        this.mainOutputDir = mainOutputDir;
        this.testSources = testSources;
        this.testOutputDir = testOutputDir;
    }

    public String name()
    {
        return name;
    }

    public File projectDir()
    {
        return projectDir;
    }

    public File projectOutputDir()
    {
        return projectOutputDir;
    }

    public Set<File> mainSources()
    {
        return Collections.unmodifiableSet( mainSources );
    }

    public File mainOutputDir()
    {
        return mainOutputDir;
    }

    public Set<File> testSources()
    {
        return Collections.unmodifiableSet( testSources );
    }

    public File testOutputDir()
    {
        return testOutputDir;
    }
}
