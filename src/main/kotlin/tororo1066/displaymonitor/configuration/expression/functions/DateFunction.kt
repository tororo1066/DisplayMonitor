package tororo1066.displaymonitor.configuration.expression.functions

import tororo1066.displaymonitor.configuration.expression.AbstractFunction
import tororo1066.displaymonitor.documentation.ParameterDoc

class DateFunction: AbstractFunction("date") {

    override fun getDescription(): String {
        return "現在の日付と時刻を数値で返す。"
    }

    override fun getParameters(): List<ParameterDoc> {
        return emptyList()
    }

    override fun eval(args: List<Any>, parameters: Map<String, Any>): Any {
        return System.currentTimeMillis()
    }
}