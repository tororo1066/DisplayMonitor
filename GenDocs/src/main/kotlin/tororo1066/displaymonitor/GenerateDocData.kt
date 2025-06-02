package tororo1066.displaymonitor

import com.google.gson.Gson
import tororo1066.displaymonitor.configuration.expression.AbstractFunction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitor.documentation.getParameterType
import tororo1066.displaymonitor.documentation.parameterTypeDocs
import tororo1066.displaymonitorapi.configuration.expression.IAbstractFunction
import tororo1066.displaymonitorapi.elements.Settable
import java.io.File
import java.net.JarURLConnection
import java.net.URISyntaxException
import java.net.URL
import java.util.jar.JarFile

object GenerateDocData {
    val packages = mutableMapOf(
        "tororo1066.displaymonitor.actions.builtin" to "Action",
        "tororo1066.displaymonitor.elements.builtin" to "Element",
    )

    @JvmStatic
    fun main(args: Array<String>) {
        val jsonWriter = Gson().newJsonWriter(File("docs-data.json").writer())
        jsonWriter.setIndent("  ")
        jsonWriter.beginArray()

        packages.forEach { (packageName, type) ->
            jsonWriter.beginObject()
            jsonWriter.name("name").value(type)
            jsonWriter.name("classes").beginArray()
            javaClass.protectionDomain.codeSource.location.getClasses(packageName).forEach second@ { clazz ->
                val classDoc = clazz.getAnnotation(ClassDoc::class.java) ?: return@second
                jsonWriter.beginObject()
                jsonWriter.name("name").value(classDoc.name)
                jsonWriter.name("description").value(classDoc.description)

                jsonWriter.name("parameters").beginArray()

                fun checkField(clazz: Class<*>, name: String) {
                    clazz.declaredFields.forEach third@ { field ->
                        if (field.isAnnotationPresent(Settable::class.java)) {
                            val annotation = field.getAnnotation(Settable::class.java)!!
                            if (annotation.childOnly) {
                                checkField(field.type, if (name.isEmpty()) field.name else "$name.${field.name}")
                            }
                        }
                        val parameterDoc = field.getAnnotation(ParameterDoc::class.java) ?: return@third
                        val docName = parameterDoc.name.ifEmpty { field.name }
                        jsonWriter.beginObject()
                        jsonWriter.name("name").value(if (name.isEmpty()) docName else "$name.${docName}")
                        jsonWriter.name("description").value(parameterDoc.description)
                        jsonWriter.name("type").value(getParameterType(field).name)
                        jsonWriter.endObject()
                    }
                }

                var currentClass: Class<*>? = clazz
                while (currentClass != null) {
                    checkField(currentClass, "")
                    currentClass = currentClass.superclass
                }

                jsonWriter.endArray()
                jsonWriter.endObject()
            }

            jsonWriter.endArray()
            jsonWriter.endObject()
        }

        jsonWriter.beginObject()
        jsonWriter.name("name").value("Type")
        jsonWriter.name("classes").beginArray()
        parameterTypeDocs.forEach second@ { (type, parameterType) ->
            jsonWriter.beginObject()
            jsonWriter.name("name").value(type.simpleName ?: "Unknown")
            jsonWriter.name("description").value(parameterType.description + "\n\n例:\n  " + parameterType.example)
            jsonWriter.name("parameters").beginArray().endArray()
            jsonWriter.endObject()
        }
        jsonWriter.endArray()
        jsonWriter.endObject()

        jsonWriter.beginObject()
        jsonWriter.name("name").value("Function")
        jsonWriter.name("classes").beginArray()
        javaClass.protectionDomain.codeSource.location.getClasses("tororo1066.displaymonitor.configuration.expression.functions").forEach second@ { clazz ->
            if (AbstractFunction::class.java.isAssignableFrom(clazz)) {
                val constructor = clazz.getConstructor()
                val function = constructor.newInstance() as AbstractFunction
                jsonWriter.beginObject()
                jsonWriter.name("name").value(function.name)
                jsonWriter.name("description").value(function.getDescription())
                jsonWriter.name("parameters").beginArray()

                function.getParameters().forEachIndexed { index, parameter ->
                    jsonWriter.beginObject()
                    jsonWriter.name("name").value(index.toString() + ". " + parameter.name)
                    jsonWriter.name("description").value(parameter.description)
                    jsonWriter.name("type").value(getParameterType(parameter.type).name)
                    jsonWriter.endObject()
                }
            }
        }
        jsonWriter.endArray()
        jsonWriter.endObject()


        jsonWriter.endArray()
        jsonWriter.close()
    }

    private fun URL.getClasses(packageName: String): List<Class<*>> {
        val classes = ArrayList<Class<*>>()
        val src = ArrayList<File>()
        val srcFile = try {
            File(toURI())
        } catch (_: IllegalArgumentException) {
            File((openConnection() as JarURLConnection).jarFileURL.toURI())
        } catch (_: URISyntaxException) {
            File(path)
        }

        src += srcFile

        src.forEach { s ->
            JarFile(s).stream().filter { it.name.endsWith(".class") }.forEach second@ {
                val name = it.name.replace('/', '.').substring(0, it.name.length - 6)
                if (!name.contains(packageName)) return@second

                kotlin.runCatching {
                    classes.add(Class.forName(name, false, GenerateDocData::class.java.classLoader))
                }
            }
        }

        return classes
    }
}