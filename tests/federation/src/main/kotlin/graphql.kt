import galak.annotation.GraphQLQuery
import galak.subgraph.GraphQLKey

@GraphQLQuery
class Query {
  fun products(): List<Product> {
    return products
  }
}

class Product(
  @GraphQLKey
  val id: String,
  val name: String
) {
  companion object {
    suspend fun resolve(id: String): Product {
      return products.first { it.id == id }
    }
  }
}

val products = listOf(
  Product("1", "foo"),
  Product("2", "bar")
)