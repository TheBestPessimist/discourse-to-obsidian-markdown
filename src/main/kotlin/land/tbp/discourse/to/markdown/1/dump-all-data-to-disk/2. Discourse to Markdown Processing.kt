package land.tbp.discourse.to.markdown.`1`.`dump-all-data-to-disk`

import kotlinx.serialization.json.JsonElement
import land.tbp.discourse.to.markdown.CategoriesResponse
import land.tbp.discourse.to.markdown.CategoryTopicsResponse
import land.tbp.discourse.to.markdown.Dump
import land.tbp.discourse.to.markdown.Topic
import land.tbp.discourse.to.markdown.json
import java.nio.file.Files
import java.util.Collections
import kotlin.io.path.Path

class DumpReader {
    fun readCategories(): CategoriesResponse {
        return json.decodeFromString(Files.readString(Dump.Categories))
    }

    fun readCategoryTopics(): Map<Int, CategoryTopicsResponse> {
        return json.decodeFromString(Files.readString(Dump.CategoryTopics))
    }

    fun readTopicInfo(): Map<Int, JsonElement> {
        return json.decodeFromString(Files.readString(Dump.TopicInfo))
    }

    fun readTopicPosts(): Map<Int, List<JsonElement>> {
        return json.decodeFromString(Files.readString(Dump.TopicPosts))
    }
}

fun main() {
    val allTopics: MutableList<Topic> = Collections.synchronizedList(mutableListOf<Topic>())

    val topic = Topic(category.name, emptyList(), topicInfo.title, topicInfo.slug, topicInfo.createdAt, posts.sortedBy { it.id }, emptyList())
allTopics.add(topic)

    println(allTopics.size)
    val t = allTopics.sortedWith(compareBy({ it.categoryName }, { it.title }))
    Files.writeString(Path("./zzzzzzzz.txt"), t.joinToString("\n".repeat(10)))
}
