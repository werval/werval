package org.qiweb.api;

import java.io.File;
import org.qiweb.api.mime.MimeTypes;
import org.qiweb.api.routes.PathBinders;
import org.qiweb.api.routes.Routes;

/**
 * Application.
 */
public interface Application
{

    /**
     * @return Application Config
     */
    Config config();

    /**
     * @return Application temporary directory
     */
    File tmpdir();

    /**
     * @return Application ClassLoader
     */
    ClassLoader classLoader();

    /**
     * @return Application Routes
     */
    Routes routes();

    /**
     * @return Application PathBinders
     */
    PathBinders pathBinders();

    /**
     * @return Application MimeTypes
     */
    MimeTypes mimeTypes();
}
