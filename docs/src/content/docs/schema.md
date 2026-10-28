---
title: "Generating a schema"
---

Galak uses KSP to parse your Kotlin code and generate a matching GraphQL schema. 

To identify a graph, the Kotlin code must contain exactly one `@GraphQLQuery` class and at most one optional  `@GraphQLMutation` and `@GraphQLSubscription` classes:

```kotlin
// Mandatory top-level Query
@GraphQLQuery
class Query {
  fun userById(id: String): User { }
  // ...
}

// Optional top-level Mutation
@GraphQLMutation
class Mutation { }

// Optional top-level Subscription
@GraphQLSubscription
class Subscription { }
```

From those root classes, the Galak processor traverses the Kotlin class graph and builds the matching GraphQL schema:
* Classes used in output positions are mapped to objects, unions and interfaces.
* Classes used in input positions are mapped to input objects.
* Scalars and Enum can happen in both input and output position.

Whenever possible, the order of fields in the generated GraphQL schema is the same as the declaration order in Kotlin.

> Non-root classes do not need to be annotated and are added to the GraphQL schema automatically.

## Objects and fields

Galak maps public Kotlin classes to GraphQL types and public functions and properties to GraphQL fields with the same name:

```kotlin title="Kotlin"
class User(val id: String) {
    fun email(): String
}
```

```graphql title="GraphQL"
type User {
    id: String!
    email: String!
}
```

Private/internal fields and functions are not exposed in GraphQL:

```kotlin title="Kotlin"
class User {
    fun email(): String
    internal fun sendVerificationEmail()
}
```

```graphql title="GraphQL"
type User {
    email: String!
}
```

## Unions and interfaces

Non-empty Kotlin interfaces are mapped to GraphQL interfaces:

```kotlin title="Kotlin"
sealed interface Node {
    id: String
}
class User(
    override val id: String
) : Node
```

```graphql title="GraphQL"
interface Node {
    id: String!
}
type User implements Node {
    id: String!
}
```

Empty Kotlin interfaces are mapped to GraphQL unions:

```kotlin title="Kotlin"
sealed interface Actor 
class User(
    val id: String,
    val email: String
) : Actor
class Organisation(
    val users: List&lt;User&gt;
) : Actor
```

```graphql title="GraphQL"
union Actor = User | Organisation
type User {
    val id: String!
    val email: String!
}
type Organisation {
    val users: [User]
}
```

:::note
All interfaces, empty or not must be sealed for KSP to be able to traverse the possible implementations.
:::

## Enums

Kotlin enums are mapped to GraphQL enums:

```kotlin title="Kotlin"
enum class Role {
    Read,
    Write,
    Admin
}
```

```graphql title="GraphQL"
enum Role {
    Read,
    Write,
    Admin
}
```

## Scalars

Classes annotated with `@GraphQLScalar` are mapped to GraphQL scalars:

```kotlin title="Kotlin"
@GraphQLScalar(GeoPointCoercing::class)
class GeoPoint(
    val latitude: Double, 
    val longitude: Double
)
```

```graphql title="GraphQL"
scalar GeoPoint
```

You can also map existing classes that you don't own to GraphQL scalars using `typealias`

```kotlin title="Kotlin"
import kotlinx.datetime.LocalDateTime

@GraphQLScalar(DateTimeCoercing::class)
typealias DateTime = LocalDateTime
```

```graphql title="GraphQL"
scalar DateTime
```

:::note
Scalars require defining an associated Coercing. You can read more in the dedicated [scalars page](/galak/scalars/).
:::

## Arguments

Kotlin parameters are mapped to GraphQL arguments:

```kotlin title="Kotlin"
class Organisation {
    fun users(
        first: Int, 
        after: String
    ): UserConnection
}
```

```graphql title="GraphQL"
type Organisation {
    users(
        first: Int!,
        after: String!
    ): UserConnection!
}
```

Use `Optional<>` anf `@GraphQLDefault` to further control your input values. 

Note that because nullability vs optionality are so closely related in GraphQL, not all combination are allowed:

```
type Query { 
  // A non-null argument
  fun fieldA(arg: Int) = 0
  
  // Disallowed: argument type is nullable and doesn't have a default value: it must also be optional.
  //fun fieldB(arg: Int?) = 0 
  
  // An nullable argument, the client may omit it in which case the function body must handle `Absent`.
  fun fieldC(arg: Optional<Int?>) = 0
  
  // Disallowed: argument type is not nullable and cannot be optional
  //fun fieldD(arg: Optional<Int>) = 0
  
  // An non-null argument, if the client omits it, the default value will be used.
  fun fieldE(@GraphQLDefault("10") arg: Int) = 0

  // An nullable argument, if the client omits it, the default value will be used.
  fun fieldF(@GraphQLDefault("10") arg: Int?) = 0

  // Disallowed: there is a default value and the argument type cannot be optional
  //fun fieldG(@GraphQLDefault("10") arg: Optional<Int?>) = 0 

  // Disallowed: there is a default value and the argument type cannot be optional
  //fun fieldH(@GraphQLDefault("10") arg: Optional<Int>) = 0
}
```

Kotlin parameters may be of class type in which case the class is generated as a GraphQL input object:

```kotlin title="Kotlin"
class UserFilter(
    val role: Role?, 
    val organisationId: String?
)
class Query {
    fun users(
        where: UserFilter, 
    ): List&lt;User&gt;
}
```

```graphql title="GraphQL"
input UserFilter {
    role: Role,
    organisationId: String
}
type Query {
    users(
        where: UserFilter!,
    ): [User!]
}
```

## Names
The GraphQL name can be customized using `@GraphQLName`:

```kotlin title="Kotlin"
@GraphQLName("User")
class DomainUser {
    @GraphQLName("email")
    fun emailAddress(): String
}
```

```graphql title="GraphQL"
type User {
    email: String
}
```

## Descriptions
The GraphQL description is generated from the KDoc:

```kotlin title="Kotlin"
/**
 * A logged-in user of the service.
 */
class User {
    /**
     * The email address of the user.
     */
    fun email(): String
}
```

```graphql title="GraphQL"
"""
A logged-in user of the service.
"""
type User {
    """
    The email address of the user.
    """
    email: String
}
```

## Nullability

Nullable fields and input fields are also nullable in GraphQL:

```kotlin title="Kotlin"
class User {
    fun email(): String?
    fun id(): String
}
```

```graphql title="GraphQL"
type User {
    email: String
    id: String!
}
```

> Note how the logic is inverted. In GraphQL, fields are nullable by default.

## Directives

Define custom directives using `@GraphQLDirective`:

```kotlin title="Kotlin"
/**
 * A field requires a specific opt-in
 */
@GraphQLDirective
annotation class requiresOptIn(val feature: String)
```

```graphql title="GraphQL"
"""
A field requires a specific opt-in
"""
directive @requiresOptIn(feature: String!) on FIELD_DEFINITION
```

:::note
The directive locations and repeatable values are inferred automatically from the usages.
:::

Use your directive by annotating your Kotlin code:

```kotlin title="Kotlin"
class Query {
    @requiresOptIn(feature = "experimental")
    fun experimentalField(): String { 
        TODO() 
    }
}
```

```graphql title="GraphQL"
type Query {
    experimentalField: String! @requiresOptIn(feature: "experimental")
}
```

The order of the directives in the GraphQL schema is the same as the order of the annotations in the Kotlin code.

Kotlin annotation classes are mapped to input objects when used as parameter types:

```kotlin title="Kotlin"
enum class OptInLevel {
    Ignore,
    Warning,
    Error
}

annotation class OptInFeature(
    val name: String,
    val level: OptInLevel
)

@GraphQLDirective
annotation class requiresOptIn(val feature: OptInFeature)
```

```graphql title="GraphQL"
enum OptInLevel {
    Ignore,
    Warning,
    Error
}
input OptInFeature {
    name: String!,
    level: OptInLevel!
}
directive @requiresOptIn(feature: OptInFeature!) on FIELD_DEFINITION
```

Limitations:
1. It is impossible to reuse other input objects in directive arguments. The directive input objects can only be used in directives. This is because [Kotlin annotations do not support regular class paramaters, only annotation classes](https://kotlinlang.org/docs/annotations.html#constructors) unlike regular function parameters.
2. Directive arguments cannot have default values. This is because KSP does not support reading Kotlin default values and using `@GraphQLDefault` only without a default value would not compile.

## Deprecation

Kotlin symbols annotated as `@Deprecated` are marked deprecated in GraphQL when applicable:

```kotlin title="Kotlin"
class User {
    fun role(): Role
    @Deprecated("Check for `role == Admin` instead")
    fun isAdmin(): Boolean
}
```

```graphql title="GraphQL"
type User {
    isAdmin: String @deprecated("Check for `role == Admin` instead")
}
```

:::note
As of 2024, field, input fields, arguments and enum values support deprecation in GraphQL. Other locations, and especially objects and input objects cannot be deprecated. See [graphql-spec#550](https://github.com/graphql/graphql-spec/issues/550) for more details.
:::

## Default values

[KSP cannot read default parameter values](https://github.com/google/ksp/issues/642).

In order to define a default value for your arguments, use `@GraphQLDefault` and pass the value [encoded as GraphQL](https://spec.graphql.org/draft/#Value):

```kotlin title="Kotlin"
class Organisation {
    fun users(
        @GraphQLDefault("100") first: Int, 
        @GraphQLDefault("null") after: String?
    ): UserConnection
}
```

```graphql title="GraphQL"
type Organisation {
    users(
        first: Int! = 100,
        after: String = null
    ): UserConnection!
}
```

:::note
Default values are not supported in directives
:::


## Built-in types

Kotlin built-in types map to their GraphQL equivalent:

| Kotlin  | GraphQL |
|---------|---------|
| Int     | Int     |
| String  | String  |
| Double  | Float   |
| Boolean | Boolean |
| List    | List    |
