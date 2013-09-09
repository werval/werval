# QiWeb - Zen and Energy for Web Development

     _____ _ _ _ _     _      ____          _____ _ _
    |     |_| | | |___| |_   |    \ ___ _ _|  |  |_| |_
    |  |  | | | | | -_| . |  |  |  | -_| | |    -| |  _|
    |__  _|_|_____|___|___|  |____/|___|\_/|__|__|_|_|
       |__|

> This is a work in progress!

QiWeb is not published yet. If you read this, feel lucky :-)

If you want to try QiWeb you'll have to build the code and documentation from
source.

QiWeb is built using Gradle (http://www.gradle.org/). You don't need to
install anything except a JVM. The `gradlew` script that can be found at the
projects root will download and bootstrap Gradle for you. If you are
interested in this Gradle feature, check the Gradle Wrapper
(http://www.gradle.org/docs/current/userguide/gradle_wrapper.html)
documentation.

Please note that if you want to get UML diagrams generated in Javadocs you'll
need to have GraphViz (http://www.graphviz.org/) installed. The build will pass
without though. But with less fun.

To get you started, here are some usefull commands:

    ./gradlew clean             # Clean up the whole project tree
    ./gradlew install           # Install all qiweb artifacts
    ./gradlew install check     # Run all tests

It is recommended to run all tests first, giving you confidence that the whole
thing work on your computer.

To run all tests you must `install` first as build plugins are tested along the
way and they need to be installed in order to be used by the tests and samples.
This is why the command to run tests call the `install` and `check` tasks.

QiWeb do not have much dependencies but the build system and the tests do.
As a consequence, a vast amount of code is downloaded the first time you
run a build. Theses downloads are cached, at the time of writing running a
full build with every dependencies in cache take less than two minutes.

QiWeb artifacts produced by the build are installed in your local maven
repository (`~/.m2/repository`).

Once installed you can depend on the following artifacts:

- `org.qiweb:org.qiweb.api:0` in your application code
- `org.qiweb:org.qiweb.lib:0` in your app code, this is the standard library
- `org.qiweb:org.qiweb.runtime:0` to run or extend `org.qiweb.runtime.Main`
- `org.qiweb:org.qiweb.test:0` in your test code to get a test environment

The maven plugin is `org.qiweb:org.qiweb.maven:0`, goal `qiweb:devshell` to run the
Development Shell.

The gradle plugin is `org.qiweb:org.qiweb.gradle:0`, is applied using
`apply: 'qiweb'` and has a `devshell` task.

If you want to test drive the `qiweb-cli` command-line app-skeleton generator
and development shell, the following commands will install it:

    # Create ~/opt if it does not exist
    if [ ! -d ~/opt ] ; then mkdir -p ~/opt ; fi
    # Unzip qiweb-cli distribution in ~/opt
    unzip -l ~/.m2/repository/org/qiweb/org.qiweb.cli/0/org.qiweb.cli-0-dist.zip -d ~/opt
    # Add qiweb-cli to your $PATH
    export PATH=~/opt/qiweb-cli-0/bin:$PATH

By default version number `0` is used, you can override this with
`-Dversion=WHATEVER`.

If you encounter any problem, please fill an issue at
https://scm.codeartisans.org/paul/qiweb/issues with the output of the build
process.
