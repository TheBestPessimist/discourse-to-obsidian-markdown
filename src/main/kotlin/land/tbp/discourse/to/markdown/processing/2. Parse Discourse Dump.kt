package land.tbp.discourse.to.markdown.processing

import kotlinx.serialization.json.*
import land.tbp.discourse.to.markdown.*
import java.nio.file.Files
import java.time.Instant

class DumpReader {
    companion object {
        val categories: CategoriesResponse by lazy { json.decodeFromString(Files.readString(Dump.Categories)) }

        // this file is useless for my purpose
        val categoryTopics: Map<Int, CategoryTopicsResponse> by lazy { json.decodeFromString(Files.readString(Dump.CategoryTopics)) }
        val topicInfo: Map<Int, JsonElement> by lazy { json.decodeFromString(Files.readString(Dump.TopicInfo)) }
        val topicPosts: Map<Int, List<JsonElement>> by lazy { json.decodeFromString(Files.readString(Dump.TopicPosts)) }
    }
}

fun main() {
    /*
    topic
    - has list of posts
    - belongs to category
    - has a list of tags
     */
    val topics = buildList {
        for ((id, jsonElem) in DumpReader.topicInfo) {
            if (id != 4692) continue

            val categoryId = jsonElem.jsonObject["category_id"]?.jsonPrimitive?.int
            val categoryName = DumpReader.categories.categoryList.categories.single { it.id == categoryId }.name
            val tags = jsonElem.jsonObject["tags"]!!.jsonArray.map { it.jsonPrimitive.content }
            val title = jsonElem.jsonObject["title"]!!.jsonPrimitive.content
            // fancy title is the url encoded version. not needed
            // val fancyTitle = jsonElem.jsonObject["fancy_title"]!!.jsonPrimitive.content

            val slug = jsonElem.jsonObject["slug"]!!.jsonPrimitive.content
            val fullUrls = listOf(
                "https://discourse.tbp.land/t/$id",
                "https://discourse.tbp.land/t/$slug/$id",
            )
            val createdAt = jsonElem.jsonObject["created_at"]!!.jsonPrimitive.content.let { Instant.parse(it) }

            // todo imageURL, thumbnails might be interesting fields???

            val posts = getPosts(id, jsonElem)
            val uploadUrls = getUploadUrls(posts)
            add(Topic(categoryName, tags, title, slug, createdAt, posts, fullUrls))
        }
    }

    // println(json.encodeToString(topics))


    // val allTopics: MutableList<Topic> = Collections.synchronizedList(mutableListOf<Topic>())
    //     val topic = Topic(category.name, emptyList(), topicInfo.title, topicInfo.slug, topicInfo.createdAt, posts.sortedBy { it.id }, emptyList())
    // allTopics.add(topic)
    //
    //     println(allTopics.size)
    //     val t = allTopics.sortedWith(compareBy({ it.categoryName }, { it.title }))
    //     Files.writeString(Path("./zzzzzzzz.txt"), t.joinToString("\n".repeat(10)))
}

private fun getUploadUrls(posts: List<Post>) {
    posts.map { (rawMarkdown, _, _) ->
        rawMarkdown.also { println(it); println(); println();println();println();println(); }
    }
}

private fun getPosts(id: Int, jsonElem: JsonElement): List<Post> {
    val postIds = jsonElem.jsonObject["post_stream"]!!.jsonObject["stream"]!!.jsonArray.map { it.jsonPrimitive.int }
    val topicPosts = DumpReader.topicPosts[id]!!
    val posts = postIds.map { postId ->
        val post = topicPosts.single { it.jsonObject["id"]!!.jsonPrimitive.int == postId }.jsonObject
        val raw = post["raw"]!!.jsonPrimitive.content
        val createdAt = post["created_at"]!!.jsonPrimitive.content.let { Instant.parse(it) }
        Post(
            raw,
            postId,
            createdAt
        )
    }
    return posts
}
