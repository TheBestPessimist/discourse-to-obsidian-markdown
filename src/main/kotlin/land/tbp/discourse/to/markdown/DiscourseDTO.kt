package land.tbp.discourse.to.markdown

import kotlinx.serialization.Serializable

@Serializable
data class CategoriesResponse(val categoryList: CategoryList)

@Serializable
data class CategoryList(val categories: List<Category>) {
    override fun toString(): String {
        return "CategoryList(categories=${categories.joinToString("\n")})"
    }
}

@Serializable
data class Category(
    val name: String,
    val slug: String,
    val topicCount: Int,
    val postCount: Int,
)
