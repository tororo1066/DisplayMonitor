package tororo1066.displaymonitor.configuration.expression.functions

import tororo1066.displaymonitor.configuration.expression.AbstractFunction
import tororo1066.displaymonitor.documentation.ParameterDoc
import kotlin.math.cos

class CosFunction: AbstractFunction("cos") {

    override fun getDescription(): String {
        return "与えられた数値のcosを返す。"
    }

    override fun getParameters(): List<ParameterDoc> {
        return listOf(
            ParameterDoc(
                name = "value",
                description = "数値(ラジアン)"
            )
        )
    }

    override fun eval(args: List<Any>, parameters: Map<String, Any>): Any {
        val value = args[0].toString().toDoubleOrNull()
            ?: throw IllegalArgumentException("Invalid argument for cos function: ${args[0]}")
        return cos(value)
    }
}