# QiWeb - Zen and Energy for Web Development

     _____ _ _ _ _     _      ____          _____ _ _
    |     |_| | | |___| |_   |    \ ___ _ _|  |  |_| |_
    |  |  | | | | | -_| . |  |  |  | -_| | |    -| |  _|
    |__  _|_|_____|___|___|  |____/|___|\_/|__|__|_|_|
       |__|

> This is a work in progress!

QiWeb is not published yet. If you read this, feel lucky :-)

If you want to try QiWeb you'll have to :

- build the code from source ;
- build the documentation from source ;
- read unit tests and the mini-sample to get a glimpse at how all this works.

QiWeb is built using [Gradle](http://www.gradle.org/).
You don't need to install anything except a JVM.
The `gradlew` script that can be found at the projects root will download and bootstrap Gradle for you.

The above is mostly true ...
The Gradle based build depend on both `asciidoctor` and the `asciidoc` toolchain to be installed.
We are working towards removing theses dependencies thus having a straight forward build process.
For now, you have to install them both beforehand.

To get you started, here are some usefull commands:

    ./gradlew clean     # Clean up the project tree
    ./gradlew check     # Run tests
    ./gradlew assemble  # Assemble all qiweb artifacts
    ./gradlew install   # Install all qiweb artifacts in local maven repository (~/.m2/repository)

By default version number `0` is used, override with `-Dversion=WHATEVER`

You can then depend on the following artifacts:

- `org.qiweb:org.qiweb.api:0` in your application code
- `org.qiweb:org.qiweb.runtime:0` to run or extend `org.qiweb.runtime.Main` class
- `org.qiweb:org.qiweb.test:0` in your test code to get a test environment easily

The maven plugin is `org.qiweb:org.qiweb.maven:0`, goal `devshell` to run the Development Shell.

The gradle plugin is `org.qiweb:org.qiweb.gradle:0`, is applied using `apply: 'qiweb'` and has a `devshell` task.

Add `./org.qiweb/org.qiweb.cli/build/install/qiweb-cli/bin` to your `PATH` and use the `qiweb-cli`.
