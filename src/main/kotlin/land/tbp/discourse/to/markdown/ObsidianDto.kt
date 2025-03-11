package land.tbp.discourse.to.markdown

import kotlinx.serialization.Serializable
import java.time.Instant

// This is the thing i will write in a file
@Serializable
data class Topic(
    val categoryName: String,
    val tags: List<String>,
    val title: String,
    val slug: String,
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant = Instant.now(),
    val posts: List<Post>,
    val fullUrls: List<String>,
)


@Serializable
data class Post(
    val rawMarkdown: String,
    val id: Int,
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant = Instant.now(),
)
