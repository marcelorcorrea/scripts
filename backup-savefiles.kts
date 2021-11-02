#!/usr/bin/env kscript
@file:DependsOn("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.5.10")

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Timer
import java.util.TimerTask
import kotlin.io.path.fileSize
import kotlin.reflect.typeOf
import kotlin.streams.toList

var lastModifiedTime: LocalDateTime = LocalDateTime.MAX
val GAMES = mapOf<Int, Pair<String, String>>(
    1 to Pair("Man of Medan", Paths.get("<<ManOfMedanSaveGamePath>>")),
    2 to Pair("Litle Hope", Paths.get("<<LittleHopeSaveGamePath>>"))
)

val myTimer = Timer();
val timerTask = object : TimerTask() {
    override fun run() {
        val formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd-HH_mm_ss")
        val saveGame = GAMES.getValue(input).second
        val backUpSaveGame = "$saveGame-${LocalDateTime.now().format(formatter)}"
        val hasCopied = copyDirectory(saveGame, backUpSaveGame)

        if (hasCopied) {
            println("Files copied to $backUpSaveGame")
        }
        println("=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+\n")
    }
}
var input = if (args.isEmpty()) {
    println("Please choose the game: \n1 - Man Of Medan\n2 - Little Hope")
    var input = readLine()!!
    getNumber(input)
} else {
    getNumber(args[0])
}
println("You have chosen ${GAMES.getValue(input).first}")
myTimer.scheduleAtFixedRate(timerTask, 0, 3 * (60 * 1000)); // Runs every 10 minutes

fun getNumber(arg: String?): Int {
    return arg?.toIntOrNull() ?: throw IllegalArgumentException("Argument is not a number")
}

fun copyDirectory(sourceDirectoryLocation: String, destinationDirectoryLocation: String): Boolean {
    val files = Files.walk(Paths.get(sourceDirectoryLocation)).toList()
    val biggestFile = files.maxByOrNull { it.fileSize() }!!
    val attributes = Files.readAttributes(biggestFile, BasicFileAttributes::class.java)
    val localDateTime = LocalDateTime.ofInstant(attributes.lastModifiedTime().toInstant(), ZoneId.systemDefault())
    if (lastModifiedTime.isEqual(LocalDateTime.MAX)) {
        lastModifiedTime = localDateTime
        println("Ready to copy...")
        return false
    } else {
        if (!lastModifiedTime.isEqual(localDateTime)) {
            println("Changes detected on the original files $lastModifiedTime - LocalDateTime: $localDateTime, prepare copy...")
            files.forEach { source: Path ->
                val destination = Paths.get(
                    destinationDirectoryLocation, source.toString()
                        .substring(sourceDirectoryLocation.length)
                )
                try {
                    println("Copying ${source.fileName}.")
                    Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            lastModifiedTime = localDateTime
            return true
        } else {
            println("No changes detected in the original file: ${biggestFile.fileName} - last modified: $localDateTime")
            return false
        }
    }
}
