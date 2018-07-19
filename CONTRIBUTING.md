<!--- Copyright (C) 2016-2018 Lightbend Inc. <https://www.lightbend.com> -->
# Lagom contributor guidelines

## Prerequisites

Before making a contribution, it is important to make sure that the change you wish to make and the approach you wish to take will likely be accepted, otherwise you may end up doing a lot of work for nothing.  If the change is only small, for example, if it's a documentation change or a simple bug fix, then it's likely to be accepted with no prior discussion.  

## Development tips

- Use sbt launcher 0.13.13 or later, which will automatically read JVM options from `.jvmopts`.
- This project uses Travis as a smoke test environment to quickly spot regressions when bumping versions. When a regression occurs there's no commitment to keep the recipes up to date and instead the submitter may decide to rollback the version bump of the recipe and leave it at an old, but stable implementation.

## Pull request procedure

1. Make sure you have signed the [Lightbend CLA](https://www.lightbend.com/contribute/cla); if not, sign it online.
2. Ensure that your contribution meets the following guidelines:
    1. Live up to the current code standard:
        - Not violate [DRY](http://programmer.97things.oreilly.com/wiki/index.php/Don%27t_Repeat_Yourself).
        - [Boy Scout Rule](http://programmer.97things.oreilly.com/wiki/index.php/The_Boy_Scout_Rule) needs to have been applied.
    2. Regardless of whether the code introduces new features or fixes bugs or regressions, it must have comprehensive tests.  This includes when modifying existing code that isn't tested.
    3. The recipes must be well documented using the GitHub flavour of markdown on each recipe README.md and include code comments inline.
    4. Implementation-wise, the following things should be avoided as much as possible:
        * Global state
        * Public mutable state
        * Implicit conversions
        * ThreadLocal
        * Locks
        * Casting
        * Introducing new, heavy external dependencies
    5. New recipes must:
        * Not use ``@author`` tags since it does not encourage [Collective Code Ownership](http://www.extremeprogramming.org/rules/collective.html).
    6. New recipes may:
        * Be added to `.travis.yml` which is used as a smoke test environment.
3. Submit a pull request.

If the pull request does not meet the above requirements then the code should **not** be merged into master, or even reviewed - regardless of how good or important it is. No exceptions.
