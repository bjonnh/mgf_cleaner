/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2021 Jonathan Bisson
 *
 */

package net.nprod.mgffilter

import java.io.FileReader
import java.io.FileWriter

data class Block(
    val ions: MutableList<String> = mutableListOf(),
    var content: String = "",
    val ignored: Boolean = false
)

fun Block.process(keep: Int, minIons: Int): String {
    var output = ""
    val ionsList =
        this.ions.distinct().map {
            it.trim().split("\t", " ").map { it.toDouble() }
        }.toMutableList()


    ionsList.sortWith { l1, l2 -> l2[1].compareTo(l1[1]) }  // Sort by intensity

    if (!this.ignored and (ionsList.size > minIons)) {
        output += "BEGIN IONS\n"
        output += this.content
        ionsList.takeLast(keep).forEach {
            output += "${String.format("%.7f", it[0])}\t${(it[1] * 10000.0).toInt()}\n"
        }
        output += "END IONS\n"
    }
    return output
}

fun main() {
    val inputFileName =
        "/data/Dbs/ISDB/MultiSources_ISDB_pos.mgf"
    val outputFileName =
        "/data/Dbs/ISDB/MultiSources_ISDB_pos_small.mgf"
    val keep = 100
    val minIons = 6
    var currentBlock = Block()
    var count = 0
    FileWriter(outputFileName).use { outputFile ->
        FileReader(inputFileName).use { inputFile ->
            inputFile.forEachLine { line ->
                when {
                    ((line.getOrNull(0) ?: ' ').isDigit()) -> currentBlock.ions.add(line)
                    line.startsWith("BEGIN IONS") -> currentBlock = Block()
                    line.startsWith("END IONS") -> outputFile.write(currentBlock.process(keep, minIons))
                    !currentBlock.ignored -> currentBlock.content += line + "\n"
                }
                count += 1
            }
        }
    }
}
