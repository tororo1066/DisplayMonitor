package tororo1066.displaymonitor.configuration.expression.functions

import tororo1066.displaymonitor.configuration.expression.AbstractFunction
import tororo1066.displaymonitor.documentation.ParameterDoc
import kotlin.math.floor

class FloorFunction: AbstractFunction("floor") {

    override fun getDescription(): String {
        return "与えられた数値の小数点以下を切り捨てた値を返す。"
    }

    override fun getParameters(): List<ParameterDoc> {
        return listOf(
            ParameterDoc(
                name = "number",
                description = "切り捨てる対象の数値。"
            )
        )
    }

    override fun eval(args: List<Any>): Any {
        return floor((args[0] as Number).toDouble())
    }
}