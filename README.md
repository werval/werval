# QiWeb - Zen and Energy for Web Development

> This is a work in progress!

    ./gradlew clean
    ./gradlew test
    ./gradlew install

By default version number 0 is used.

You can then depend on the following artifacts:

- `org.qiweb:org.qiweb.api:0` in your application code
- `org.qiweb:org.qiweb.runtime:0` to run `org.qiweb.runtime.Main` class

The maven plugin is `org.qiweb:org.qiweb.maven:0`, goal `devshell` to run the Development Shell.

The gradle plugin is `org.qiweb.org.qiweb.gradle:0`, is applied using `apply: 'qiweb'` and has a `devshell` task.

