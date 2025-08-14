package tororo1066.displaymonitor.configuration.expression.functions

import tororo1066.displaymonitor.configuration.expression.AbstractFunction
import tororo1066.displaymonitor.documentation.ParameterDoc

class MaxFunction: AbstractFunction("max") {
    override fun getDescription(): String {
        return "与えられた数値の中で最大の値を返す。"
    }

    override fun getParameters(): List<ParameterDoc> {
        return listOf(
            ParameterDoc(
                name = "numbers",
                description = "数値のリスト。少なくとも1つの数値が必要。"
            )
        )
    }

    override fun eval(args: List<Any>, parameters: Map<String, Any>): Any {
        if (args.isEmpty()) {
            throw IllegalArgumentException("At least one argument is required for max function.")
        }
        return args.maxOf {
            when (it) {
                is Number -> it.toDouble()
                else -> throw IllegalArgumentException("All arguments must be numbers.")
            }
        }
    }

}