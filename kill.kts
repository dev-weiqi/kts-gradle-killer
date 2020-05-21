#!/usr/bin/env kscript

val prefix = "$"
val suffix = " ??"
var pidList = mutableListOf<String>()

// 找出所有pid
var cmd = "ps -ax|grep gradle"() { it.append(prefix) }
println("All process: \n$cmd")

while (suffix in cmd) {
    //pid will between prefix and suffix
    val pid = cmd.substringAfter(prefix).substringBefore(suffix)
    pidList.add(pid)
    cmd = cmd.replace("$$pid$suffix", "")
}

if ("ttys000" !in cmd) {
    // 這兩個pid被殺掉之後會一直產生，導致無限loop，所以把他移除
    // drop /bin/sh -c ps -ax|grep gradle
    // drop grep gradle
    pidList = pidList.dropLast(2).toMutableList()
}

// 執行
if (pidList.isEmpty()) {
    println("Done")
} else {
    var killCmd = "kill -9"
    pidList.forEach { killCmd += " $it" }
    killCmd()
    println("$killCmd")
}

operator fun String.invoke(block: (StringBuilder) -> Unit = { }): String {
    val processBuilder = ProcessBuilder("/bin/sh", "-c", this)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()
    processBuilder.waitFor()
    val sb = StringBuilder()
    val lines = processBuilder.inputStream.bufferedReader().readLines()
    lines.forEach {
        block(sb)
        sb.append(it).append("\n")
    }
    return sb.toString()
}