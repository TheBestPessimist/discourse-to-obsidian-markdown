package land.tbp.discourse.to.markdown.processing

import findUploadPatterns
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
            // if (id != 4692) continue

            val categoryId = jsonElem.jsonObject["category_id"]?.jsonPrimitive?.int
            val categoryName = DumpReader.categories.categoryList.categories.single { it.id == categoryId }.name
            val tags = jsonElem.jsonObject["tags"]!!.jsonArray.map { it.jsonPrimitive.content }
            val title = jsonElem.jsonObject["title"]!!.jsonPrimitive.content
            // fancy title is the url encoded version. not needed
            // val fancyTitle = jsonElem.jsonObject["fancy_title"]!!.jsonPrimitive.content

            val slug = jsonElem.jsonObject["slug"]!!.jsonPrimitive.content
            val fullUrls = listOf(
                "https://discourse.tbp.land/t/$id",
                "https://discourse.tbp.land/raw/$id",
                "https://discourse.tbp.land/t/$slug/$id",
            )
            val createdAt = jsonElem.jsonObject["created_at"]!!.jsonPrimitive.content.let { Instant.parse(it) }

            val posts = getPosts(id, jsonElem)
            add(Topic(categoryName, tags, title, slug, createdAt, posts, fullUrls))
        }
    }

    migrateFilesAndUploads(topics)
}

private fun getPosts(id: Int, jsonElem: JsonElement): List<Post> {
    val postIds = jsonElem.jsonObject["post_stream"]!!.jsonObject["stream"]!!.jsonArray.map { it.jsonPrimitive.int }
    val topicPosts = DumpReader.topicPosts[id]!!
    val posts = postIds.map { postId ->
        val post = topicPosts.single { it.jsonObject["id"]!!.jsonPrimitive.int == postId }.jsonObject
        val raw = post["raw"]!!.jsonPrimitive.content
        val createdAt = post["created_at"]!!.jsonPrimitive.content.let { Instant.parse(it) }
        val uploadUrls = findUploadPatterns(raw)

        Post(
            raw,
            postId,
            createdAt,
            uploadUrls
        )
    }
    return posts
}
