package com.takaapoo.weatherer.data.local

import java.io.InputStream

object CsvParser {
    fun parseCsv(inputStream: InputStream, hasHeader: Boolean = true): List<List<String>> {
        return inputStream.bufferedReader().useLines { lines ->
            lines.map { line ->
                // Simple CSV parsing (doesn't handle quoted commas)
                line.split(",").map { it.trim() }
            }.toList()
        }.let { data ->
            if (hasHeader) data.drop(1) else data
        }
    }

    // Alternative: parse with quoted values support
    fun parseCsvWithQuotes(inputStream: InputStream, hasHeader: Boolean = true): List<List<String>> {
        val regex = """"([^"]*)"|([^,]+)""".toRegex()
        return inputStream.bufferedReader().useLines { lines ->
            lines.map { line ->
                regex.findAll(line).map { match ->
                    (match.groups[1]?.value ?: match.groups[2]?.value ?: "").trim()
                }.toList()
            }.toList()
        }.let { data ->
            if (hasHeader) data.drop(1) else data
        }
    }
}