# Changelog

This log of changes contains security fixes, bug fixes, breaking changes, new features or enhancements and dependencies changes.
For minor changes, build system or test changes, see the full [source code history](https://github.com/werval/werval).



## 0.7.1 - [history](https://github.com/werval/werval/commits/0.7.1)

> HotFix 0.7!

### Bug fixes

- (#242) Fix `@AcceptContentType` filter failure when `Content-Type` header absent



## 0.7.0 - [history](https://github.com/werval/werval/commits/0.7.0)

> One step at a time!

### Breaking changes

- (#246) APIs now leverage Java 8 `java.util.Optional`

### Bug fixes

- (#239) Fix ReverseRoutes NPE if controller is interface and no parameters
- (#242) Fix `@AcceptContentType` filter failure on `Content-Type` headers with charset info

### New features

- (#5) CORS support
- (#241) JSON Web Token support

### Enhancements

- (#240) Allow filter annotations on filter annotations
- (#243) Allow global filter annotations on Global object
- (#244) DevShell now open `http://localhost:port/` instead of `http://0.0.0.0:port/` when `werval.http.address` is `0.0.0.0`



## 0.6.0 - [history](https://github.com/werval/werval/commits/0.6.0)

> [U can't touch this](http://grooveshark.com/s/U+Cant+Touch+This/3sWgpi)

### Breaking changes

- (#233) `Config.object( String key )` has been replaced by `Config.atKey( String key )` and `Config.atPath( String path )`

### Bug fixes

- (#232 & #233) Fix dynamic modules documentation in development mode
- (#235) Fix QueryString.Decoder that incorrectly added a null query param

### New features

- (#194) Hashid parameter binder

### Enhancements

- (#237) Controllers type lookup packages for routes can now be set in configuration
- (#68) Server bootstrap now loads `Application` and `HttpServer` implementations through `ServiceLoader`
- (#164) Performance enhancement, all `equals()` methods have been short-circuited
- (#193) Tests now use a randomly choosen free port in test mode allowing to run tests while development mode is running

### Dependency changes

- Core
    - Upgrade SLF4J from `1.7.7` to `1.7.10`, see the [annoucements](http://www.slf4j.org/news.html)
    - Upgrade Netty from `4.0.24` to `4.0.25`, see the announcement:
      [4.0.25](http://netty.io/news/2014/12/31/4-0-25-Final.html)
- Modules
    - Upgrade Jackson from `2.4.4` to `2.5.0`, see the [release-notes](https://github.com/FasterXML/jackson/wiki/Jackson-Release-2.5)
    - Upgrade Jedis from `2.6.1` to `2.6.2`, see the [release-notes](https://github.com/xetorthio/jedis/releases)
    - Upgrade Liquibase from `3.3.0` to `3.3.2`, see the [announcement](http://blog.liquibase.org/2015/01/liquibase-3-3-2-released.html)
    - Upgrade spymemcached from `2.11.4` to `2.11.5`, see the [history](https://github.com/couchbase/spymemcached)
    - Upgrade Spring from `4.1.2` to `4.1.4`, see the [changes-report](http://docs.spring.io/spring-framework/docs/4.1.x/spring-framework-reference/htmlsingle/#new-in-4.1)
    - Upgrade Thymeleaf from `2.1.3` to `2.1.4`, see the [changelog](https://github.com/thymeleaf/thymeleaf/blob/thymeleaf-2.1.4.RELEASE/ChangeLog.txt)



## 0.5.1 - [history](https://github.com/werval/werval/commits/0.5.1)

> Werval on Github, Travis & JCenter

This release contains only build & infrastructure changes.



## 0.5.0 - [history](https://github.com/werval/werval/commits/0.5.0)

> Werval!

### Breaking changes

- (#226) Renamed the project to `werval` changing artifacts ids, packages etc... a massive breaking-change

### New features

- (#3) Sanitize Module, allow easy inputs sanitization and outputs encoding thanks to several OWASP projects

### Dependency changes

- Tooling
    - Upgrade Gradle from `2.2` to `2.2.1`, see the [release-notes](http://www.gradle.org/docs/2.2.1/release-notes)


## 0.4.0 - [history](https://github.com/werval/werval/commits/0.4.0)

> [Dwr Budr - Orbital - In Sides](http://grooveshark.com/s/Dwr+Budr/2Nl0gw?src=5)

### Breaking changes

- (#195) Plugins can now be registered by names in configuration. Registration by FQCN is still possible but the syntax has changed
- (#212) Gradle plugins IDs have changed to be compliant with the official Gradle Plugin Portal policy
- (#216) Error responses now comply with content negiciation (`HTML`, `JSON` or `text/plain`). `Global.onRequestError(...)` method signature had to be changed.

### Bug fixes

- (#203) Fix Plugin's lookup mechanism to correctly honor polymorphism

### New features

- (#190) Application Events, mainly used for metrics, see below
- (#186) & #202 Metrics & HealthChecks Plugin with core and filter annotations metrics plus metrics in other modules
- (#133) XML Module, secure by default, gradually relaxable, provide a Plugin allowing easy production and consumption of XML
- (#199) [Hashids](http://hashids.org/) support
- (#200) Base62 codec
- (#138) The Gradle plugin now has module build support

### Enhancements

- (#72) Refined runtime logging
- (#205) One can now start an application in development or test mode without an `app.secret` defined (even without an `application.conf` file) ; in such a case, a random secret is generated
- (#42 & #209) Plugins can now declare dependencies to others, resolved at application activation
- (#201) The JDBC module now use [HikariCP](http://brettwooldridge.github.io/HikariCP/) instead of [BoneCP](https://github.com/wwadge/bonecp) for connection pooling
- (#145) The JDBC module now provide easy integration with [log4jdbc](https://code.google.com/p/log4jdbc-log4j2/)
- (#187 & #189) Allow setting application config location in Gradle and Maven plugins for the `devshell` and `start` tasks/goals
- (#214) Gradle and Maven plugins now open the default browser upon development mode startup
- (#104) Allow to add `SourceSets` in Gradle plugin configuration for the `devshell` and `start` tasks
- (#215) Gradle plugin now keep a persistent daemon connection for faster rebuilds
- (#213) Gradle plugin now comes with a `dev` `SourceSet`, that is `src/dev/java` and `src/dev/resources`
- (#210) Gradle plugin now comes with hassle-free application packaging
- (#188) Allow to add `extraClassPath` entries in Maven plugin configuration for the `devshell` and `start` goals
- (#196) Add `extraWatch` parameter to Maven plugin `devshell` goal

### Dependency changes

- Core
    - Upgrade Netty from `4.0.23` to `4.0.24`, see the announcement:
      [4.0.24](http://netty.io/news/2014/10/29/4-0-24-Final.html)
- Modules
    - Upgrade EhCache from `2.8.4` to `2.9.0`, see the [changes-report](http://www.ehcache.org/changes-report)
    - Upgrade Liquibase from `3.2.2` to `3.3.0`, see the [announcement](http://blog.liquibase.org/2014/11/liquibase-3-3-0-released.html)
- Tooling
    - Upgrade Gradle from `2.1` to `2.2`, see the [release-notes](http://www.gradle.org/docs/2.2/release-notes)



## 0.3.3 - [history](https://github.com/werval/werval/commits/0.3.3)

> Punk is not dead and has been bugfixed, more than a few times!

#### Bug fixes

- (#191) Fix Plugin.afterInterfaction hook that was never called



## 0.3.2 - [history](https://github.com/werval/werval/commits/0.3.2)

> Punk is not dead and has been bugfixed, twice!

#### Bug fixes

- (#182) Fix multipart form uploads failing when < 8k



## 0.3.1-beta - [history](https://github.com/werval/werval/commits/0.3.1-beta)

> Punk is not dead and has been bugfixed!

### Bug fixes

- (#178) DevShell: links to application sources in stacktraces are back, a bug removed them
- (#136) DevShell: source code changes are now properly detected on absents files/dirs
- (#177) DevShell: fix application source URL corner-case bug when several files have the same name
- (#176) DevShell: when a build error occurs, the build log is now showed in the browser

### New features

- (#180) Add `@LogIfSlow` filter annotation that logs slow interactions



## 0.3.0-beta - [history](https://github.com/werval/werval/commits/0.3.0-beta)

> Punk is not dead!

That release has been a long one.
Core has been stabilized, a lot of new features added, existing ones enhanced.
Along the way, breaking changes were mades. That's what beta versions are for!


### Breaking changes

- (#174) Moved `org.qiweb.api.util` to `org.qiweb.util`
- (#160) Moved all standard controllers from `org.qiweb.api.controllers` to `org.qiweb.controllers`
- (#171) Moved `@Cached` from `org.qiweb.api.cache` to `org.qiweb.filters`
- (#124) Renamed `StaticFiles` to `Static`
- (#123) Renamed `ClasspathResources` to `Classpath`
- (#159) Renamed `Global::onApplicationError ` to `Global::onRequestError`
- (#172) The Rythm Plugin now embrace the Templates API and must be used through it
- (#125) Gradle Plugin development mode build logic revamped

### Bug fixes

- (#120) Fix multi-cookies parsing from requests
- (#121) Fix handling of request with Content-Type but no Charset
- (#116) First request in development mode no longer trigger a superflous rebuild, this is snappier!
- (#113) Fix Application reload after global error in DevShell
- (#152) Fix POST requests parsing when Keep-Alive in Netty Server
- (#153) Fix DevShell classpath in Gradle Plugin

### New features

- (#158) Application now has a startup banner
- (#149) Application now has Executors (thread pools) and all application code is run in the default one
- (#167, #9, #10 & #168) Security annotations using Filters for `X-Frame-Options`, `X-XSS-Protection`, `X-Content-Type-Options`,
  `Strict-Transport-Security`, `Content-Security-Policy`and Do No Track (`DNT`) headers
- (#172 & #173) Server-side templating API supported by plugins for [Rythm](http://rythmengine.org/) and [Thymeleaf](http://www.thymeleaf.org/) engines
- (#122) [Liquibase](http://www.liquibase.org/) Plugin for managed database migrations
- (#96) Proper JSON and [JSON-P](https://en.wikipedia.org/wiki/JSONP) support using [Jackson](http://wiki.fasterxml.com/JacksonHome)
- (#112) Add a Cache implementation backed by [Redis](http://redis.io/)
- (#170) Add `@NeverCache` filter
- (#97) Maven support on par with Gradle support
- (#101) Add `start` task/goal to the Gradle and Maven build plugins that start the application in production mode

### Enhancements

- (#130) Application Routes are now logged upon activation
- (#148) Controller methods can now return either a plain `Outcome` or a `CompletableFuture<Outcome>`
- (#128) Outome builder API now expose helpers for common Content-Types and Charsets
- (#127) The Cache API now leverage Java 8 Lambdas
- (#134 & #137) The `Classpath` and `Static` controllers gets uniform index files support
- (#169) Filters annotation can now be `@Repeatable` thanks to Java 8
- (#119) Plugins can now contribute routes with regards to the application mode (`DEV`, `PROD`, `TEST`)
- (#42) Plugins can now use other plugins during activation/passivation
- (#166) Plugins can now contribute to interactions context
- (#71) Modules can now contribute dynamic documentation to `/@doc`
- (#114) The JPA Plugin now has clear lifecycle and thread/context model plus a `@Transactional` annotation
- (#115) The JPA Plugin now redirects all EclipseLink logging to SLF4J
- (#106 & #71) Resources at `/@doc` in development mode are now all in a dedicated DocumentationPlugin
- (#19) Development mode can now watch individual files
- (#98) Add a file based run lock for the DevShell for easier process monitoring by both the build plugins and their integration tests
- (#111) CLI, Gradle and Maven support converged in `org.qiweb.commands`
- (#103) Move Gradle Plugin tasks configuration into tasks
- (#73 & #100) Enhance the application skeleton generated by `qiweb new`

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



## 0.2.0.Alpha - [history](https://github.com/werval/werval/commits/0.2.0.Alpha)

> Complete HTTP Protocol Support, Caching and various enhancements

### Breaking changes

- (#87 & #91) Moved `org.qiweb.lib.controllers` into `org.qiweb.api.controllers`

### Bug fixes

- (#78) Fix QueryString multi-empty-valued parameters handling
- (#79) Fix parsing of escaped quotes in forced/defaulted route parameters

### New features

- (#85) Add HTTP Language Negotiation helpers
- (#7) Add HTTP Content Negotiation helpers
- (#61) Complete Cookies support
- (#88) Add a Cache Extension Plugin and EhCache & Memcache based implementations
- (#90) Add `@Cached` annotation that leverage both server-side and client-side caching
- Crypto helpers for `SHA-256`

### Enhancements

- (#76) Add support for default parameters values in Routes
- (#86) Routes contributed by plugins can now easily be prefixed
- (#89) Filters can now be declared using custom annotations
- (#92) The CLI now generate a `build.gradle` file in new applications
- Better error reporting on passivation
- Minor enhancements to test support
- Some progress towards Windows support in CLI, untested!

### Dependency changes

- Core
    - Upgrade Netty from `4.0.17` to `4.0.18`, see the [announcement](http://netty.io/news/2014/04/01/4-0-18-Final.html)



## 0.1.3.Alpha - [history](https://github.com/werval/werval/commits/0.1.3.Alpha)

> Hotfix!

- (#69) Add support for `DATABASE_URL` Heroku syntax to the JDBC Plugin, fixes samples deployment on Heroku



## 0.1.2.Alpha - [history](https://github.com/werval/werval/commits/0.1.2.Alpha)

> Hotfix!

- (#74) Fix Rythm Plugin template resolution in prod mode



## 0.1.1.Alpha - [history](https://github.com/werval/werval/commits/0.1.1.Alpha)

> Hotfix!

- Fix CLI new app generation bugs
- DevShell now advertise the app `URL` to `STDOUT` on startup



## 0.1.0.Alpha - [history](https://github.com/werval/werval/commits/0.1.0.Alpha)

> First release!

