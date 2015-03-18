POM-less Tycho builds
----------------------

This a maven build extension which enables (almost) pom-less Tycho builds.
It derives the maven pom model from an OSGi MANIFEST with the mapping rules known from Tycho already, i.e. Bundle-SymbolicName == artifactId and pom version == Bundle-Version (with .qualifier version suffix in MANIFEST translated to -SNAPSHOT suffix in pom)
Packaging eclipse-plugin is used for bundles, if the Bundle-SymbolicName ends with ".tests", packaging type eclipse-test-plugin is used.
Tycho still needs at least a parent pom for common configuration and aggregation, but bundles and features with no further info in pom.xml (and I assume this is the 95% case) can now be built pom-less.

This is experimental and not for productive use.

How to try it?
--------------

To build the build extension:

```mvn clean install```

`src/test/resources/testpomless` is a demo project with a pom-less bundle, test bundle and feature.

To build the demo project, you need at least maven 3.3.1 to be able to use the new [build extension mechanism](http://jira.codehaus.org/browse/MNG-5771) `.mvn/extensions.xml`.

In `src/test/resources/testpomless`:

```mvn install```

You should see a bundle, feature and test bundle being built (all without pom.xml).

Feedback welcome, contact tycho-dev@eclipse.org .