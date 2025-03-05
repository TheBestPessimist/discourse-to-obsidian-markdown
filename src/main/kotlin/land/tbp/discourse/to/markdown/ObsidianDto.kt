package land.tbp.discourse.to.markdown

import java.time.Instant

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
