# QiWeb - Zen and Energy for Web Development

     _____ _ _ _ _     _      _____ ____  _____ 
    |     |_| | | |___| |_   |   __|    \|  |  |
    |  |  | | | | | -_| . |  |__   |  |  |    -|
    |__  _|_|_____|___|___|  |_____|____/|__|__|
       |__|

> This is a work in progress!


## Getting Started

The QiWeb documentation is not published online yet but it is embedded in the QiWeb Development Mode.
We'll bootstrap you there first.


### Installing the QiWeb command line

We are going to create a first application and run it in development mode in order to access the embedded documentation.

Of course, you need a [Java 8 SDK](http://www.oracle.com/technetwork/java/javase/downloads/) installed.

Then check your `JAVA_HOME` environment variable and ensure `java` and `javac` are available in `PATH`).

Now, here are the steps needed to install the `qiweb` command:

- Login to the [QiWeb Repository](https://repo.codeartisans.org/) (username: `qiweb`, password: `qiweb`)
- Download the [CLI distribution](https://repo.codeartisans.org/qiweb/org/qiweb/org.qiweb.cli/0.2.0.Alpha/org.qiweb.cli-0.2.0.Alpha-dist.zip)
- Unzip it
- Prepend the `bin` directory to your `PATH` (ex: `export PATH="/opt/qiweb/bin:${PATH}"`)
- Use the `qiweb` executable on UN*X
- Use the `qiweb.bat` executable on Windows

TIP: Invoke `qiweb --help` to get a comprehensive help on each commands and options.


### Create a new application

To create a new QiWeb application, use the `new` command of `qiweb`:

    user@host ~ $ qiweb new hello-qiweb
    New QiWeb Application generated in '/home/user/hello-qiweb'.
    user@host ~ $ cd hello-qiweb/
    user@host ~/hello-qiweb $


### Run the application in development mode

To run a QiWeb application in development mode, use the `run` command of `qiweb`:

    user@host ~/hello-qiweb $ qiweb run
     _____ _ _ _ _     _      ____          _____ _       _ _ 
    |     |_| | | |___| |_   |    \ ___ _ _|   __| |_ ___| | |
    |  |  | | | | | -_| . |  |  |  | -_| | |__   |   | -_| | |
    |__  _|_|_____|___|___|  |____/|___|\_/|_____|_|_|___|_|_|
       |__|

    Loading...
    Compiling Application...
    >> QiWeb DevShell starting...
    >> Ready for requests on http(s)://127.0.0.1:23023!

You can now open your browser to [http://localhost:23023/](http://localhost:23023/) to see the welcome page.

You should see the following page:

----

![DevShell Welcome Page](org.qiweb/org.qiweb.doc/src/assets/images/welcome.png)

----


### Going further

You'll certainly want a build system to help you manage your application construction, its dependencies, packaging and so on.
The generated application contains sample build files for two build systems: Gradle and Maven.

To use Gradle, rename `build.gradle.example` as `build.gradle`.

To use Maven, rename `pom.xml.example` as `pom.xml`.

Both builds produce executables applications and allow you to run your application in production or development mode.
Respectively `gradle start` or `gradle devshell` and `mvn qiweb:start` or `mvn qiweb:devshell`.
