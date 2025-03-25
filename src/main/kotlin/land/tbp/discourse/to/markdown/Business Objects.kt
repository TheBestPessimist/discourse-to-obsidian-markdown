package land.tbp.discourse.to.markdown

import kotlinx.serialization.Serializable
import land.tbp.discourse.to.markdown.processing.base62Decode
import java.time.Instant

/**
 * This is the thing I will write in a markdown file
 */
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
    val uploadUrls: List<UploadUrl>,
) {
    /**
     * This is the field where i replace the discourse URLs with the markdown ones.
     */
    val markdownWithImages: String by lazy {
        for (upload in uploadUrls) {
            rawMarkdown.replace(upload.fullMatch, upload.uploadIdNameOnDisk)
        }
        ""
    }
}

@Serializable
data class UploadUrl(
    val fullMatch: String,
    val altText: String,
    val uploadId: String,
) {
    /**
     * The name of the uploaded file, or the custom name that the user entered.
     *
     * For example, it's `the fancy name` in `![[image.jpg|the fancy name]]`
     */
    val fileName: String = createFileName()

    /**
     * The file on disk, unhashed from Discourse's base62 hash schema
     */
    val uploadIdNameOnDisk: String = computeUploadIdNameOnDisk()
}

private fun UploadUrl.computeUploadIdNameOnDisk(): String {
    // Get the extension from [uploadId]
    val extension = when {
        uploadId.endsWith(".") -> "" // Some uploadIds end with . and have no extension
        uploadId.contains(".") -> uploadId.substringAfterLast(".")
        else -> "" // No extension found
    }

    val hash = when {
        uploadId.endsWith(".") -> uploadId // Some uploadIds end with . and have no extension
        uploadId.contains(".") -> uploadId.substringBeforeLast(".")
        else -> uploadId // No extension found
    }

    val uploadHashDecoded = base62Decode(hash)
    return "$uploadHashDecoded.$extension"
}

private fun UploadUrl.createFileName(): String {
    // Get the extension from [uploadId]
    val extension = when {
        uploadId.endsWith(".") -> "" // Some uploadIds end with . and have no extension
        uploadId.contains(".") -> uploadId.substringAfterLast(".")
        else -> "" // No extension found
    }

    // Extract the base filename from [altText]
    val baseFileName = when {
        // If altText already contains a filename with extension, use it without the extension
        altText.contains(".") && !altText.contains("|") ->
            altText.substringBeforeLast(".")

        // Handle cases with dimensions (e.g., "image|690x373" or "my file|123x456")
        altText.contains("|") ->
            altText.substringBefore("|").trim()

        // If it's just a plain text without extension or dimensions, use it as is
        else -> altText.trim()
    }

    // Combine filename and extension
    return if (extension.isNotEmpty()) "$baseFileName.$extension" else baseFileName
}
