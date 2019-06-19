package com.jkilp.blueprintdiagram.util

import com.jkilp.blueprintdiagram.dot.CamelContext
import java.io.File
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import java.util.regex.Matcher
import java.util.regex.Pattern


object XmlUtil {

    fun processBlueprint(xmlFile: File, cfgMap: Map<String, Map<String,String>>): List<CamelContext>? {
        val xml = xmlFile.readText()
        val configs = loadConfigs(xml, cfgMap)
        val xmlWithConfigs = resolvePropertyPlaceholders(xml, configs)
        return xmlToCamelContexts(xmlWithConfigs)
    }

    private fun xmlToCamelContexts(xml: String): List<CamelContext>? {
        val parsed = Jsoup.parse(xml, "", Parser.xmlParser())
        val camelContexts = parsed.select("camelContext") ?: return null
        return camelContexts.map {
            val routes = it.select("route").filter { !it.attr("id").isNullOrBlank() }
            if (routes.isEmpty()) null
            CamelContext(name = it.attr("id"), routes = routes.map { parseRoute(it) })
        }
    }

    private fun parseRoute(elem: Element): CamelContext.Route {
        val inputs = elem.select("from").map { toDotEntity(it.attr("uri")) }
        val wireTaps = elem.select("wireTap").map { toDotEntity(it.attr("uri")) }
        val recipients = elem.select("recipientList > simple,recipientList > constant").map { toDotEntity(it.text()) }
        val outputs = elem.select("to").map { toDotEntity(it.attr("uri")) }

        val outputSet = wireTaps.union(recipients).toMutableSet()
        outputSet.addAll(outputs.filter { it.name.contains(":") })

        return CamelContext.Route(
                name = elem.attr("id"),
                inputs = inputs,
                outputs = outputSet.toList()
        )
    }

    private fun toDotEntity(uri: String): CamelContext.Route.DotEntity {
        val relevantPart = uri.substringBefore("?")
        val topicFixed = virtualTopicFix(relevantPart)
        return CamelContext.Route.DotEntity(name = topicFixed)
    }

    /**
     * Remove Consumer.{consumer-name} from VirtualTopic-consumers,
     * so that they are properly connected to the producer in diagram.
     */
    private fun virtualTopicFix(uri: String): String {
        val virtualTopicPattern = Pattern.compile("(Consumer\\.)(.*?)(VirtualTopic\\.)", Pattern.CASE_INSENSITIVE)
        val buffer = StringBuffer()
        val matcher = virtualTopicPattern.matcher(uri)
        if (matcher.find()) matcher.appendReplacement(buffer, matcher.group(3))
        matcher.appendTail(buffer)
        return buffer.toString()
    }

    private fun resolvePropertyPlaceholders(xml: String, configs: Map<String,String>): String {
        val placeholderPattern = Pattern.compile("(\\{\\{)(.*?)(}})")
        val stringBuffer = StringBuffer()
        val matcher = placeholderPattern.matcher(xml)

        while (matcher.find()) {
            val newContent = configs[matcher.group(2)] ?: matcher.group(2)
            matcher.appendReplacement(stringBuffer, Matcher.quoteReplacement(newContent))
        }
        matcher.appendTail(stringBuffer)
        return stringBuffer.toString()
    }

    private fun loadConfigs(xml: String, cfgMap: Map<String, Map<String,String>>): Map<String,String> {
        val parsed = Jsoup.parse(xml, "", Parser.xmlParser())
        val configs = HashMap<String, String>()

        parsed.select("*|property-placeholder")
                .map { it.attr("persistent-id") }
                .map { cfgMap["$it.cfg"] }
                .filterNotNull()
                .forEach { configs.putAll(it) }

        return configs
    }
}