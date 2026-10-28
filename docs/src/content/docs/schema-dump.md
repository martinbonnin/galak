---
title: "Monitoring the generated schema"
---

When working with Kotlin code, it's easy to introduce unwanted changes to your GraphQL schema. Not every Kotlin construct needs to be part of your exported GraphQL API.

For this reason, Galak comes with built-in monitoring of your GraphQL schema. The Gradle plugin adds an `galakCheckSchema` task that is run automatically whenever you run `./gradlew check` (and therefore `./gradlew build`).

> If you are familiar with [Kotlin binary compatibility validator](https://github.com/Kotlin/binary-compatibility-validator), the concept is very similar.
 
For an example, if you add a `exposedByMistake()` function, running `./gradlew galakCheckSchema` fails with the below:

```
$ ./gradlew galakCheckSchema
> Task :galakCheckSchema FAILED

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':galakCheckSchema'.
> Galak schema check failed.
  --- /Users/mbonnin/git/galak/sample-ktor/graphql/schema.graphqls
  +++ /Users/mbonnin/git/galak/sample-ktor/build/generated/ksp/main/resources/serviceSchema.graphqls
  @@ -7,5 +7,7 @@
      Greeting for name
     """
     hello(name: String!): String!
  +
  +  exposedByMistake: String!
   }
   
  
  Run 'galakDumpSchema' to overwrite the schema.
```

If the change is unwanted, then you can fix your code to hide it.

If the change is in fact desired, you can run `./gradlew galakDumpSchema` and commit the resulted file to make the check pass:

```
$ ./gradlew galakDumpSchema        
BUILD SUCCESSFUL in 522ms
$ git diff graphql/
diff --git a/sample-ktor/graphql/schema.graphqls b/sample-ktor/graphql/schema.graphqls
index 6f4c510..b7f7311 100644
--- a/sample-ktor/graphql/schema.graphqls
+++ b/sample-ktor/graphql/schema.graphqls
@@ -7,4 +7,6 @@ type Query {
    Greeting for name
   """
   hello(name: String!): String!
+
+  newField: String!
 }
$ git commit -a -m 'Update GraphQL schema'
$ ./gradlew galakCheckSchema        
BUILD SUCCESSFUL in 324ms
```
