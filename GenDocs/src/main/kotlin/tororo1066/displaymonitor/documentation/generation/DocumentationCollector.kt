package tororo1066.displaymonitor.documentation.generation

import tororo1066.displaymonitor.configuration.expression.AbstractFunction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ConfigurationType
import tororo1066.displaymonitor.documentation.DocumentationNamespace
import tororo1066.displaymonitor.documentation.DocumentationRef
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitor.documentation.ParameterType
import tororo1066.displaymonitor.documentation.getParameterType
import tororo1066.displaymonitor.documentation.parameterTypeDocs
import tororo1066.displaymonitorapi.elements.CustomSettable
import tororo1066.displaymonitorapi.elements.Settable
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.jvm.kotlinProperty

class DocumentationCollector {
    fun collect(
        actionClasses: List<Class<*>>,
        elementClasses: List<Class<*>>,
        functionClasses: List<Class<*>>,
    ): DocumentationCatalog {
        val discoveredTypes = linkedMapOf<String, ResolvedTypeDoc>()

        fun collectType(type: KClass<*>, doc: ParameterType) {
            if (!type.java.isEnum && !CustomSettable::class.java.isAssignableFrom(type.java)) return
            val key = type.qualifiedName ?: type.java.name
            discoveredTypes.putIfAbsent(
                key,
                ResolvedTypeDoc(key, doc.name, doc.description, doc.example),
            )
        }

        fun collectClass(clazz: Class<*>, kind: DocumentationKind): ResolvedClassDoc? {
            val classDoc = clazz.getAnnotation(ClassDoc::class.java) ?: return null
            val namespace = when (kind) {
                DocumentationKind.ACTION -> DocumentationNamespace.ACTION
                DocumentationKind.ELEMENT -> DocumentationNamespace.ELEMENT
            }
            val parameters = collectParameters(
                clazz = clazz,
                prefix = emptyList(),
                visited = emptySet(),
                onType = ::collectType,
            ).sortedBy { it.path.joinToString(".") }

            return ResolvedClassDoc(
                ref = DocumentationRef(namespace, classDoc.name),
                name = classDoc.name,
                description = classDoc.description,
                kind = kind,
                parameters = parameters,
            )
        }

        val actions = actionClasses.mapNotNull { collectClass(it, DocumentationKind.ACTION) }
            .sortedBy(ResolvedClassDoc::name)
        val elements = elementClasses.mapNotNull { collectClass(it, DocumentationKind.ELEMENT) }
            .sortedBy(ResolvedClassDoc::name)

        val functions = functionClasses.asSequence()
            .filter { AbstractFunction::class.java.isAssignableFrom(it) }
            .filterNot { it.isInterface || Modifier.isAbstract(it.modifiers) }
            .map { clazz ->
                val function = clazz.getConstructor().newInstance() as AbstractFunction
                val parameters = function.getParameters().mapIndexed { index, parameter ->
                    val typeDoc = getParameterType(parameter.type)
                    collectType(parameter.type, typeDoc)
                    ResolvedFunctionParameter(
                        name = "$index.${parameter.name}",
                        description = parameter.description,
                        typeName = typeDoc.name,
                    )
                }
                ResolvedFunctionDoc(
                    name = function.name,
                    description = function.getDescription(),
                    parameters = parameters,
                )
            }
            .sortedBy(ResolvedFunctionDoc::name)
            .toList()

        val builtInTypes = parameterTypeDocs.map { (type, doc) ->
            ResolvedTypeDoc(type.qualifiedName ?: type.java.name, doc.name, doc.description, doc.example)
        }
        val catalog = DocumentationCatalog(
            actions = actions,
            elements = elements,
            types = (builtInTypes + discoveredTypes.values.sortedBy(ResolvedTypeDoc::sourceType))
                .distinctBy(ResolvedTypeDoc::sourceType),
            functions = functions,
        )
        catalog.validate()
        return catalog
    }

    private fun collectParameters(
        clazz: Class<*>,
        prefix: List<String>,
        visited: Set<Class<*>>,
        onType: (KClass<*>, ParameterType) -> Unit,
    ): List<ResolvedParameter> {
        if (clazz in visited) return emptyList()
        val nextVisited = visited + clazz
        val parameters = mutableListOf<ResolvedParameter>()

        generateSequence(clazz as Class<*>?) { it.superclass }
            .takeWhile { it != Any::class.java }
            .forEach { currentClass ->
                currentClass.declaredFields
                    .filterNot { Modifier.isStatic(it.modifiers) || it.isSynthetic }
                    .sortedBy(Field::getName)
                    .forEach { field ->
                        val parameterDoc = field.getAnnotation(ParameterDoc::class.java)
                        val settable = field.getAnnotation(Settable::class.java)
                        val customSettable = CustomSettable::class.java.isAssignableFrom(field.type)
                        val fieldPath = prefix + ConfigurationKeyResolver.path(field, parameterDoc)

                        if (parameterDoc != null) {
                            val typeDoc = getParameterType(field)
                            val documentedType = documentedType(typeDoc, customSettable)
                            val sourceType = documentedClass(field, parameterDoc)
                            onType(sourceType, typeDoc)
                            parameters += ResolvedParameter(
                                path = fieldPath,
                                description = parameterDoc.description,
                                defaultValue = parameterDoc.default,
                                typeName = typeDoc.name,
                                type = documentedType,
                                nullable = field.isKotlinNullable(),
                                required = false,
                            )
                        }

                        val shouldRecurse = customSettable || settable?.childOnly == true
                        if (shouldRecurse) {
                            parameters += collectParameters(
                                clazz = field.type,
                                prefix = fieldPath,
                                visited = nextVisited,
                                onType = onType,
                            )
                        }
                    }
            }

        return parameters
    }

    private fun documentedClass(field: Field, annotation: ParameterDoc): KClass<*> =
        if (annotation.type != Any::class) annotation.type else field.type.kotlin

    private fun documentedType(
        typeDoc: ParameterType,
        customSettable: Boolean,
    ): ConfigurationType = when {
        customSettable -> ConfigurationType.Object
        else -> typeDoc.configurationType
    }

    private fun Field.isKotlinNullable(): Boolean =
        runCatching { kotlinProperty?.returnType?.isMarkedNullable == true }.getOrDefault(false)
}
