package tororo1066.displaymonitor.documentation

enum class DocumentationNamespace(val key: String) {
    ACTION("action"),
    ELEMENT("element"),
    TYPE("type"),
    INTERNAL("internal"),
}

data class DocumentationRef(
    val namespace: DocumentationNamespace,
    val id: String,
) {
    init {
        require(id.isNotBlank()) { "Documentation reference ID must not be blank" }
    }

    val definitionKey: String
        get() = "${namespace.key}:$id"

    companion object {
        val ACTION_ITEM = DocumentationRef(DocumentationNamespace.INTERNAL, "action-item")
        val ACTION_LIST = DocumentationRef(DocumentationNamespace.INTERNAL, "action-list")
        val ACTION_CONFIGURATION = DocumentationRef(DocumentationNamespace.INTERNAL, "action-configuration")
        val ELEMENT_DEFINITION = DocumentationRef(DocumentationNamespace.INTERNAL, "element-definition")
    }
}
