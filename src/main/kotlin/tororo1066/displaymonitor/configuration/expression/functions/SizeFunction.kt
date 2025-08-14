package tororo1066.displaymonitor.configuration.expression.functions

import tororo1066.displaymonitor.configuration.expression.AbstractFunction
import tororo1066.displaymonitor.documentation.ParameterDoc

class SizeFunction: AbstractFunction("size") {
    override fun getDescription(): String {
        return "リストまたはマップの要素数を返す。"
    }

    override fun getParameters(): List<ParameterDoc> {
        return listOf(
            ParameterDoc(
                name = "listOrMap",
                description = "要素数を取得するリストまたはマップ",
            )
        )
    }

    override fun eval(args: List<Any>, parameters: Map<String, Any>): Any {
        if (args.size != 1) {
            throw IllegalArgumentException("At least one argument is required for size function.")
        }
        val list = parameters[args[0].toString()]
        if (list == null) {
            throw IllegalArgumentException("The argument '${args[0]}' is not defined.")
        }
        return when (list) {
            is List<*> -> list.size
            is Map<*, *> -> list.size
            else -> throw IllegalArgumentException("The argument must be a list or a map.")
        }
    }


}