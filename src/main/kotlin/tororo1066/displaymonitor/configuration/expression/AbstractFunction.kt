package tororo1066.displaymonitor.configuration.expression

import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitorapi.configuration.expression.IAbstractFunction

abstract class AbstractFunction(private val name: String): IAbstractFunction {
    override fun getName(): String {
        return name
    }

    abstract fun getDescription(): String

    abstract fun getParameters(): List<ParameterDoc>
}
