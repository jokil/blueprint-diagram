package com.jkilp.blueprintdiagram.util

import org.xml.sax.SAXException
import org.xml.sax.SAXParseException
import java.io.File
import javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI
import javax.xml.validation.SchemaFactory
import java.io.IOException
import java.io.InputStream
import javax.xml.transform.stream.StreamSource

object FileUtil {
    private val validator = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI)
            .newSchema(StreamSource(getResource("blueprint.xsd")))
            .newValidator()

    fun getFileLists(root: String): Pair<ArrayList<File>, ArrayList<File>> {
        val cfgFiles = ArrayList<File>()
        val xmlFiles = ArrayList<File>()

        File(root).walkTopDown()
                .filter { !File("$root/$it").isDirectory && !it.absolutePath.contains("/target/") }
                .forEach {
                    when (it.extension) {
                        "cfg" -> cfgFiles.add(it)
                        "xml" -> if (isBlueprint(it)) xmlFiles.add(it)
                    }
        }
        return Pair(cfgFiles, xmlFiles)
    }

    private fun isBlueprint(xml: File): Boolean {
        return try {
            validator.validate(StreamSource(xml))
            true
        } catch (e: SAXParseException) {
            false
        } catch (e: SAXException) {
            false
        } catch (e: IOException) {
            false
        }
    }

    private fun getResource(filename: String): InputStream {
        return this::class.java.classLoader.getResource(filename).openStream()
    }
}