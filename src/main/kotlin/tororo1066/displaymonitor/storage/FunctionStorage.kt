package tororo1066.displaymonitor.storage

import tororo1066.displaymonitor.Utils.getClasses
import tororo1066.displaymonitorapi.configuration.expression.IAbstractFunction
import tororo1066.displaymonitorapi.storage.IFunctionStorage
import tororo1066.tororopluginapi.SJavaPlugin

object FunctionStorage: IFunctionStorage {

    val functions = mutableMapOf<String, IAbstractFunction>()

    init {
        SJavaPlugin.plugin.javaClass.protectionDomain.codeSource.location.getClasses("tororo1066.displaymonitor.configuration.expression.functions").forEach { clazz ->
            if (IAbstractFunction::class.java.isAssignableFrom(clazz)) {
                val constructor = clazz.getConstructor()
                val function = constructor.newInstance() as IAbstractFunction
                functions[function.name] = function
            }
        }
    }

    override fun addFunction(function: IAbstractFunction) {
        functions[function.name] = function
    }
}