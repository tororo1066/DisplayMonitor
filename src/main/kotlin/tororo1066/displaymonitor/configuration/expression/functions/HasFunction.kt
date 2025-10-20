package tororo1066.displaymonitor.configuration.expression.functions

import tororo1066.displaymonitor.configuration.expression.AbstractFunction
import tororo1066.displaymonitor.documentation.ParameterDoc

class HasFunction: AbstractFunction("has") {
    override fun getDescription(): String {
        return "変数に指定したキーの値が存在するかどうかを返す。"
    }

    override fun getParameters(): List<ParameterDoc> {
        return listOf(
            ParameterDoc(
                name = "key",
                description = "存在を確認するキー。",
                type = String::class
            )
        )
    }

    override fun eval(
        args: List<Any>,
        parameters: Map<String, Any>
    ): Any {
        return parameters.containsKey(args[0].toString()).toString()
    }
}