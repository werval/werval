# Changes

## Versioning Scheme

    X.Y.Z.Qualifer

- X Major version number
- Y Minor version number
- Z Release number
- Qualifer can be Alpha, Beta, RC or Final.

Only use Final versions in production!


## Changelog


### 0.2.0.Alpha

- Fix QueryString multi-empty-valued parameters handling, see #78
- Fix parsing of escaped quotes in forced/defaulted route parameters, see #79
- Add HTTP Language Negotiation helpers, see #85
- Add HTTP Content Negotiation helpers, see #7
- Add a Cache Extension Plugin and EhCache & Memcache based implementations, see #88
- Add @Cached annotation that leverage both server-side and client-side caching, see #90
- Complete Cookies support, see #61
- Add support for default parameters values in Routes, see #76
- Routes contributed by plugins can now easily be prefixed, see #86
- Filters can now be declared using custom annotations, see #89
- The CLI now generate a build.gradle file in new applications, see #92
- Moved `org.qiweb.lib.controllers` into `org.qiweb.api.controllers`, see #87 & #91
- Better error reporting on passivation
- Minor enhancements to test support
- Crypto helpers for SHA-256
- Some progress towards Windows support in CLI
- Reintroduce Gradle Wrapper in all projects
- Upgrade Netty from 4.0.17 to 4.0.18


### 0.1.3.Alpha

- Fix Rythm Plugin template resolution in prod mode, see #74
- Add support for DATABASE_URL Heroku syntax to the JDBC Plugin, see #69
- Upgrade Spring dependency from 4.0.2 to 4.0.3 that comes with [Java 8 support](http://spring.io/blog/2014/03/27/spring-framework-4-0-3-released-with-java-8-support-now-production-ready)


### 0.1.1.Alpha

- Fix CLI new app generation bugs
- DevShell now advertise the app URL to STDOUT on startup


### 0.1.0.Alpha

First release!

