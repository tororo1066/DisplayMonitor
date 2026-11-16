package tororo1066.displaymonitor.documentation.generation

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import tororo1066.displaymonitor.documentation.ConfigurationType
import tororo1066.displaymonitor.documentation.DocumentationRef
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class JsonSchemaWriter {
    private companion object {
        // Expressions are opaque at validation time, so require syntax that the runtime evaluator recognizes.
        const val ENUM_EXPRESSION_PATTERN =
            """\$\{[\s\S]*\}|@[A-Za-z0-9_.:/\\#-]+|"(?:[^"\\]|\\.)*"|\+|==|!=|>=|<=|&&|\|\||[()<>]"""
    }

    private val gson = GsonBuilder().setPrettyPrinting().create()

    fun write(catalog: DocumentationCatalog, destination: Path) {
        Files.newBufferedWriter(destination, StandardCharsets.UTF_8).use { writer ->
            gson.toJson(toJson(catalog), writer)
        }
    }

    private fun toJson(catalog: DocumentationCatalog): JsonObject {
        catalog.validate()
        val definitions = linkedMapOf<String, JsonObject>()

        catalog.actions.forEach { addDefinition(definitions, it.ref, classSchema(it)) }
        catalog.elements.forEach { addDefinition(definitions, it.ref, classSchema(it)) }

        addDefinition(
            definitions,
            DocumentationRef.ACTION_ITEM,
            oneOf(
                catalog.actions.map { schemaRef(it.ref) } +
                    genericActionSchema(catalog.actions.map(ResolvedClassDoc::name))
            ),
        )
        addDefinition(definitions, DocumentationRef.ACTION_LIST, actionListSchema())
        addDefinition(definitions, DocumentationRef.ACTION_CONFIGURATION, actionConfigurationSchema())
        addDefinition(
            definitions,
            DocumentationRef.ELEMENT_DEFINITION,
            oneOf(
                catalog.elements.map { schemaRef(it.ref) } +
                    genericElementSchema(catalog.elements.map(ResolvedClassDoc::name))
            ),
        )

        return JsonObject().apply {
            addProperty("\$schema", "http://json-schema.org/draft-07/schema#")
            addProperty("title", "DisplayMonitor configuration")
            addProperty(
                "description",
                "Action definitions and Element presets used by DisplayMonitor."
            )
            addProperty("type", "object")
            add(
                "additionalProperties",
                JsonObject().apply {
                    add(
                        "anyOf",
                        JsonArray().apply {
                            add(schemaRef(DocumentationRef.ACTION_CONFIGURATION))
                            add(schemaRef(DocumentationRef.ELEMENT_DEFINITION))
                        }
                    )
                }
            )
            add("definitions", JsonObject().apply {
                definitions.forEach { (key, value) -> add(key, value) }
            })
        }
    }

    private fun addDefinition(
        definitions: MutableMap<String, JsonObject>,
        ref: DocumentationRef,
        schema: JsonObject,
    ) {
        check(definitions.putIfAbsent(ref.definitionKey, schema) == null) {
            "Duplicate JSON Schema definition: ${ref.definitionKey}"
        }
    }

    private fun classSchema(doc: ResolvedClassDoc): JsonObject {
        val properties = propertyTree(doc.parameters)
        val required = listOf(doc.kind.discriminator) + doc.parameters
            .filter { it.required && it.path.isNotEmpty() }
            .map { it.path.first() }
            .distinct()
        properties.add(
            doc.kind.discriminator,
            JsonObject().apply {
                add("enum", jsonArray(doc.name))
                addProperty(
                    "description",
                    if (doc.kind == DocumentationKind.ACTION) "実行するActionの種類。" else "生成するElementの種類。"
                )
            },
        )

        val schema = objectSchema().apply {
            addProperty("title", doc.name)
            addProperty("description", doc.description)
            add("properties", properties)
            add("required", jsonArray(required))
        }
        return schema
    }

    private class PropertyNode {
        var parameter: ResolvedParameter? = null
        val children = linkedMapOf<String, PropertyNode>()
    }

    private fun propertyTree(parameters: List<ResolvedParameter>): JsonObject {
        val root = PropertyNode()
        parameters.forEach { parameter ->
            var current = root
            parameter.path.forEach { segment ->
                current = current.children.getOrPut(segment, ::PropertyNode)
            }
            check(current.parameter == null) {
                "Duplicate parameter path: ${parameter.path.joinToString(".")}"
            }
            current.parameter = parameter
        }
        return propertiesOf(root)
    }

    private fun propertiesOf(root: PropertyNode): JsonObject = JsonObject().apply {
        root.children.forEach { (name, node) -> add(name, propertySchema(node)) }
    }

    private fun propertySchema(node: PropertyNode): JsonObject {
        val parameter = node.parameter
        val childProperties = propertiesOf(node)
        val childRequired = node.children
            .filterValues { it.parameter?.required == true }
            .keys
        val hasChildren = node.children.isNotEmpty()

        val valueSchema = if (hasChildren) objectSchema() else parameter?.let { configurationSchema(it.type) }
            ?: JsonObject()
        if (hasChildren) {
            valueSchema.add("properties", childProperties)
            if (childRequired.isNotEmpty()) valueSchema.add("required", jsonArray(childRequired.toList()))
        }
        val schema = if (parameter?.nullable == true) {
            anyOf(listOf(valueSchema, typedSchema("null")))
        } else {
            valueSchema
        }

        if (parameter != null) {
            if (parameter.description.isNotEmpty()) schema.addProperty("description", parameter.description)
            if (parameter.defaultValue.isNotEmpty()) schema.add("default", defaultValue(parameter))
        }
        return schema
    }

    private fun configurationSchema(type: ConfigurationType): JsonObject = when (type) {
        ConfigurationType.Any -> JsonObject()
        ConfigurationType.String -> typedSchema("string")
        ConfigurationType.Boolean -> typedSchema("boolean")
        ConfigurationType.Integer -> typedSchema("integer")
        ConfigurationType.Number -> typedSchema("number")
        ConfigurationType.Object -> objectSchema()
        is ConfigurationType.AnyOf -> anyOf(type.alternatives.map(::configurationSchema))
        is ConfigurationType.Record -> recordSchema(type)
        is ConfigurationType.Enum -> enumSchema(type.values)
        is ConfigurationType.Ref -> schemaRef(type.target)
        is ConfigurationType.Expression -> {
            val valueSchema = configurationSchema(type.value)
            when {
                type.value == ConfigurationType.String || type.value == ConfigurationType.Any -> valueSchema
                type.value is ConfigurationType.Enum -> anyOf(
                    listOf(valueSchema, expressionStringSchema())
                )
                else -> anyOf(listOf(valueSchema, typedSchema("string")))
            }
        }
        is ConfigurationType.Array -> {
            val arraySchema = JsonObject().apply {
                addProperty("type", "array")
                add("items", configurationSchema(type.item))
            }
            if (type.acceptInlineString) {
                anyOf(listOf(arraySchema, typedSchema("string")))
            } else {
                arraySchema
            }
        }
    }

    private fun recordSchema(type: ConfigurationType.Record): JsonObject = objectSchema().apply {
        add("properties", JsonObject().apply {
            type.properties.forEach { property ->
                add(property.name, configurationSchema(property.type).apply {
                    if (property.description.isNotEmpty()) addProperty("description", property.description)
                })
            }
        })

        val required = type.properties.filter { it.required }.map { it.name }
        if (required.isNotEmpty()) add("required", jsonArray(required))

        if (type.requiredAnyOf.isNotEmpty()) {
            add("anyOf", JsonArray().apply {
                type.requiredAnyOf.forEach { property ->
                    add(JsonObject().apply { add("required", jsonArray(property)) })
                }
            })
        }

        if (!type.additionalProperties) add("additionalProperties", JsonPrimitive(false))
    }

    private fun enumSchema(values: List<String>): JsonObject {
        val exactValues = JsonObject().apply { add("enum", jsonArray(values)) }
        if (values.isEmpty()) return exactValues

        val caseInsensitiveValues = typedSchema("string").apply {
            addProperty("pattern", "^(?:${values.joinToString("|") { it.caseInsensitivePattern() }})$")
        }
        return anyOf(listOf(exactValues, caseInsensitiveValues))
    }

    private fun expressionStringSchema(): JsonObject = typedSchema("string").apply {
        addProperty("pattern", ENUM_EXPRESSION_PATTERN)
    }

    private fun String.caseInsensitivePattern(): String = buildString {
        for (character in this@caseInsensitivePattern) {
            if (character.isLetter()) {
                append('[')
                append(character.lowercaseChar())
                append(character.uppercaseChar())
                append(']')
            } else {
                if (character in "\\.^$|?*+()[]{}") append('\\')
                append(character)
            }
        }
    }

    private fun actionConfigurationSchema(): JsonObject = objectSchema().apply {
        add(
            "properties",
            JsonObject().apply {
                add("triggers", triggerListSchema())
                add("actions", schemaRef(DocumentationRef.ACTION_LIST))
            },
        )
        add("not", JsonObject().apply { add("required", jsonArray("type")) })
    }

    private fun triggerListSchema(): JsonObject = JsonObject().apply {
        addProperty("type", "array")
        add(
            "items",
            objectSchema().apply {
                add(
                    "properties",
                    JsonObject().apply {
                        add(
                            "trigger",
                            typedSchema("string").apply {
                                addProperty("description", "Actionを実行するイベント名。")
                            },
                        )
                    },
                )
                add("required", jsonArray("trigger"))
            },
        )
    }

    private fun actionListSchema(): JsonObject = JsonObject().apply {
        addProperty("type", "array")
        add("items", schemaRef(DocumentationRef.ACTION_ITEM))
    }

    private fun genericActionSchema(knownNames: List<String>): JsonObject = objectSchema().apply {
        add(
            "properties",
            JsonObject().apply {
                add(
                    "class",
                    typedSchema("string").apply {
                        if (knownNames.isNotEmpty()) {
                            add("not", JsonObject().apply { add("enum", jsonArray(knownNames)) })
                        }
                    },
                )
            },
        )
        add("required", jsonArray("class"))
    }

    private fun genericElementSchema(knownNames: List<String>): JsonObject = objectSchema().apply {
        add(
            "properties",
            JsonObject().apply {
                add(
                    "type",
                    typedSchema("string").apply {
                        if (knownNames.isNotEmpty()) {
                            add("not", JsonObject().apply { add("enum", jsonArray(knownNames)) })
                        }
                    },
                )
            },
        )
        add("required", jsonArray("type"))
    }

    private fun defaultValue(parameter: ResolvedParameter): JsonElement {
        val raw = parameter.defaultValue
        if (raw.equals("null", ignoreCase = true)) return JsonNull.INSTANCE
        return when (baseType(parameter.type)) {
            ConfigurationType.Boolean -> when (raw.lowercase()) {
                "true" -> JsonPrimitive(true)
                "false" -> JsonPrimitive(false)
                else -> JsonPrimitive(raw)
            }
            ConfigurationType.Integer -> raw.toLongOrNull()?.let(::JsonPrimitive) ?: JsonPrimitive(raw)
            ConfigurationType.Number -> raw.toDoubleOrNull()?.let(::JsonPrimitive) ?: JsonPrimitive(raw)
            else -> JsonPrimitive(raw)
        }
    }

    private fun baseType(type: ConfigurationType): ConfigurationType = when (type) {
        is ConfigurationType.Expression -> baseType(type.value)
        is ConfigurationType.AnyOf -> type.alternatives.firstOrNull()?.let(::baseType) ?: ConfigurationType.Any
        else -> type
    }

    private fun objectSchema(): JsonObject = typedSchema("object")

    private fun typedSchema(type: String): JsonObject = JsonObject().apply { addProperty("type", type) }

    private fun oneOf(schemas: List<JsonObject>): JsonObject = JsonObject().apply {
        add("oneOf", JsonArray().apply { schemas.forEach(::add) })
    }

    private fun anyOf(schemas: List<JsonObject>): JsonObject = JsonObject().apply {
        add("anyOf", JsonArray().apply { schemas.forEach(::add) })
    }

    private fun schemaRef(ref: DocumentationRef): JsonObject = JsonObject().apply {
        addProperty("\$ref", "#/definitions/${ref.definitionKey.jsonPointerToken()}")
    }

    private fun String.jsonPointerToken(): String = replace("~", "~0").replace("/", "~1")

    private fun jsonArray(vararg values: String): JsonArray = jsonArray(values.toList())

    private fun jsonArray(values: List<String>): JsonArray = JsonArray().apply {
        values.forEach(::add)
    }
}
