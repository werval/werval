# Werval Maven Plugin


## Testing

See the
[Review of Plugin Testing Strategies](http://docs.codehaus.org/display/MAVENUSER/Review+of+Plugin+Testing+Strategies)
CodeHaus wiki for reference.
Also see the [Maven Unit and Integration Test Guide](http://khmarbaise.github.io/maui/) that contains many examples of
using the `maven-invoker-plugin`.

Actual unit testing is done using `maven-plugin-testing-harness`.
Integration testing leverage the `install` goal of `maven-invoker-plugin` to create a temporary local repository and
is implemented using `maven-failsafe-plugin` and a JUnit test that uses `maven-invoker` directly.


## Others

Maven plugin descriptor:
http://books.sonatype.com/mvnref-book/reference/writing-plugins-sect-plugin-descriptor.html

Maven Java 8 issues:
https://cwiki.apache.org/confluence/display/MAVEN/Java+8+Upgrade
