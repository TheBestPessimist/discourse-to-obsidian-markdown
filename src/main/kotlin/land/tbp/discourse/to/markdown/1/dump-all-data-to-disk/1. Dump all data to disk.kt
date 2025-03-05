package land.tbp.discourse.to.markdown.`1`.`dump-all-data-to-disk`

import com.sksamuel.hoplite.Constants.indent
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import land.tbp.discourse.to.markdown.CategoriesResponse
import land.tbp.discourse.to.markdown.Dump
import land.tbp.discourse.to.markdown.client
import land.tbp.discourse.to.markdown.discourseRequest
import land.tbp.discourse.to.markdown.json
import java.nio.file.Files
import kotlin.io.path.Path


/*
1. Dump all categories
 */
fun main() {
    `1 dumpAllCategories`()
    `2 dumpAllCategoryTopics`()
    `3 dumpAllTopicInfo`()
    `4 dumpAllTopicPosts`()
}

fun `1 dumpAllCategories`() = runBlocking {
    println("1 dump all Categories")
    if (Files.exists(Dump.Categories)) {
        println("File already exists")
        return@runBlocking
    }

    val categoriesResponse = discourseRequest(client, "categories").bodyAsText()
    Files.writeString(Dump.Categories, categoriesResponse)
    println(Files.readString(Dump.Categories))
}

fun `2 dumpAllCategoryTopics`() = runBlocking {
    println("2 dump all CategoryTopics")
    if (Files.exists(Dump.CategoryTopics)) {
        println("File already exists")
        return@runBlocking
    }
    val categories = json.decodeFromString<CategoriesResponse>(Files.readString(Dump.Categories))
    val map = categories.categoryList.categories.associate { category ->

        val categoryTopics = getCategoryTopics("c", category.slug, category.id.toString())
        category.id to categoryTopics
    }
    val json = json.encodeToString(map)
    Files.writeString(Dump.CategoryTopics, json)
    println(Files.readString(Dump.CategoryTopics))
}

fun `3 dumpAllTopicInfo`() = runBlocking {
    println("3 dump all TopicInfo")
    if (Files.exists(Dump.TopicInfo)) {
        println("File already exists")
        return@runBlocking
    }

    val categoryTopics = json.decodeFromString<Map<Int, JsonArray>>(Files.readString(Dump.CategoryTopics))
    val allTopics = categoryTopics.values.flatten().mapNotNull { jsonElement: JsonElement ->
        val topicId = jsonElement.jsonObject["id"]?.jsonPrimitive?.int
        topicId
    }
        .sorted()

    val map = allTopics.associate { topicId ->
        val jsons = discourseRequest(client, "t", topicId.toString()).bodyAsText()
        println("Topic: $topicId")
        topicId to json.parseToJsonElement(jsons)
    }
    val jsons = json.encodeToString(map)
    Files.writeString(Dump.TopicInfo, jsons)
    println(Files.readString(Dump.TopicInfo))
}

fun `4 dumpAllTopicPosts`() = runBlocking {
    println("4 dump all TopicPosts")
    if (Files.exists(Dump.TopicPosts)) {
        println("File already exists")
        return@runBlocking
    }

    val topicInfo = json.decodeFromString<Map<Int, JsonElement>>(Files.readString(Dump.TopicInfo))
    val topicPostsIds = topicInfo.asIterable()
        .map { ti ->
            val topicId = ti.key
            val posts = ti.value.jsonObject["post_stream"]?.jsonObject["stream"]?.jsonArray?.map { it.jsonPrimitive.int } ?: emptyList()

            topicId to posts
        }

    val s = topicPostsIds.map { it.second }.flatten().sorted()
    println("total posts: " + topicPostsIds.sumOf { it.second.count() })
    println(s.count())
    println("flatten: ${s.toSet().count()}")

    val topicPosts = topicPostsIds.associate { tpi ->
        val topicId = tpi.first
        val postIds = tpi.second

        println("Topic: $topicId")
        val posts = postIds.map {
            println("   Post: $it")
            val jsons = discourseRequest(client, "posts", it.toString()).bodyAsText()
            json.parseToJsonElement(jsons)
        }
        topicId to posts
    }

    val jsons = json.encodeToString(topicPosts)
    Files.writeString(Dump.TopicPosts, jsons)
    println(Files.readString(Dump.TopicPosts))
}


suspend fun getCategoryTopics(vararg urls: String): JsonArray {
    if (urls.isEmpty()) return buildJsonArray { }

    println(indent + urls.joinToString())

    val jsons = discourseRequest(client, *urls).bodyAsText()
    val topicList = json.parseToJsonElement(jsons).jsonObject["topic_list"]!!
    val topics = topicList.jsonObject["topics"]!!.jsonArray

    val moreTopicsUrl = topicList.jsonObject["more_topics_url"]?.jsonPrimitive?.contentOrNull

    return buildJsonArray {
        addAll(topics)
        if (moreTopicsUrl != null) {
            addAll(getCategoryTopics(moreTopicsUrl))
        }
    }
}
