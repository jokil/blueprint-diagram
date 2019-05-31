package com.jkilp.blueprintdiagram

import com.jkilp.blueprintdiagram.dot.Diagram
import com.jkilp.blueprintdiagram.util.CfgUtil
import com.jkilp.blueprintdiagram.util.EnvUtil
import com.jkilp.blueprintdiagram.util.FileUtil
import com.jkilp.blueprintdiagram.util.XmlUtil
import java.io.File

object DiagramWriter {
    fun run(env: List<String>?, path: String, output: String) {
        val (cfgFiles, xmlFiles) = FileUtil.getFileLists(path)
        val envMap = EnvUtil.getEnvMap(env)
        val cfgMap = CfgUtil.getCfgMap(cfgFiles, envMap)

        val diagram = Diagram(xmlFiles.map { XmlUtil.processBlueprint(it, cfgMap) }.filterNotNull().flatten())

        File("$output.dot").writeText(diagram.toString())
        File("$output.html").writeText("<!DOCTYPE html>\n" +
                "<meta charset=\"utf-8\">\n" +
                "<body>\n" +
                "<script src=\"https://d3js.org/d3.v5.js\"></script>\n" +
                "<script src=\"http://viz-js.com/bower_components/viz.js/viz-lite.js\"></script>\n" +
                "<script src=\"https://github.com/magjac/d3-graphviz/releases/download/v0.1.2/d3-graphviz.min.js\"></script>\n" +
                "<div id=\"graph\" style=\"text-align: center;\"></div>\n" +
                "<script>\n" +
                "  d3.select(\"#graph\").graphviz()\n" +
                "    .renderDot(`${diagram.toString().replace("\$", "\\\$")}`);\n" +
                "</script>")
    }
}