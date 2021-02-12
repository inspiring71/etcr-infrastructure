package de.tiendonam.prscraper.utils

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.FileWriter

object CsvUtils {

    /**
     * helper function to write csv
     * @param fileName name of csv file
     * @param action print records here
     */
    fun writeFile(fileName: String, action: (CSVPrinter) -> Unit) {
        val file = FileWriter(fileName)
        val printer = CSVPrinter(file, CSVFormat.DEFAULT)
        action(printer)
        printer.close()
    }
}