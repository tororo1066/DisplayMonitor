package tororo1066.displaymonitor.configuration.expression.functions

import tororo1066.displaymonitor.configuration.expression.AbstractFunction
import tororo1066.displaymonitor.documentation.ParameterDoc

class RandomFunction: AbstractFunction("random") {
    override fun getDescription(): String {
        return "指定された範囲内のランダムな整数を返す。"
    }

    override fun getParameters(): List<ParameterDoc> {
        return listOf(
            ParameterDoc(
                name = "min",
                description = "ランダムな整数の最小値。"
            ),
            ParameterDoc(
                name = "max",
                description = "ランダムな整数の最大値。"
            )
        )
    }

    override fun eval(
        args: List<Any>,
        parameters: Map<String, Any>
    ): Any {
        if (args.size != 2) {
            throw IllegalArgumentException("Two arguments are required for random function.")
        }
        val min = args[0].toString().toIntOrNull()
        val max = args[1].toString().toIntOrNull()
        if (min == null || max == null) {
            throw IllegalArgumentException("Both arguments must be valid integers.")
        }
        if (min == max) {
            return min
        }
        if (min > max) {
            throw IllegalArgumentException("The first argument must be less than the second argument.")
        }
        return (min..max).random()
    }
}