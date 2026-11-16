package tororo1066.displaymonitor.documentation.generation

import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitorapi.elements.Settable
import java.lang.reflect.Field

object ConfigurationKeyResolver {
    fun path(field: Field, parameterDoc: ParameterDoc?): List<String> {
        val settable = field.getAnnotation(Settable::class.java)
        val key = when {
            settable != null -> settable.name.ifEmpty { field.name }
            parameterDoc?.name?.isNotEmpty() == true -> parameterDoc.name
            else -> field.name
        }
        return key.split('.').filter(String::isNotEmpty)
    }
}
