POM-less Tycho builds
----------------------

This a maven build extension which enables (almost) pom-less Tycho builds.
It derives the maven pom model from an OSGi MANIFEST with the mapping rules known from Tycho already, i.e. Bundle-SymbolicName == artifactId and pom version == Bundle-Version (with .qualifier version suffix in MANIFEST translated to -SNAPSHOT suffix in pom)
Packaging eclipse-plugin is used for bundles, if the Bundle-SymbolicName ends with ".tests", packaging type eclipse-test-plugin is used.
Tycho still needs at least a parent pom for common configuration and aggregation, but bundles and features with no further info in pom.xml (and I assume this is the 95% case) can now be built pom-less.

This is experimental and not for productive use.

How to try it?
--------------

```mvn clean install```
At this point you need to build maven 3.2.6-SNAPSHOT locally (master branch of https://github.com/apache/maven) 
Maven 3.2.6-SNAPSHOT has new feature http://jira.codehaus.org/browse/MNG-5771 which this build extension is based on.
Set M2_HOME to the locally built and extracted maven distro.

src/test/resources/testpomless is a demo project with a pom-less bundle, test bundle and feature.

Use 'mvn install' to build the demo project .

Feedback welcome, contact tycho-dev@eclipse.org .