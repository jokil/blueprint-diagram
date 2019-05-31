package com.jkilp.blueprintdiagram.util

import java.io.File

object CfgUtil {

    fun getCfgMap(cfgFiles: List<File>, envMap: Map<String, String>): HashMap<String, HashMap<String, String>> {
        val cfgMap = HashMap<String, HashMap<String, String>>()
        cfgFiles.forEach { file ->
            cfgMap[file.name] = HashMap()
            file.readLines()
                    .filter { it.isNotBlank() && it.contains("=") }
                    .forEach {
                        val pair = CfgUtil.resolveParam(envMap, it)
                        cfgMap[file.name]?.set(pair.first, pair.second)
                    }
        }
        return cfgMap
    }

    fun resolveParam(envMap: Map<String,String>, line: String): Pair<String,String> {
        val paramName = line.substringBefore("=")
        var paramValue = line.substringAfter("=")
        if (paramValue.startsWith("\${env:")) {
            paramValue = paramValue.replace(".*\\\$\\{env:|\\}.*".toRegex(), "")
            if (paramValue.contains(":-")) {
                val splitted = paramValue.split(":-")
                paramValue = envMap[splitted[0]] ?: splitted[1]
            } else {
                paramValue = envMap[paramValue] ?: "\${env:$paramValue}"
            }
        }
        return Pair(paramName, paramValue)
    }
}