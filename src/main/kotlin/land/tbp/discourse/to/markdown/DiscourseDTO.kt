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
    val id: Int,
    val slug: String,
    val name: String,
    val topicCount: Int,
    val postCount: Int,
)

///////////////////////

@Serializable
data class CategoryTopicsResponse(val topicList: TopicList)

@Serializable
data class TopicList(
    val moreTopicsUrl: String?,
    val topics: List<TopicInfo>,
)
@Serializable
data class TopicInfo (
    val id: Int,
    val slug: String,
    val title: String,
)
