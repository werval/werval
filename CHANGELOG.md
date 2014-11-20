# Changelog

This log of changes contains security fixes, bug fixes, breaking changes, new features or enhancements and dependencies changes.
For minor changes, build system or test changes, see the full [source code history](https://scm.codeartisans.org/qiweb/qiweb/network/master).



## develop - [history](https://scm.codeartisans.org/qiweb/qiweb/network/develop)

> Unreleased!

### Breaking changes

- Plugins can now be registered by names in configuration. Registration by FQCN is still possible but the syntax has changed, see #195
- Gradle plugins IDs have changed to be compliant with the official Gradle Plugin Portal policy, see #212

### Bug fixes

- Fix Plugin's lookup mechanism to correctly honor polymorphism, see #203

### New features

- Application Events, mainly used for metrics, see #190 and below
- Metrics & HealthChecks Plugin with core and filter annotations metrics plus metrics in other modules, see #186 and #202
- [Hashids](http://hashids.org/) support, see #199
- Base62 codec, see #200
- Hassle free application packaging using Gradle, see #210
- The Gradle plugin now has module build support, see #138

### Enhancements

- Refined runtime logging, see #72
- One can now start an application in development or test mode without an `app.secret` defined (even without an `application.conf` file) ; in such a case, a random secret is generated, see #205
- Plugins can now declare dependencies to others, resolved at application activation, see #42 and #209
- The JDBC module now use [HikariCP](http://brettwooldridge.github.io/HikariCP/) instead of [BoneCP](https://github.com/wwadge/bonecp) for connection pooling, see #201
- The JDBC module now provide easy integration with [log4jdbc](https://code.google.com/p/log4jdbc-log4j2/), see #145
- Gradle plugin now comes with a `dev` `SourceSet`, that is `src/dev/java` and `src/dev/resources`, see #213
- Add `extraWatch` parameter to Maven plugin `devshell` goal, see #196
- Allow setting application config file in Maven plugin configuration for `devshell` and `start` goals, see #189
- Allow to add `extraClassPath` entries in Maven plugin configuration for `devshell` and `start` goals, see #188
- Allow setting application config file in Gradle plugin configuration for `devshell` and `start` tasks, see #187
- Allow to add `SourceSets` in Gradle plugin configuration for `devshell` and `start` tasks, see #104

### Dependency changes

- Core
    - Upgrade Netty from `4.0.23` to `4.0.24`, see the announcement:
      [4.0.24](http://netty.io/news/2014/10/29/4-0-24-Final.html)
- Modules
    - Upgrade EhCache from `2.8.4` to `2.9.0`, see the [changes-report](http://www.ehcache.org/changes-report)
    - Upgrade Liquibase from `3.2.2` to `3.3.0`, see the [announcement](http://blog.liquibase.org/2014/11/liquibase-3-3-0-released.html)
- Tooling
    - Upgrade Gradle from `2.1` to `2.2`, see the [release-notes](http://www.gradle.org/docs/2.2/release-notes)



## 0.3.3 - [history](https://scm.codeartisans.org/qiweb/qiweb/network/master?utf8=%E2%9C%93&extended_sha1=0.3.3)

> Punk is not dead and has been bugfixed, more than a few times!

#### Bug fixes

- Fix Plugin.afterInterfaction hook that was never called, see #191



## 0.3.2 - [history](https://scm.codeartisans.org/qiweb/qiweb/network/master?utf8=%E2%9C%93&extended_sha1=0.3.2)

> Punk is not dead and has been bugfixed, twice!

#### Bug fixes

- Fix multipart form uploads failing when < 8k, see #182



## 0.3.1-beta - [history](https://scm.codeartisans.org/qiweb/qiweb/network/master?utf8=%E2%9C%93&extended_sha1=0.3.1-beta)

> Punk is not dead and has been bugfixed!

### Bug fixes

- DevShell: links to application sources in stacktraces are back, a bug removed them, see #178
- DevShell: source code changes are now properly detected on absents files/dirs, see #136
- DevShell: fix application source URL corner-case bug when several files have the same name, see #177
- DevShell: when a build error occurs, the build log is now showed in the browser, see #176

### New features

- Add `@LogIfSlow` filter annotation that logs slow interactions, see #180



## 0.3.0-beta - [history](https://scm.codeartisans.org/qiweb/qiweb/network/master?utf8=%E2%9C%93&extended_sha1=0.3.0-beta)

> Punk is not dead!

That release has been a long one.
Core has been stabilized, a lot of new features added, existing ones enhanced.
Along the way, breaking changes were mades. That's what beta versions are for!


### Breaking changes

- Moved `org.qiweb.api.util` to `org.qiweb.util`, see #174
- Moved all standard controllers from `org.qiweb.api.controllers` to `org.qiweb.controllers`, see #160
- Moved `@Cached` from `org.qiweb.api.cache` to `org.qiweb.filters`, see #171
- Renamed `StaticFiles` to `Static`, see #124
- Renamed `ClasspathResources` to `Classpath`, see #123
- Renamed `Global::onApplicationError ` to `Global::onRequestError`, see #159
- The Rythm Plugin now embrace the Templates API and must be used through it, see #172
- Gradle Plugin development mode build logic revamped, see #125

### Bug fixes

- Fix multi-cookies parsing from requests, see #120
- Fix handling of request with Content-Type but no Charset, see #121
- First request in development mode no longer trigger a superflous rebuild, this is snappier!, see #116
- Fix Application reload after global error in DevShell, see #113
- Fix POST requests parsing when Keep-Alive in Netty Server, see #152
- Fix DevShell classpath in Gradle Plugin, see #153

### New features

- Application now has a startup banner, see #158
- Application now has Executors (thread pools) and all application code is run in the default one, see #149
- Security annotations using Filters for `X-Frame-Options`, `X-XSS-Protection`, `X-Content-Type-Options`,
  `Strict-Transport-Security`, `Content-Security-Policy`and Do No Track (`DNT`) headers, see #167, #9, #10, #168
- Server-side templating API supported by plugins for [Rythm](http://rythmengine.org/) and [Thymeleaf](http://www.thymeleaf.org/) engines, see #172 and #173
- [Liquibase](http://www.liquibase.org/) Plugin for managed database migrations, see #122
- Proper JSON and [JSON-P](https://en.wikipedia.org/wiki/JSONP) support using [Jackson](http://wiki.fasterxml.com/JacksonHome), see #96
- Add a Cache implementation backed by [Redis](http://redis.io/), see #112
- Add `@NeverCache` filter, see #170
- Maven support on par with Gradle support, see #97
- Add `start` task/goal to the Gradle and Maven build plugins that start the application in production mode, see #101

### Enhancements

- Application Routes are now logged upon activation, see #130
- Controller methods can now return either a plain `Outcome` or a `CompletableFuture<Outcome>`, see #148
- Outome builder API now expose helpers for common Content-Types and Charsets, see #128
- The Cache API now leverage Java 8 Lambdas, see #127
- The `Classpath` and `Static` controllers gets uniform index files support, see #134 and #137
- Filters annotation can now be `@Repeatable` thanks to Java 8, see #169
- Plugins can now contribute routes with regards to the application mode (`DEV`, `PROD`, `TEST`), see #119
- Plugins can now use other plugins during activation/passivation, see #42
- Plugins can now contribute to interactions context, see #166
- Modules can now contribute dynamic documentation to `/@doc`, see #71
- The JPA Plugin now has clear lifecycle and thread/context model plus a `@Transactional` annotation, see #114
- The JPA Plugin now redirects all EclipseLink logging to SLF4J, see #115
- Resources at `/@doc` in development mode are now all in a dedicated DocumentationPlugin, see #106 and #71
- Development mode can now watch individual files, see #19
- Add a file based run lock for the DevShell for easier process monitoring by both the build plugins and their integration tests, #98
- CLI, Gradle and Maven support converged in `org.qiweb.commands`, see #111
- Move Gradle Plugin tasks configuration into tasks, see #103
- Enhance the application skeleton generated by `qiweb new`, see #73, #100

### Dependency changes

- Core
    - Upgrade Netty from `4.0.18` to `4.0.23`, see all the announcements:
      [4.0.19](http://netty.io/news/2014/04/30/release-day.html),
      [4.0.20](http://netty.io/news/2014/06/12/4.html),
      [4.0.21](http://netty.io/news/2014/07/01/4.html),
      [4.0.22](http://netty.io/news/2014/08/14/4-0-22-Final.html),
      [4.0.23](http://netty.io/news/2014/08/15/4-0-23-Final-and-4-1-0-Beta3.html)
    - Upgrade Javassist from `3.18.1-GA` to `3.18.2-GA`, see the [history](https://github.com/jboss-javassist/javassist/commits/3.18)
    - Upgrade Typesafe Config from `1.2.0` to `1.2.1`, see the [history](https://github.com/typesafehub/config)
- Modules
    - Upgrade EclipseLink from `2.5.1` to `2.5.2`, see the [release-notes](http://www.eclipse.org/eclipselink/releases/index.php)
    - Upgrade EhCache from `2.8.2` to `2.8.4`, see the [changes-report](http://www.ehcache.org/changes-report)
    - Upgrade Commons Email from `1.3.2` to `1.3.3`, see the [changes-report](http://commons.apache.org/proper/commons-email/changes-report.html)
    - Upgrade spymemcached from `2.11.1` to `2.11.4`, see the [release-notes](https://github.com/couchbase/spymemcached/releases)
    - Upgrade Rythm Engine from `1.0` to `1.0.1`, see the [history](https://github.com/greenlaw110/Rythm/commits/1.0)
    - Upgrade Spring from `4.0.3` to `4.1.1`, see the [changes-report](http://docs.spring.io/spring-framework/docs/4.1.x/spring-framework-reference/htmlsingle/#new-in-4.1)
- Tooling
    - Upgrade Gradle from `1.11` to `2.1`, see all the release-notes:
      [1.12](http://www.gradle.org/docs/1.12/release-notes),
      [2.0](http://www.gradle.org/docs/2.0/release-notes),
      [2.1](http://www.gradle.org/docs/2.1/release-notes)



## 0.2.0.Alpha - [history](https://scm.codeartisans.org/qiweb/qiweb/network/master?utf8=%E2%9C%93&extended_sha1=0.2.0.Alpha)

> Complete HTTP Protocol Support, Caching and various enhancements

### Breaking changes

- Moved `org.qiweb.lib.controllers` into `org.qiweb.api.controllers`, see #87 & #91

### Bug fixes

- Fix QueryString multi-empty-valued parameters handling, see #78
- Fix parsing of escaped quotes in forced/defaulted route parameters, see #79

### New features

- Add HTTP Language Negotiation helpers, see #85
- Add HTTP Content Negotiation helpers, see #7
- Complete Cookies support, see #61
- Add a Cache Extension Plugin and EhCache & Memcache based implementations, see #88
- Add `@Cached` annotation that leverage both server-side and client-side caching, see #90
- Crypto helpers for `SHA-256`

### Enhancements

- Add support for default parameters values in Routes, see #76
- Routes contributed by plugins can now easily be prefixed, see #86
- Filters can now be declared using custom annotations, see #89
- The CLI now generate a `build.gradle` file in new applications, see #92
- Better error reporting on passivation
- Minor enhancements to test support
- Some progress towards Windows support in CLI, untested!

### Dependency changes

- Core
    - Upgrade Netty from `4.0.17` to `4.0.18`, see the [announcement](http://netty.io/news/2014/04/01/4-0-18-Final.html)



## 0.1.3.Alpha - [history](https://scm.codeartisans.org/qiweb/qiweb/network/master?utf8=%E2%9C%93&extended_sha1=0.1.3.Alpha)

> Hotfix!

- Add support for `DATABASE_URL` Heroku syntax to the JDBC Plugin, fixes samples deployment on Heroku, see #69



## 0.1.2.Alpha - [history](https://scm.codeartisans.org/qiweb/qiweb/network/master?utf8=%E2%9C%93&extended_sha1=0.1.2.Alpha)

> Hotfix!

- Fix Rythm Plugin template resolution in prod mode, see #74



## 0.1.1.Alpha - [history](https://scm.codeartisans.org/qiweb/qiweb/network/master?utf8=%E2%9C%93&extended_sha1=0.1.1.Alpha)

> Hotfix!

- Fix CLI new app generation bugs
- DevShell now advertise the app `URL` to `STDOUT` on startup



## 0.1.0.Alpha - [history](https://scm.codeartisans.org/qiweb/qiweb/network/master?utf8=%E2%9C%93&extended_sha1=0.1.0.Alpha)

> First release!

