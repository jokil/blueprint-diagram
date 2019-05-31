package com.jkilp.blueprintdiagram

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import java.io.File

class ClickStart: CliktCommand() {
    val env: List<String>? by option(help="Environment file(s) for variable substitution. Separate paths with comma.").split(",")
    val path: String by option(help="Path to search for blueprints and configs.").required()
    val output: String by option(help="Filename to use in generated .dot and .html files").default("diagram")

    override fun run() = DiagramWriter.run(env,path,output)
}

@Mojo(name = "diagram")
class MojoStart(): AbstractMojo() {
    @Parameter var env: List<File>? = null
    @Parameter var path: File? = null
    @Parameter var output: String = "diagram"

    override fun execute(){
        DiagramWriter.run(
                env?.map { it.absolutePath },
                path?.absolutePath ?: File(".").absolutePath,
                output
        )
    }
}

fun main(args: Array<String>) = ClickStart().main(args)
