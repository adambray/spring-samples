# Spring Samples

This is a sample project that demonstrates a couple of useful Spring maneuvers
for reference purposes. It has all the trappings of an actual application that
does actual work (namely, an API for creating and querying events) to make it
easier to reason about how these techniques can be used.

## Building the project

You'll need Java 8 installed.

Clone the project, and in the project directory run:

```bash
$ ./gradlew clean test
```

When working in an IDE, you'll need to have annotation processing enabled, and
you'll probably need a Lombok plugin.

## Points of interest

### Multi-project Gradle build

The project consists of a `core` module containing high-level use cases, an
`api` module containing a deployable Spring Boot app that provides the delivery
mechanism, and a `sql-backed-persistence` module containing a database plugin.
The gradle buildscripts define the dependencies between the modules.

In particular, the `sql-backed-persistence` module has a test dependency on the
`core` module's test source set, because it needs access to an abstract
repository test to extend for the implementation it provides. See the
`build.gradle` file in the `sql-backed-persistence` module to see how that
dependency is defined.

### @Bean and @Import for building a Spring context without wide package scanning

The `core` module has no Spring dependency, so the use case classes are not
annotated with @Component. Instead, the `api` module calls out the components
it wants. See the `Application` class in the `api` module.

### "Microcosm" Spring context for tests requiring Spring magic

Sometimes you have a test that needs to stand up a Spring context to make use
of some Spring magic. However, this might be a unit test that doesn't need (and
perhaps shouldn't know about) the whole application's context.

In this project, we have a `SqlBackedEventRepositoryTest` which needs Spring to
implement a JPA repository. See that test for a way to create a tiny,
test-specific Spring context.

### Interface for configuration values

Spring has all kinds of mechanisms for accessing configuration values from the
environment. Unfortunately, it can get unwieldy keeping track of what they all
are, making sure you haven't missed any, and remembering the right techniques
to make them available for a test.

Having a dedicated interface that defines what values the application expects
to find in the environment can help with this. See the `ConfigValues` interface
in the `api` module, and how test configuration is provided in the
`LocalFeatureTest`.

### Abstract feature tests

If you're careful, you can design the feature tests for a web application to
run equally well against an app instance running locally as part of the test,
as well as a deployed instance in an actual environment (like acceptance). This
lets you run your test suite against the application after each deployment to
ensure it's working well.

See the `FeatureTest` abstract class in the `api` module, along with the
`LocalFeatureTest` and `AcceptanceFeatureTest` implementations.
