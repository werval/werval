# QiWeb Maven Plugin

## Status

Maven do not support Java 8 for writing plugins yet.
That's because the maven-plugin-plugin use ASM to read discover mojos and generate the plugin descriptor.

Maven plugin descriptor:
http://books.sonatype.com/mvnref-book/reference/writing-plugins-sect-plugin-descriptor.html

Maven Java 8 issues:
https://cwiki.apache.org/confluence/display/MAVEN/Java+8+Upgrade

Conclusion, no maven plugin for now...

To generate a HelpMojo that builds well, the very same maven-plugin-plugin create invalid javadoc.
Java 8 enforce strict javadoc.
See http://jira.codehaus.org/browse/MPLUGIN-244

## Testing

See the
[Review of Plugin Testing Strategies](http://docs.codehaus.org/display/MAVENUSER/Review+of+Plugin+Testing+Strategies)
CodeHaus wiki for reference.

