package de.tiendonam.prscraper.utils

private val regexCodeBlock = Regex("```.*?```")
private val regexCodeLine = Regex("`.*?`")
private val regexBreak = Regex("[\\t\\n\\r]+")
private val regexUrlWithLabel = Regex("\\[([^]]+)]\\(([^)]+)\\)")
private val regexUrl = Regex("http\\S+")
private val regexMention = Regex("@\\S+")
private val regexReference = Regex("#\\d+")
private val regexNumber = Regex(" [+-]?\\d+[.,]?\\d*")
private val regexQuoted = Regex("\".*?\"")
private val regexBrackets = Regex("\\(([^)]+)\\)")
private val regexMultipleSpaces = Regex("  +")

data class ExportRow(val docId: Long, val docText: String, val label: String?, val note: String?, val prOwner: Boolean) {

    // preprocessed docText
    val docTextNormalized by lazy {
        docText
            .replace(regexBreak, " ")
            .replace(regexCodeBlock, " [codeblock] ")
            .replace(regexCodeLine, " [codeline] ")
            .replace(regexUrlWithLabel, "$1 [url]")
            .replace(regexUrl, " [url] ")
            .replace(regexMention, " [user] ")
            .replace(regexReference, " [ref] ")
            .replace(regexNumber, " [number] ")
            .replace(regexQuoted, " [quoted] ")
            .replace(regexBrackets, " ( $1 ) ")
            .replace(",", " ")
            .replace(".", " ")
            .trim()
            .replace(regexMultipleSpaces, " ")
            .toLowerCase()
    }
}

object ExportUtils {

    fun exportCSV(data: List<ExportRow>, destination: String, preprocessing: Boolean = false) {
        val finalData = if (preprocessing) data.distinctBy { row -> row.docTextNormalized } else data

        CsvUtils.writeFile(destination) { printer ->
            printer.printRecord("doc_id", "doc_text", "label", "note")
            finalData.forEach { row ->
                if (!preprocessing || filterComment(row)) {
                    val text = if (preprocessing) row.docTextNormalized else row.docText
                    printer.printRecord(row.docId, text, row.label, row.note)
                }
            }
        }
    }

    /**
     * @param comment the comment (raw) to be filtered
     * @return true if this comment should be included, false otherwise
     */
    private fun filterComment(comment: ExportRow): Boolean {
        return !comment.prOwner // ignore replies from PR issuer
                && !comment.docText.startsWith("pinging", ignoreCase = true) // ignore ping command
                && !comment.docText.startsWith("jenkins", ignoreCase = true) // ignore jenkins command
                && !comment.docText.startsWith("@elasticmachine") // ignore elasticmachine command
                && !comment.docText.startsWith("run ", ignoreCase = true) // ignore run command
                && !comment.docText.startsWith("s/") // ignore spelling correction
                && !comment.docText.startsWith("<!-- CLA-CHECK") // ignore CLA notices
                && !comment.docText.startsWith("###") // ignore backports
                && !comment.docText.startsWith(">") // ignore replies
    }
}