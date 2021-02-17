package de.tiendonam.prscraper.utils

data class ExportRow(val docId: Long, val docText: String, val label: String?, val note: String?)

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

    fun exportCSV(data: List<ExportRow>, destination: String, preprocessing: Boolean = false) {
        CsvUtils.writeFile(destination) { printer ->
            printer.printRecord("doc_id", "doc_text", "label", "note")
            data.forEach { row ->
                if (!preprocessing || filter(row.docText)) {
                    val text = if (preprocessing) preprocessComment(row.docText) else row.docText
                    printer.printRecord(row.docId, text, row.label, row.note)
                }
            }
        }
    }

    /**
     * @param comment the comment to be preprocessed
     * @return the preprocessed comment
     */
    private fun preprocessComment(comment: String): String {
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
    private fun filter(comment: String): Boolean {
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