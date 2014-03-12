# QiWeb Maven Plugin

Maven do not support Java 8 for writing plugins yet.
That's because the maven-plugin-plugin use ASM to read discover mojos and generate the plugin descriptor.

Maven plugin descriptor:
http://books.sonatype.com/mvnref-book/reference/writing-plugins-sect-plugin-descriptor.html

Maven Java 8 issues:
https://cwiki.apache.org/confluence/display/MAVEN/Java+8+Upgrade

Conclusion, no maven plugin for now...

