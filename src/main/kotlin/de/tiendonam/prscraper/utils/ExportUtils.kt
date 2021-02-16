package de.tiendonam.prscraper.utils

object ExportUtils {

    private val regexCodeBlock = Regex("```.*?```")
    private val regexCodeLine = Regex("`.*?`")
    private val regexBreak = Regex("[\\t\\n\\r]+")
    private val regexUrlWithLabel = Regex("\\[([^]]+)]\\(([^)]+)\\)")
    private val regexUrl = Regex("http\\S+")
    private val regexMention = Regex("@\\S+")
    private val regexReference = Regex("#\\d+")
    private val regexNumber = Regex(" [+-]?\\d+[.,]?\\d*")
    private val regexQuoted = Regex("\".*?\"")

    /**
     * @param comment the comment to be preprocessed
     * @return the preprocessed comment
     */
    fun normalizeComment(comment: String): String {
        return comment.trim()
            .replace(regexBreak, " ")
            .replace(regexCodeBlock, " [codeblock] ")
            .replace(regexCodeLine, " [codeline] ")
            .replace(regexUrlWithLabel, "$1 [url]")
            .replace(regexUrl, " [url] ")
            .replace(regexMention, " [user] ")
            .replace(regexReference, " [ref] ")
            .replace(regexNumber, " [number] ")
            .replace(regexQuoted, " [quoted] ")
            .replace(",", " ")
            .replace(".", " ")
            .toLowerCase()
    }

    /**
     * @param comment the comment (raw) to be filtered
     * @return true if this comment should be included, false otherwise
     */
    fun filter(comment: String): Boolean {
        return !comment.startsWith("pinging", ignoreCase = true) // ignore ping command
                && !comment.startsWith("jenkins", ignoreCase = true) // ignore jenkins command
                && !comment.startsWith("@elasticmachine") // ignore elasticmachine command
                && !comment.startsWith("run ", ignoreCase = true) // ignore run command
                && !comment.startsWith("s/") // ignore spelling correction
                && !comment.startsWith("<!-- CLA-CHECK") // ignore CLA notices
                && !comment.startsWith("###") // ignore backports
                && !comment.startsWith(">") // ignore replies
    }
}