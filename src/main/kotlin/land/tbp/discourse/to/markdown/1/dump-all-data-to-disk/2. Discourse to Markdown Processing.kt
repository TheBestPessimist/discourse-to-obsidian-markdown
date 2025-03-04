package land.tbp.discourse.to.markdown.`1`.`dump-all-data-to-disk`

import kotlinx.serialization.json.JsonElement
import land.tbp.discourse.to.markdown.CategoriesResponse
import land.tbp.discourse.to.markdown.CategoryTopicsResponse
import land.tbp.discourse.to.markdown.json
import java.nio.file.Files

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
