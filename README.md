# QiWeb - Zen and Energy for Web Development

     _____ _ _ _ _     _      _____ ____  _____ 
    |     |_| | | |___| |_   |   __|    \|  |  |
    |  |  | | | | | -_| . |  |__   |  |  |    -|
    |__  _|_|_____|___|___|  |_____|____/|__|__|
       |__|

> This is a work in progress!

QiWeb is not published yet. If you read this, feel lucky :-)


## Getting Started

The QiWeb documentation is not published online yet but it is embedded in the QiWeb Development Mode. We'll bootstrap you there first.


### Installing the QiWeb command line

We are going to create a first application and run it in development mode in order to access the embedded documentation.

Of course, you need a Java 8 SDK installed (http://www.oracle.com/technetwork/java/javase/downloads/).

Then check your `JAVA_HOME` environment variable and ensure `java` and `javac` are available in `PATH`).

Now, here are the steps needed to install the `qiweb` command:

- Download the CLI distribution from  https://repo.codeartisans.org/qiweb/org/qiweb/org.qiweb.cli/0.1.0.Alpha/org.qiweb.cli-0.1.0.Alpha-dist.zip
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

You can now open your browser to http://localhost:23023/ to see the welcome page.

You should see the following page:

![DevShell Welcome Page](org.qiweb/org.qiweb.doc/src/assets/images/welcome.png)


### Browse the embedded documentation

Simply open http://localhost:23023/@doc to browse the embedded documentation, including javadocs.

If you encounter any problem, please fill an issue at https://scm.codeartisans.org/paul/qiweb/issues with the maximum information about your problem.


## Build QiWeb from the Source

QiWeb is built using Gradle (http://www.gradle.org/).

You need to install Java 8 JDK (http://www.oracle.com/technetwork/java/javase/downloads/) and Gradle (http://www.gradle.org/).

This git repository contains several independent projects.

    org.qiweb               QiWeb Core
    org.qiweb.modules       Modules
    org.qiweb.gradle        Gradle Plugin
    org.qiweb.maven         Maven Plugin

For convenience, two shell scripts are provided:

    clean.sh                Clean the repository of built artifacts
    build.sh                Quick build without tests
    check.sh                Full build with all tests

Please note that if you want to get UML diagrams generated in Javadocs you'll need to have GraphViz (http://www.graphviz.org/) installed. The build will pass without though. But with less fun.

When working on the QiWeb source code, it is recommended to run all tests first, giving you confidence that the whole thing work on your computer. You can do that easily by running the `check.sh` build script.

QiWeb do not have much dependencies but the build system and the tests do. As a consequence, a vast amount of code is downloaded the first time you run a build. Theses downloads are cached in `~/.gradle/caches`.

QiWeb artifacts produced by the build are installed in your local maven repository (`~/.m2/repository`) for consumption by other applications and in the `repository` directory of this very git repository for use by different projects under this repository umbrella.

By default version number `0` is used, you can override this with `-Dversion=WHATEVER`.

If you encounter any problem, please fill an issue at https://scm.codeartisans.org/paul/qiweb/issues with the output of the build process.

