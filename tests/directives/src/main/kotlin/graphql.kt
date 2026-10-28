import galak.annotation.GraphQLDirective
import galak.annotation.GraphQLQuery

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

@GraphQLQuery
class Query {
  @requiresOptIn(OptInFeature("experimental", OptInLevel.Warning))
  fun experimentalField(): String {
    return "Hello"
  }
}