package org.qiweb.gradle

import org.gradle.api.Project
import org.gradle.api.Plugin

class QiWebPlugin implements Plugin<Project> {

    private static final String PLUGIN_ID = "qiweb";
    private static final String EXT_ID = "qiweb";
    private static final String DEVSHELL_TASK = "devshell";

    void apply( Project project ) {
        
        // Add the 'qiweb' extension object
        project.extensions.create( EXT_ID, QiWebPluginExtension )
        
        // Add a task that uses the configuration
        project.task( DEVSHELL_TASK, description: 'Start the QiWeb DevShell.' ) << {
            println project.qiweb.message
        }

    }

}
