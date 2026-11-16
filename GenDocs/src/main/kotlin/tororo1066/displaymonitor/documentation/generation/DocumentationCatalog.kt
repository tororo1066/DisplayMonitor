package tororo1066.displaymonitor.documentation.generation

import tororo1066.displaymonitor.documentation.ConfigurationType
import tororo1066.displaymonitor.documentation.DocumentationNamespace
import tororo1066.displaymonitor.documentation.DocumentationRef

enum class DocumentationKind(
    val categoryName: String,
    val discriminator: String,
) {
    ACTION("Action", "class"),
    ELEMENT("Element", "type"),
}

data class ResolvedParameter(
    val path: List<String>,
    val description: String,
    val defaultValue: String,
    val typeName: String,
    val type: ConfigurationType,
    val nullable: Boolean,
    val required: Boolean = false,
)

data class ResolvedClassDoc(
    val ref: DocumentationRef,
    val name: String,
    val description: String,
    val kind: DocumentationKind,
    val parameters: List<ResolvedParameter>,
)

data class ResolvedTypeDoc(
    val sourceType: String,
    val name: String,
    val description: String,
    val example: String,
)

data class ResolvedFunctionParameter(
    val name: String,
    val description: String,
    val typeName: String,
)

data class ResolvedFunctionDoc(
    val name: String,
    val description: String,
    val parameters: List<ResolvedFunctionParameter>,
)

data class DocumentationCatalog(
    val actions: List<ResolvedClassDoc>,
    val elements: List<ResolvedClassDoc>,
    val types: List<ResolvedTypeDoc>,
    val functions: List<ResolvedFunctionDoc>,
) {
    fun validate() {
        val classDocs = actions + elements
        val duplicateRefs = classDocs.groupBy(ResolvedClassDoc::ref)
            .filterValues { it.size > 1 }
            .keys
        require(duplicateRefs.isEmpty()) {
            "Duplicate documentation refs: ${duplicateRefs.joinToString { it.definitionKey }}"
        }

        classDocs.forEach { doc ->
            val duplicates = doc.parameters.groupBy(ResolvedParameter::path)
                .filterValues { it.size > 1 }
                .keys
            require(duplicates.isEmpty()) {
                "Duplicate parameter paths in ${doc.ref.definitionKey}: " +
                    duplicates.joinToString { it.joinToString(".") }
            }

            val parametersByPath = doc.parameters.associateBy(ResolvedParameter::path)
            doc.parameters.forEach { parameter ->
                for (length in 1 until parameter.path.size) {
                    val parent = parametersByPath[parameter.path.take(length)] ?: continue
                    require(parent.type.supportsChildProperties()) {
                        "Parameter ${parent.path.joinToString(".")} in ${doc.ref.definitionKey} " +
                            "is both a scalar value and a parent of another parameter"
                    }
                }
            }
        }

        val definitions = classDocs.mapTo(mutableSetOf(), ResolvedClassDoc::ref).apply {
            addAll(INTERNAL_DEFINITIONS)
        }
        val unresolved = classDocs.flatMap { doc ->
            doc.parameters.flatMap { it.type.references() }
        }.filterNot(definitions::contains).distinct()
        require(unresolved.isEmpty()) {
            "Unresolved documentation refs: ${unresolved.joinToString { it.definitionKey }}"
        }

        val invalidNamespaces = classDocs.filter {
            when (it.kind) {
                DocumentationKind.ACTION -> it.ref.namespace != DocumentationNamespace.ACTION
                DocumentationKind.ELEMENT -> it.ref.namespace != DocumentationNamespace.ELEMENT
            }
        }
        require(invalidNamespaces.isEmpty()) {
            "Class documentation has a ref in the wrong namespace: " +
                invalidNamespaces.joinToString { it.name }
        }
    }

    private fun ConfigurationType.references(): List<DocumentationRef> = when (this) {
        is ConfigurationType.Ref -> listOf(target)
        is ConfigurationType.AnyOf -> alternatives.flatMap { it.references() }
        is ConfigurationType.Array -> item.references()
        is ConfigurationType.Expression -> value.references()
        is ConfigurationType.Record -> properties.flatMap { it.type.references() }
        is ConfigurationType.Enum,
        ConfigurationType.Any,
        ConfigurationType.String,
        ConfigurationType.Boolean,
        ConfigurationType.Integer,
        ConfigurationType.Number,
        ConfigurationType.Object -> emptyList()
    }

    private fun ConfigurationType.supportsChildProperties(): Boolean = when (this) {
        ConfigurationType.Object -> true
        is ConfigurationType.Expression -> value.supportsChildProperties()
        is ConfigurationType.AnyOf -> alternatives.all { it.supportsChildProperties() }
        is ConfigurationType.Record -> true
        else -> false
    }

    companion object {
        private val INTERNAL_DEFINITIONS = setOf(
            DocumentationRef.ACTION_ITEM,
            DocumentationRef.ACTION_LIST,
            DocumentationRef.ACTION_CONFIGURATION,
            DocumentationRef.ELEMENT_DEFINITION,
        )
    }
}
