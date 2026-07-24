import org.junit.Test
import org.junit.Assert.assertEquals
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

    @Test
    fun testExpression2() {
        val expression = "(5 - 0) * -0.11 - 0.22"
        val rpn = toRPN(tokenize(expression))
        val result = evaluateRPN(rpn, mapOf(), mapOf()) {}
        println(rpn)
        println(result)
        assert(rpn.isNotEmpty())
        assertEquals(-0.77, (result as Number).toDouble(), 1e-9)
    }

    @Test
    fun testUnaryMinusWithWhitespace() {
        val expression = "5 * -0.11"
        val rpn = toRPN(tokenize(expression))
        val result = evaluateRPN(rpn, mapOf(), mapOf()) {}
        assertEquals(-0.55, (result as Number).toDouble(), 1e-9)
    }

    @Test
    fun testUnaryMinusNoLeadingZero() {
        val expression = "-.11 + 0"
        val rpn = toRPN(tokenize(expression))
        val result = evaluateRPN(rpn, mapOf(), mapOf()) {}
        assertEquals(-0.11, (result as Number).toDouble(), 1e-9)
    }
}