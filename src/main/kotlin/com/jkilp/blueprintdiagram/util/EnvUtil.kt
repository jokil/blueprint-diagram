package com.jkilp.blueprintdiagram.util

import java.io.File

object EnvUtil {
    fun getEnvMap(envFiles: List<String>?): HashMap<String, String> {
        val envMap = HashMap<String, String>()
        envFiles?.forEach { filename ->
            File(filename).readLines()
                    .filter { it.isNotBlank() && !it.startsWith("#") && it.contains("=") }
                    .forEach { line ->
                        val split = line.split("=")
                        envMap[split[0]] = split[1]
                    }
        }
        return envMap
    }
}