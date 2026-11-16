package tororo1066.displaymonitor

import tororo1066.displaymonitor.documentation.generation.ClassScanner
import tororo1066.displaymonitor.documentation.generation.DocsDataWriter
import tororo1066.displaymonitor.documentation.generation.DocumentationCollector
import tororo1066.displaymonitor.documentation.generation.JsonSchemaWriter
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

object GenerateDocData {
    private const val JSON_SCHEMA_FILE = "json-schema.json"
    private const val DOCS_DATA_FILE = "docs-data.json"
    private const val FUNCTION_PACKAGE = "tororo1066.displaymonitor.configuration.expression.functions"

    val packages = linkedMapOf(
        "tororo1066.displaymonitor.actions.builtin" to "Action",
        "tororo1066.displaymonitor.elements.builtin" to "Element",
    )

    @JvmStatic
    fun main(args: Array<String>) {
        val scanner = ClassScanner()
        val classesByPackage = packages.keys.associateWith(scanner::scan)
        val functions = scanner.scan(FUNCTION_PACKAGE)
        val catalog = DocumentationCollector().collect(
            actionClasses = classesByPackage["tororo1066.displaymonitor.actions.builtin"].orEmpty(),
            elementClasses = classesByPackage["tororo1066.displaymonitor.elements.builtin"].orEmpty(),
            functionClasses = functions,
        )

        writeOutputs(outputDirectory(args), catalog)
    }

    private fun outputDirectory(args: Array<String>): Path {
        if (args.isEmpty()) return Paths.get(".").toAbsolutePath().normalize()

        val outputPath = when {
            args.size == 1 && args[0].startsWith("--output-dir=") ->
                args[0].substringAfter('=')
            args.size == 2 && args[0] == "--output-dir" -> args[1]
            else -> error("Usage: GenDocs [--output-dir <directory>]")
        }
        require(outputPath.isNotBlank()) { "Output directory must not be blank" }
        return Paths.get(outputPath).toAbsolutePath().normalize()
    }

    private fun writeOutputs(outputDirectory: Path, catalog: tororo1066.displaymonitor.documentation.generation.DocumentationCatalog) {
        Files.createDirectories(outputDirectory)
        val docsTarget = outputDirectory.resolve(DOCS_DATA_FILE)
        val schemaTarget = outputDirectory.resolve(JSON_SCHEMA_FILE)
        val docsTemporary = Files.createTempFile(outputDirectory, "docs-data-", ".tmp")
        val schemaTemporary = Files.createTempFile(outputDirectory, "json-schema-", ".tmp")

        try {
            DocsDataWriter().write(catalog, docsTemporary)
            JsonSchemaWriter().write(catalog, schemaTemporary)
            replaceFile(docsTemporary, docsTarget)
            replaceFile(schemaTemporary, schemaTarget)
        } finally {
            Files.deleteIfExists(docsTemporary)
            Files.deleteIfExists(schemaTemporary)
        }
    }

    private fun replaceFile(source: Path, target: Path) {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING)
        }
    }
}
