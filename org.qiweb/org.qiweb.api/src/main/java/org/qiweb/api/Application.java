package org.qiweb.api;

import java.io.File;
import org.qiweb.api.mime.MimeTypes;
import org.qiweb.api.routes.PathBinders;
import org.qiweb.api.routes.Routes;

/**
 * Application.
 * <p>
 *     An Application instance can run in different {@link Mode}s, default Mode is {@link Mode#test}. For your code to
 *     be {@link Mode#dev} friendly, don't hold references to instances returned by an Application instance.
 * </p>
 */
public interface Application
{

    /**
     * {@link Application} Mode.
     * <p>Default is {@link #test} Mode.</p>
     */
    enum Mode
    {

        /**
         * Development Mode.
         * <p>Source is watched, Application is reloaded on-demand.</p>
         */
        dev( "Development" ),
        /**
         * Test Mode.
         * <p>Intended to be used in unit tests, almost equivalent to {@link #prod} Mode.</p>
         */
        test( "Test" ),
        /**
         * Production Mode.
         * <p>Run from binaries only, nothing gets reloaded.</p>
         */
        prod( "Production" );
        private final String displayName;

        private Mode( String displayName )
        {
            this.displayName = displayName;
        }

        @Override
        public String toString()
        {
            return displayName;
        }
    }

    /**
     * @return Application {@link Mode}, defaults to {@link Mode#test}.
     */
    Mode mode();

    /**
     * Application {@link Config}.
     * <p>Don't hold references to the Config instance in order to make your code {@link Mode#dev} friendly.</p>
     * @return Application {@link Config}
     */
    Config config();

    /**
     * Application temporary directory {@link File}.
     * <p>Don't hold references to the File instance in order to make your code {@link Mode#dev} friendly.</p>
     * @return Application temporary directory {@link File}
     */
    File tmpdir();

    /**
     * Application {@link ClassLoader}.
     * <p>Don't hold references to the ClassLoader instance to prevent memory leaks in {@link Mode#dev}.</p>
     * @return Application {@link ClassLoader}
     */
    ClassLoader classLoader();

    /**
     * Application {@link Routes}.
     * <p>Don't hold references to the Routes instance in order to make your code {@link Mode#dev} friendly.</p>
     * @return Application {@link Routes}
     */
    Routes routes();

    /**
     * Application {@link PathBinders}.
     * <p>Don't hold references to the PathBinders instance in order to make your code {@link Mode#dev} friendly.</p>
     * @return Application {@link PathBinders}
     */
    PathBinders pathBinders();

    /**
     * Application {@link MimeTypes}.
     * <p>Don't hold references to the MimeTypes instance in order to make your code {@link Mode#dev} friendly.</p>
     * @return Application {@link MimeTypes}
     */
    MimeTypes mimeTypes();
}
