package tororo1066.displaymonitor.configuration.expression.functions

import tororo1066.displaymonitor.configuration.expression.AbstractFunction
import tororo1066.displaymonitor.documentation.ParameterDoc

class KeyFunction: AbstractFunction("key") {
    override fun getDescription(): String {
        return "マップのエントリのキーを取得する。"
    }

    override fun getParameters(): List<ParameterDoc> {
        return listOf(
            ParameterDoc(
                name = "map",
                description = "キーを取得したいエントリの変数名。"
            )
        )
    }

    override fun eval(
        args: List<Any>,
        parameters: Map<String, Any>
    ): Any {
        if (args.size != 1) {
            throw IllegalArgumentException("At least one argument is required for key function.")
        }
        val entry = parameters[args[0].toString()] as? Map.Entry<*, *>
            ?: throw IllegalArgumentException("The argument '${args[0]}' is not defined.")

        return entry.key ?: ""
    }


}