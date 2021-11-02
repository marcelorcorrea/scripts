#!/usr/bin/env kscript
@file:DependsOn("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.5.10")

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

val GAMES = mapOf<Int, Pair<String, Path>>(
    1 to Pair("Man of Medan", Paths.get("<<ManOfMedanSaveGamePath>>")),
    2 to Pair("Litle Hope", Paths.get("<<LittleHopeSaveGamePath>>"))
)
var input = if (args.isEmpty()) {
    println("Please choose the game: \n1 - Man Of Medan\n2 - Little Hope")
    var input = readLine()!!
    getNumber(input)
} else {
    getNumber(args[0])
}
fun getNumber(arg: String?): Int {
    return arg?.toIntOrNull() ?: throw IllegalArgumentException("Argument is not a number")
}

print("Please enter the folder name: ")
val fileName = readLine()!!
println("--------------------------------")

val path = Paths.get(GAMES.getValue(input).second.parent.toString(), fileName)
if (!path.exists()) {
    throw IllegalArgumentException("Folder name does not exist")
}
deleteFiles(path)
copyFiles(path)
println("--------------------------------")


fun deleteFiles(path: Path) {
    Files.walk(GAMES.getValue(input).second).forEach { path ->
        if (!path.isDirectory()) {
            println("Removing $path...")
            path.deleteIfExists()
        }
    }
    println("--------------------------------")
}

fun copyFiles(path: Path) {
    Files.walk(path).forEach { source: Path ->
        val destination = Paths.get(
            GAMES.getValue(input).second.toString(), source.toString()
                .substring(source.parent.toString().length)
        )
        if (!Files.isDirectory(source)) {
            println("Copying $source...")
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING)
        }
    }
}
println("All Done.")
