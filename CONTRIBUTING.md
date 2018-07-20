<!--- Copyright (C) 2016-2018 Lightbend Inc. <https://www.lightbend.com> -->
# Lagom Recipes contributor guidelines

## General guidelines

This project follows the [Lagom contributor guidelines](https://github.com/lagom/lagom/blob/master/CONTRIBUTING.md). Please read and follow those when contributing Lagom Recipes.

## Additional guidelines

In addition to the overall Lagom contributor guidelines, there are additional guidelines that are specific to Lagom Recipes.

- Start each recipe using one of the standard starting templates for sbt or Maven:
  - `mvn archetype:generate -DarchetypeGroupId=com.lightbend.lagom -DarchetypeArtifactId=maven-archetype-lagom-java   - `sbt new lagom/lagom-java.g8`
  - `sbt new lagom/lagom-scala.g8`
-DarchetypeVersion=RELEASE`
- Create a top-level directory for new recipe types, and then name each project within that directory using the pattern appropriate to the style of recipe:
  - `<recipe-type>/<recipe-type>-java-mvn`
  - `<recipe-type>/<recipe-type>-java-sbt`
  - `<recipe-type>/<recipe-type>-scala-sbt`
- Adding multiple versions of each recipe is appreciated (Java Maven, Java sbt, and Scala) but not required. Porting to multiple languages and build systems is time-consuming, and it's better to have something than nothing at all. If you'd like to contribute, but don't have an idea for a new recipe, porting an existing one to fill in missing examples is a great place to start.
- Recipes are intended to each demonstrate a single concept in a simple way. Trim away any code that is not necessary to demonstrate the concept of your recipe. For example, if your recipe doesn't rely on persistence, the message broker API, or multiple services, then remove these parts from the starting template.
- Each recipe is usually built in two steps (git commits) where the first commit introduces an example project and the second commit introduces the minimum changes required to complete the example. Please commit the (trimmed down) starting template first, before adding the functionality you wish to demonstrate in a second commit. This makes it easier for readers to understand what you changed in the recipe.
- Each recipe should have a detailed `README.md` that gives an overview of the concept it demonstrates, implementation details with links to relevant source files, and instructions for running and testing the recipe.
- This project uses Travis as a smoke test environment to quickly spot regressions when bumping versions. When a regression occurs there's no commitment to keep the recipes up to date and instead the submitter may decide to rollback the version bump of the recipe and leave it at an old, but stable implementation.
- When adding a new recipe, be sure to add it to `.travis.yml` and the list of recipes in the top-level `README.md` file.
