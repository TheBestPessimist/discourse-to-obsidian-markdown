 import java.util.regex.Pattern

class ImagePatternDetector {
    companion object {
        // This pattern matches:
        // - Starting with ![
        // - Followed by any characters (non-greedy) until ]
        // - Followed by (upload:// and then any characters until the first )
        private val IMAGE_PATTERN = Pattern.compile(
            """!\[([^]]*?)]\(upload://([^)\s]+)\)""",
            Pattern.MULTILINE or Pattern.DOTALL
        )

        /**
         * Finds all image patterns in the given text that match the format:
         * ![any text](upload://any_text_without_spaces)
         *
         * @param text The input text to search for patterns
         * @return List of ImageMatch objects containing the full match, alt text, and upload ID
         */
        fun findImagePatterns(text: String): List<ImageMatch> {
            val matches = mutableListOf<ImageMatch>()
            val matcher = IMAGE_PATTERN.matcher(text)
            
            while (matcher.find()) {
                matches.add(
                    ImageMatch(
                        fullMatch = matcher.group(0),
                        altText = matcher.group(1),
                        uploadId = matcher.group(2)
                    )
                )
            }
            
            return matches
        }
    }

    data class ImageMatch(
        val fullMatch: String,
        val altText: String,
        val uploadId: String
    )
}
