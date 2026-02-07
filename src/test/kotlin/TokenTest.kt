import org.junit.Test
import tororo1066.displaymonitor.configuration.expression.evaluateRPN
import tororo1066.displaymonitor.configuration.expression.functions.DateFunction
import tororo1066.displaymonitor.configuration.expression.toRPN
import tororo1066.displaymonitor.configuration.expression.tokenize

class TokenTest {

    @Test
    fun testExpression() {
        val functions = mapOf(
            "date" to DateFunction()
        )

        val expression = " @date() < 1000 + 86400000 || (1 + 1 == 2 && 5 / 2 < 3)"
        val rpn = toRPN(tokenize(expression))
        val result = evaluateRPN(rpn, mapOf(), functions) {}
        println(rpn)
        println(result)
        assert(rpn.isNotEmpty())
    }
}