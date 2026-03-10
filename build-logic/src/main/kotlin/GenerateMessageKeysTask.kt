import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class GenerateMessageKeysTask : DefaultTask() {

    @get:InputFile
    abstract val yamlFile: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    abstract val packageName: Property<String>

    @get:Input
    abstract val objectName: Property<String>

    @TaskAction
    fun generate() {
        val yaml = org.yaml.snakeyaml.Yaml()
        val data: Map<String, Any> = yamlFile.get().asFile.reader().use { yaml.load(it) } ?: return

        val sb = StringBuilder()
        sb.appendLine("@file:Suppress(\"unused\")")
        sb.appendLine()
        sb.appendLine("package ${packageName.get()}")
        sb.appendLine()
        sb.appendLine("/** Auto-generated message keys. Do not edit manually. */")
        generateObject(sb, objectName.get(), data, "")

        val dir = packageName.get().replace('.', '/')
        val outputFile = File(outputDir.get().asFile, "$dir/${objectName.get()}.kt")
        outputFile.parentFile.mkdirs()
        outputFile.writeText(sb.toString())
    }

    @Suppress("UNCHECKED_CAST")
    private fun generateObject(sb: StringBuilder, name: String, data: Map<String, Any>, prefix: String, indent: String = "") {
        sb.appendLine("${indent}object $name {")
        for ((key, value) in data) {
            val fullKey = if (prefix.isEmpty()) key else "$prefix.$key"
            when (value) {
                is Map<*, *> -> {
                    sb.appendLine()
                    generateObject(sb, toPascalCase(key), value as Map<String, Any>, fullKey, "$indent    ")
                }
                else -> {
                    sb.appendLine("$indent    const val ${toUpperSnakeCase(key)} = \"$fullKey\"")
                }
            }
        }
        sb.appendLine("${indent}}")
    }

    private fun toUpperSnakeCase(input: String): String =
        input.replace("-", "_").replace(Regex("([a-z])([A-Z])"), "$1_$2").uppercase()

    private fun toPascalCase(input: String): String =
        input.split("-", "_").joinToString("") { it.replaceFirstChar { c -> c.uppercase() } }
}
