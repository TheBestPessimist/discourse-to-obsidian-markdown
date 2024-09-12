package land.tbp.discourse.to.markdown

import io.seruco.encoding.base62.Base62
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

//////////////////////

@Serializable
data class TopicPostsResponse(
    val postStream: PostStream,
)

@Serializable
data class PostStream(
    val stream: List<Int>,
)

/////

@Serializable
data class PostResponse(
    val raw: String,
)

// This is the thing i will write in a file
data class Topic(
    val categoryName: String,
    val tags: List<String>, // todo
    val title: String,
    val slug: String,
    val createdAt: Instant = Instant.now(),// todo
    val posts: List<Post>,
    // todo
    // should be something like:
    // https://discourse.tbp.land/t/all-tomorrows-a-billion-year-chronicle-of-the-myriad-species-and-varying-fortunes-of-man-nemo-ramjet/25
    // so that my linking history is not lost!
    // basically, i should make it such that all posts inside my discourse are available under the **same** address in the future
    val slugs: List<String>,
)

data class Post(
    val rawMarkdown: String,
    val id: Int,
    val createdAt: Instant = Instant.now(), // todo
)


@OptIn(ExperimentalStdlibApi::class)
fun main() {
    val b = Base62.createInstanceWithInvertedCharacterSet()
    val d = b.decode("6LX9Y8dW88pEjXU4ufdLkTrkhc0".encodeToByteArray())
    d.toHexString(HexFormat.Default).also { println(it) } // 37b7c679596e2d220a5a05d6ab8a7336e02f1817
}
