# QiWeb - Zen and Energy for Web Development

> This is a work in progress!

QiWeb is not published yet. If you read this, feel lucky :-)

If you want to try QiWeb you'll have to :

- build the code from source ;
- build the documentation from source ;
- read unit tests and the mini-sample to get a glimpse at how all this works.

QiWeb is built using [Gradle](http://www.gradle.org/). You don't need to install anything except a JVM, the `gradlew`
script that can be found at the projects root will download and bootstrap Gradle for you.

To get you started, here are some usefull commands:

    ./gradlew clean     # Clean up the project tree
    ./gradlew check     # Run tests
    ./gradlew assemble  # Assemble artifacts
    ./gradlew install   # Install artifacts in local maven repository (~/.m2/repository)

By default version number `0` is used, override with `-Dversion=WHATEVER`

You can then depend on the following artifacts:

- `org.qiweb:org.qiweb.api:0` in your application code
- `org.qiweb:org.qiweb.runtime:0` to run `org.qiweb.runtime.Main` class
- `org.qiweb:org.qiweb.test:0` in your test code

The maven plugin is `org.qiweb:org.qiweb.maven:0`, goal `devshell` to run the Development Shell.

The gradle plugin is `org.qiweb:org.qiweb.gradle:0`, is applied using `apply: 'qiweb'` and has a `devshell` task.
