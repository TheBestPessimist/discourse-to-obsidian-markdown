package land.tbp.discourse.to.markdown.`1`.`dump-all-the-data-to-disk`

import com.sksamuel.hoplite.Constants.indent
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import land.tbp.discourse.to.markdown.CategoriesResponse
import land.tbp.discourse.to.markdown.client
import land.tbp.discourse.to.markdown.discourseRequest
import land.tbp.discourse.to.markdown.objectMapper
import java.nio.file.Files
import kotlin.io.path.Path

// todo this object should have a better location
object Dump {
    val Categories = Path("datadump/Categories.json")
    val CategoryTopics = Path("datadump/CategoryTopics.json")
    val TopicInfo = Path("datadump/TopicInfo.json")
    val TopicPosts = Path("datadump/TopicPosts.json")
}


/*
1. Dump all categories
 */

fun main() {
    `1 dumpAllCategories`()
    `2 dumpAllCategoryTopics`()
    `3 dumpAllTopicInfo`()
    `4 dumpAllTopicPosts`()

    runBlocking {


//        for (category in categoriesResponse.categoryList.categories) {
////            if (category.name!="Blog")
////            if (category.name!="Discourse Testing")
////            if (category.name!="Site Feedback/Meta")
//            if (category.name!="Staff")
////            if (category.name!="Lounge")
////            if (category.name!="A Shadow of the Day...")
////            if (category.name!="The Mighty Nahsuc's Song Of The Day")
////            if (category.name!="pokambrian category")
//                continue
//            launch {
//                println(category)
//                val topicInfos = getTopicInfos(client, "c", category.slug, category.id.toString())
//                topicInfos.forEach { topicInfo ->
//                    launch {
//                        println(indent.repeat(1) + topicInfo)
//                        val topicPostInfos = getTopicPostInfos(client, "t", topicInfo.id.toString())
//                        topicCount.incrementAndGet()
//
//                        val posts = coroutineScope {
//                            topicPostInfos.postStream.stream.map { postId ->
//                                async {
//                                    val rawPost = discourseRequest(client, "posts", postId.toString()).body<PostResponse>().raw
//                                    Post(rawPost, postId)
//                                }
//                            }.awaitAll()
//                        }
//                        val topic = Topic(category.name, emptyList(), topicInfo.title, topicInfo.slug, topicInfo.createdAt, posts.sortedBy { it.id }, emptyList())
//                        allTopics.add(topic)
//                    }
//                }
//            }
//        }
    }
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
    val categories = objectMapper.decodeFromString<CategoriesResponse>(Files.readString(Dump.Categories))
    val map = categories.categoryList.categories.associate { category ->

        val categoryTopics = getCategoryTopics("c", category.slug, category.id.toString())
        category.id to categoryTopics
    }
    val json = objectMapper.encodeToString(map)
    Files.writeString(Dump.CategoryTopics, json)
    println(Files.readString(Dump.CategoryTopics))
}

fun `3 dumpAllTopicInfo`() = runBlocking {
    println("3 dump all TopicInfo")
    if (Files.exists(Dump.TopicInfo)) {
        println("File already exists")
        return@runBlocking
    }

    val categoryTopics = objectMapper.decodeFromString<Map<Int, JsonArray>>(Files.readString(Dump.CategoryTopics))
    val allTopics = categoryTopics.values.flatten().mapNotNull { jsonElement: JsonElement ->
        val topicId = jsonElement.jsonObject["id"]?.jsonPrimitive?.int
        topicId
    }
        .sorted()

    val map = allTopics.associate { topicId ->
        val json = discourseRequest(client, "t", topicId.toString()).bodyAsText()
        println("Topic: $topicId")
        topicId to objectMapper.parseToJsonElement(json)
    }
    val json = objectMapper.encodeToString(map)
    Files.writeString(Dump.TopicInfo, json)
    println(Files.readString(Dump.TopicInfo))
}

fun `4 dumpAllTopicPosts`() = runBlocking {
    println("4 dump all TopicPosts")
    if (Files.exists(Dump.TopicPosts)) {
        println("File already exists")
        return@runBlocking
    }

    val topicInfo = objectMapper.decodeFromString<Map<Int, JsonElement>>(Files.readString(Dump.TopicInfo))
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
            val json = discourseRequest(client, "posts", it.toString()).bodyAsText()
            objectMapper.parseToJsonElement(json)
        }
        topicId to posts
    }

    val json = objectMapper.encodeToString(topicPosts)
    Files.writeString(Dump.TopicPosts, json)
    println(Files.readString(Dump.TopicPosts))
}


suspend fun getCategoryTopics(vararg urls: String): JsonArray {
    if (urls.isEmpty()) return buildJsonArray { }

    println(indent + urls.joinToString())

    val json = discourseRequest(client, *urls).bodyAsText()
    val topicList = objectMapper.parseToJsonElement(json).jsonObject["topic_list"]!!
    val topics = topicList.jsonObject["topics"]!!.jsonArray

    val moreTopicsUrl = topicList.jsonObject["more_topics_url"]?.jsonPrimitive?.contentOrNull

    return buildJsonArray {
        addAll(topics)
        if (moreTopicsUrl != null) {
            addAll(getCategoryTopics(moreTopicsUrl))
        }
    }
}
