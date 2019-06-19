package com.jkilp.blueprintdiagram.dot

data class Diagram(val camelContexts: List<CamelContext>) {
    override fun toString() = "digraph G {\n" +
            "graph [pad=\"0.02\", nodesep=\"0.5\", ranksep=\"0.02\"];\n" +
            "center=true;\n" +
            "splines=polyline;\n" +
            //"splines=line;\n" +
            "layout=\"dot\";\n" +
            "rankdir=LR;\n" +
            camelContexts.joinToString("") { it.toString() } +
            camelContexts.map { it.routePaths() }.joinToString("") +
            "}\n"
}