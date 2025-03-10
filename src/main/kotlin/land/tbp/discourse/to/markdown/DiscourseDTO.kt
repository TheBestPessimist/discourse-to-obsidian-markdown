package land.tbp.discourse.to.markdown

import kotlinx.serialization.Serializable
import java.time.Instant

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
//    val topicCount: Int, // this field is sometimes 0, even though there are topics
//    val postCount: Int, // this field is sometimes 0, even though there are posts
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
data class TopicInfo(
    val id: Int,
    val slug: String,
    val title: String,
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant,
)
