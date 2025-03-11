import java.util.regex.Pattern

// This pattern matches:
// - Starting with ![
// - Followed by any characters (non-greedy) until ]
// - Followed by (upload:// and then any characters until the first )
private val UPLOAD_PATTERN = Pattern.compile(
    """!\[([^]]*?)]\(upload://([^)\s]+)\)""",
    Pattern.MULTILINE or Pattern.DOTALL
)

/**
 * Finds all upload patterns in the given text that match the format:
 * ![any text](upload://any_text_without_spaces)
 *
 * @param text The input text to search for patterns
 * @return List of UploadMatch objects containing the full match, alt text, and upload ID
 */
fun findUploadPatterns(text: String): List<UploadMatch> {
    val matches = mutableListOf<UploadMatch>()
    val matcher = UPLOAD_PATTERN.matcher(text)

    while (matcher.find()) {
        matches.add(
            UploadMatch(
                fullMatch = matcher.group(0),
                altText = matcher.group(1),
                uploadId = matcher.group(2)
            )
        )
    }

    return matches
}

data class UploadMatch(
    val fullMatch: String,
    val altText: String,
    val uploadId: String,
) {
    val fileName: String
        get() {
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

}
