package com.jkilp.blueprintdiagram.dot

data class CamelContext(val name: String, val routes: List<Route>) {
    override fun toString() = "subgraph \"cluster-$name\" {\n" +
            "node [style=filled,color=white];\n" +
            "style=filled;\n" +
            "color=lightgrey;\n" +
            "rank=source;\n" +
            "label = \"$name\";\n" +
            routes.joinToString("") { it.toString() } +
            "}\n"

    fun routePaths(): String {
        val list = HashSet<String>()
        routes.forEach { route ->
            // draw direct lines if message comes from same camelContext
            route.inputs.forEach { input ->
                val inputRoute = routes.find { it.outputs.any { it.name == input.name } }
                list.add("\"${inputRoute?.name ?: input.name}\" -> \"${route.name}\";\n")
            }
            route.outputs.forEach { output ->
                val outputRoute = routes.find { it.inputs.any { it.name == output.name } }
                list.add("\"${route.name}\" -> \"${outputRoute?.name ?: output.name}\";\n")
            }
        }
        return if (list.isNotEmpty()) list.joinToString("") else ""
    }

    data class Route(val name: String, val inputs: List<DotEntity>, val outputs: List<DotEntity>) {
        override fun toString() = if (name.isNotBlank()) "\"$name\";\n" else ""

        data class DotEntity(val name: String) {
            override fun toString() = "\"$name\" [shape=box];\n"
        }
    }
}