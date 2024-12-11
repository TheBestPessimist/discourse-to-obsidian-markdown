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

}


/*
1. Dump all categories
 */

fun main() {
    `1 dump all Categories`()
    `2 dump all CategoryTopics`()

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

fun `1 dump all Categories`() = runBlocking {
    println("1 dump all Categories")
    if (Files.exists(Dump.Categories))
        return@runBlocking

    val categoriesResponse = discourseRequest(client, "categories").bodyAsText()
    Files.writeString(Dump.Categories, categoriesResponse)
    println(Files.readString(Dump.Categories))
}

fun `2 dump all CategoryTopics`() = runBlocking {
    println("2 dump all CategoryTopics")
    val categories = objectMapper.decodeFromString<CategoriesResponse>(Files.readString(Dump.Categories))
    val map = categories.categoryList.categories.associate { category ->
//        if (category.name != "Staff")
//            return@map
//        if (category.slug != "a-shadow-of-the-day") {
//            return@associate category.id to buildJsonArray {  }
//        }

        val categoryTopics = getCategoryTopics("c", category.slug, category.id.toString())
        category.id to categoryTopics
    }
    val json = objectMapper.encodeToString(map)
    Files.writeString(Dump.CategoryTopics, json)
    println(Files.readString(Dump.CategoryTopics))
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
