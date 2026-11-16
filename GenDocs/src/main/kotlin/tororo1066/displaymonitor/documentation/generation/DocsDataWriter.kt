package tororo1066.displaymonitor.documentation.generation

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class DocsDataWriter {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    fun write(catalog: DocumentationCatalog, destination: Path) {
        Files.newBufferedWriter(destination, StandardCharsets.UTF_8).use { writer ->
            gson.toJson(toJson(catalog), writer)
        }
    }

    private fun toJson(catalog: DocumentationCatalog): JsonArray = JsonArray().apply {
        add(classCategory(DocumentationKind.ACTION.categoryName, catalog.actions))
        add(classCategory(DocumentationKind.ELEMENT.categoryName, catalog.elements))
        add(typeCategory(catalog.types))
        add(functionCategory(catalog.functions))
    }

    private fun classCategory(name: String, classes: List<ResolvedClassDoc>): JsonObject = JsonObject().apply {
        addProperty("name", name)
        add("classes", JsonArray().apply {
            classes.forEach { doc ->
                add(JsonObject().apply {
                    addProperty("name", doc.name)
                    addProperty("description", doc.description)
                    add("parameters", JsonArray().apply {
                        doc.parameters.forEach { parameter ->
                            add(JsonObject().apply {
                                addProperty("name", parameter.path.joinToString("."))
                                addProperty("description", parameter.description)
                                addProperty("default", parameter.defaultValue)
                                addProperty("type", parameter.typeName)
                            })
                        }
                    })
                })
            }
        })
    }

    private fun typeCategory(types: List<ResolvedTypeDoc>): JsonObject = JsonObject().apply {
        addProperty("name", "Type")
        add("classes", JsonArray().apply {
            types.forEach { type ->
                add(JsonObject().apply {
                    addProperty("name", type.name)
                    addProperty("description", type.description + "\n\n例:\n" + type.example)
                    add("parameters", JsonArray())
                })
            }
        })
    }

    private fun functionCategory(functions: List<ResolvedFunctionDoc>): JsonObject = JsonObject().apply {
        addProperty("name", "Function")
        add("classes", JsonArray().apply {
            functions.forEach { function ->
                add(JsonObject().apply {
                    addProperty("name", function.name)
                    addProperty("description", function.description)
                    add("parameters", JsonArray().apply {
                        function.parameters.forEach { parameter ->
                            add(JsonObject().apply {
                                addProperty("name", parameter.name)
                                addProperty("description", parameter.description)
                                addProperty("type", parameter.typeName)
                            })
                        }
                    })
                })
            }
        })
    }
}
