package org.qiweb.devshell;

import java.io.File;
import java.util.Set;

public interface Watcher
{

    interface ChangeListener
    {

        void onChange();
    }

    interface Watch
    {

        void unwatch();
    }

    Watch watch( Set<File> directories, ChangeListener listener );
}
