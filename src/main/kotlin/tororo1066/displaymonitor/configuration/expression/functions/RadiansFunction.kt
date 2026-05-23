package tororo1066.displaymonitor.configuration.expression.functions

import tororo1066.displaymonitor.configuration.expression.AbstractFunction
import tororo1066.displaymonitor.documentation.ParameterDoc

class RadiansFunction: AbstractFunction("radians") {

    override fun getDescription(): String {
        return "与えられた数値のradiansを返す。"
    }

    override fun getParameters(): List<ParameterDoc> {
        return listOf(
            ParameterDoc(
                name = "value",
                description = "数値(度)"
            )
        )
    }

    override fun eval(args: List<Any>, parameters: Map<String, Any>): Any {
        val value = args[0].toString().toDoubleOrNull()
            ?: throw IllegalArgumentException("Invalid argument for radians function: ${args[0]}")
        return Math.toRadians(value)
    }
}