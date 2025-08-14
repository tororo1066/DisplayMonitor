package tororo1066.displaymonitor.configuration.expression

import tororo1066.displaymonitor.storage.FunctionStorage
import java.util.ArrayDeque

sealed class Token {
    data class StringLiteral(val value: String) : Token()
    data class Number(val value: Double) : Token()
    data class Function(val meta: FuncMeta) : Token()
    object LParen : Token()
    object RParen : Token()
    object Comma : Token()
    data class Whitespace(val count: Int = 1) : Token()

    sealed class Symbolic(open val symbol: String) : Token()
    data class Operator(override val symbol: String) : Symbolic(symbol)
    data class Comparison(override val symbol: String) : Symbolic(symbol)
    data class Logical(override val symbol: String) : Symbolic(symbol)
}

data class FuncMeta(val name: String, val argCount: Int)

private val precedence = mapOf(
    "+" to 2, "-" to 2, "*" to 3, "/" to 3, "%" to 3,
    "==" to 2, "!=" to 2, ">" to 2, "<" to 2, ">=" to 2, "<=" to 2,
    "&&" to 1, "||" to 1
)

fun tokenize(input: String): List<Token> {
    var pos = 0
    val tokens = mutableListOf<Token>()

    fun addStringLiteral(string: String) {
        if (string.isNotEmpty()) {
            val last = tokens.lastOrNull()
            if (last is Token.StringLiteral) {
                tokens[tokens.size - 1] = Token.StringLiteral(last.value + string)
            } else {
                tokens += Token.StringLiteral(string)
            }
        } else {
            throw IllegalArgumentException("Empty string literal at position $pos")
        }
    }

    while (pos < input.length) {
        val c = input[pos]
        when {
            c.isWhitespace() -> {
                var count = 0
                while (pos < input.length && input[pos].isWhitespace()) {
                    count++
                    pos++
                }
                tokens += Token.Whitespace(count)
            }
            c.isDigit() || c == '.' || (c == '-' && (tokens.isEmpty() || tokens.last() is Token.Operator || tokens.last() is Token.LParen || tokens.last() is Token.Comma)) -> {
                val start = pos
                if (c == '-' && pos + 1 < input.length && input[pos + 1].isDigit()) {
                    pos++ // Skip '-' for negative numbers
                }
                while (pos < input.length && (input[pos].isDigit() || input[pos] == '.')) {
                    pos++
                }
                if (c == '.' && start == pos - 1) {
                    throw IllegalArgumentException("Invalid number format at position $start")
                }
                val next = input.getOrNull(pos)
                if (next?.isLetter() == true) {
                    // 文字列として扱う
                    while (pos < input.length && (input[pos].isLetterOrDigit() || input[pos] == '_')) {
                        pos++
                    }
                    val identifier = input.substring(start, pos)
                    addStringLiteral(identifier)
                } else {
                    val numberString = input.substring(start, pos)
                    tokens += Token.Number(numberString.toDouble())
                }

            }
            c == '@' -> {
                pos++ // Skip '@'
                val start = pos
                while (pos < input.length && (input[pos].isLetterOrDigit() || input[pos] == '_')) {
                    pos++
                }
                if (start == pos) {
                    throw IllegalArgumentException("Invalid function name at position $start")
                }
                val functionName = input.substring(start, pos)
                tokens += Token.Function(FuncMeta(functionName, 0)) // 引数数は0で初期化
            }
            c == '"' -> {
                pos++ // Skip opening quote
                val start = pos
                while (pos < input.length && input[pos] != '"') {
                    if (input[pos] == '\\' && pos + 1 < input.length) {
                        pos += 2 // Skip escaped character
                    } else {
                        pos++
                    }
                }
                if (pos >= input.length || input[pos] != '"') {
                    throw IllegalArgumentException("Unmatched quotes in string literal")
                }
                val stringValue = input.substring(start, pos)
                addStringLiteral(stringValue.replace("\\\"", "\"").replace("\\n", "\n"))
                pos++ // Skip closing quote
            }
            c == '(' -> {
                tokens += Token.LParen
                pos++
            }
            c == ')' -> {
                tokens += Token.RParen
                pos++
            }
            c == ',' -> {
                tokens += Token.Comma
                pos++
            }
            c in listOf('+', '-', '*', '/', '%') -> {
                if (c != '+' && (tokens.isEmpty() || tokens.last() is Token.StringLiteral)) {
                    addStringLiteral(c.toString())
                } else {
                    tokens += Token.Operator(c.toString())
                }
                pos++ // Skip operator
            }
            c in listOf('=', '>', '<', '!') -> {
                val next = input.getOrNull(pos + 1)
                if (next == '=') {
                    tokens += Token.Comparison(c.toString() + next)
                    pos += 2 // Skip '==', '!=', '>=', '<='
                } else {
                    if (c !in listOf('=', '!')) {
                        tokens += Token.Comparison(c.toString())
                        pos++ // Skip single comparison operator
                    } else {
                        throw IllegalArgumentException("Invalid comparison operator: $c")
                    }
                }
            }
            c in listOf('&', '|') -> {
                val next = input.getOrNull(pos + 1)
                if (next == c) {
                    tokens += Token.Logical(c.toString() + next)
                    pos += 2 // Skip '&&' or '||'
                } else {
                    addStringLiteral(c.toString())
                    pos++ // Skip single logical operator
                }
            }
            else -> {
                val start = pos
                while (pos < input.length && (input[pos].isLetterOrDigit() || input[pos] == '_')) {
                    pos++
                }
                val identifier = input.substring(start, pos)
                addStringLiteral(identifier)
            }
        }
    }

    return tokens
}

fun toRPN(tokens: List<Token>): List<Token> {
    val output = mutableListOf<Token>()
    val stack = ArrayDeque<Token>()
    val functionArgCount = ArrayDeque<Int>()

    var i = 0
    while (i < tokens.size) {
        when (val token = tokens[i]) {
            is Token.Whitespace -> {
                // 空白は無視
            }
            is Token.Number, is Token.StringLiteral -> output += token
            is Token.Function -> {
                stack.push(token)
                functionArgCount.push(1) // 1引数目開始
            }

            Token.Comma -> {
                while (stack.isNotEmpty() && stack.peek() !is Token.LParen) {
                    output += stack.pop()
                }
                functionArgCount.push(functionArgCount.pop() + 1)
            }

            Token.LParen -> stack.push(token)

            Token.RParen -> {
                while (stack.isNotEmpty() && stack.peek() !is Token.LParen) {
                    output += stack.pop()
                }
                if (stack.isEmpty() || stack.pop() !is Token.LParen) {
                    throw IllegalArgumentException("Unmatched parentheses")
                }
                // 関数なら出す
                if (stack.isNotEmpty() && stack.peek() is Token.Function) {
                    val func = stack.pop() as Token.Function
                    output += Token.Function(func.meta.copy(argCount = functionArgCount.pop()))
                }
            }

            is Token.Operator -> {
                while (stack.isNotEmpty()) {
                    val top = stack.peek()
                    if (top is Token.Operator &&
                        (precedence[top.symbol] ?: 0) >= (precedence[token.symbol] ?: 0)
                    ) {
                        output += stack.pop()
                    } else break
                }
                stack.push(token)
            }

            is Token.Comparison -> {
                while (stack.isNotEmpty()) {
                    val top = stack.peek()
                    if (top is Token.Comparison &&
                        (precedence[top.symbol] ?: 0) >= (precedence[token.symbol] ?: 0)
                    ) {
                        output += stack.pop()
                    } else break
                }
                stack.push(token)
            }

            is Token.Logical -> {
                while (stack.isNotEmpty()) {
                    val top = stack.peek()
                    if (top is Token.Symbolic && (top is Token.Logical || top is Token.Comparison) &&
                        (precedence[top.symbol] ?: 0) >= (precedence[token.symbol] ?: 0)
                    ) {
                        output += stack.pop()
                    } else break
                }
                stack.push(token)
            }

        }
        i++
    }

    while (stack.isNotEmpty()) {
        val top = stack.pop()
        if (top is Token.LParen || top is Token.RParen) {
            throw IllegalArgumentException("Mismatched parentheses")
        }
        output += top
    }

    return output
}

private fun asNumber(value: Any): Double {
    return when (value) {
        is Number -> value.toDouble()
        is String -> value.toDoubleOrNull()
            ?: throw IllegalArgumentException("Invalid numeric value: $value")
        else -> throw IllegalArgumentException("Cannot convert to number: $value")
    }
}

private fun asBoolean(value: Any): Boolean {
    return when (value) {
        is Boolean -> value
        is String -> value.toBoolean()
        else -> throw IllegalArgumentException("Cannot convert to boolean: $value")
    }
}


fun evalExpressionRecursive(
    expr: String,
    parameters: Map<String, Any>
): Any {
    try {
        val expandExpr = expandVariablesRecursive(expr, parameters)
        try {
            val tokens = tokenize(expandExpr)
            val rpn = toRPN(tokens)
            return evaluateRPN(rpn, parameters) { innerExpr ->
                // ネスト式（関数引数など）を再帰的に評価
                evalExpressionRecursive(innerExpr, parameters)
            }
        } catch (_: Exception) {
            return expandExpr
        }
    } catch (_: Exception) {
        return expr
    }
}

private fun formatNumber(value: Any): Any {
    return if (value is Double && value % 1.0 == 0.0) {
        value.toInt()
    } else {
        value
    }
}

fun evaluateRPN(
    rpn: List<Token>,
    parameters: Map<String, Any>,
    nestedEval: (String) -> Any
): Any {
    val stack = ArrayDeque<Any>()

    for (token in rpn) {
        when (token) {
            is Token.Whitespace -> {
                // 空白は無視
            }
            is Token.Number -> {
                stack.push(formatNumber(token.value))
            }
            is Token.StringLiteral -> stack.push(token.value)
            is Token.Function -> {
                val args = (0 until token.meta.argCount).map { stack.pop() }.reversed()
                val func = FunctionStorage.functions[token.meta.name]
                    ?: throw IllegalArgumentException("Unknown function: ${token.meta.name}")
                val evaluatedArgs = args.map {
                    if (it is String) {
                        if (it.contains(' ') || it.startsWith("@")) {
                            return@map nestedEval(it)
                        }
                    }
                    it
                }
                stack.push(formatNumber(func.eval(evaluatedArgs, parameters)))
            }
            is Token.Operator -> {
                val b = stack.pop()
                val a = stack.pop()
                val result = when (token.symbol) {
                    "+" -> if (a is String || b is String) a.toString() + b.toString()
                    else asNumber(a) + asNumber(b)
                    "-" -> asNumber(a) - asNumber(b)
                    "*" -> asNumber(a) * asNumber(b)
                    "/" -> asNumber(a) / asNumber(b)
                    "%" -> asNumber(a) % asNumber(b)
                    else -> throw IllegalArgumentException("Unknown operator: ${token.symbol}")
                }
                stack.push(formatNumber(result))
            }
            is Token.Comparison -> {
                val b = stack.pop()
                val a = stack.pop()
                val result = when (token.symbol) {
                    "==" -> a == b
                    "!=" -> a != b
                    ">" -> asNumber(a) > asNumber(b)
                    "<" -> asNumber(a) < asNumber(b)
                    ">=" -> asNumber(a) >= asNumber(b)
                    "<=" -> asNumber(a) <= asNumber(b)
                    else -> throw IllegalArgumentException("Unknown comparison operator: ${token.symbol}")
                }
                stack.push(result)
            }
            is Token.Logical -> {
                val b = stack.pop()
                val a = stack.pop()
                val result = when (token.symbol) {
                    "&&" -> asBoolean(a) && asBoolean(b)
                    "||" -> asBoolean(a) || asBoolean(b)
                    else -> throw IllegalArgumentException("Unknown logical operator: ${token.symbol}")
                }
                stack.push(result)
            }
            else -> error("Unsupported token: $token")
        }
    }

    return stack.singleOrNull() ?: throw IllegalStateException("Invalid expression")
}

fun getParametersValue(
    key: String,
    parameters: Map<String, Any>
): Any? {
    val regex = Regex("""\b([a-zA-Z_][a-zA-Z0-9_]*)((\[[^\[\]]+])+)""")
    val bracketContentRegex = Regex("""\[([^\[\]]+)]""")

    val matchResult = regex.find(key) ?: return parameters[key]
    val baseKey = matchResult.groupValues[1]
    val brackets = matchResult.groupValues[2]

    if (brackets.count { it == '[' } != brackets.count { it == ']' }) {
        throw IllegalArgumentException("Mismatched brackets in key: $key")
    }

    val nestedKeys = bracketContentRegex.findAll(brackets).map { it.groupValues[1] }.toList()

    var currentValue: Any? = parameters[baseKey]
    for (nestedKey in nestedKeys) {
        val evaluatedKey = evalExpressionRecursive(nestedKey, parameters).toString()

        when (currentValue) {
            is Map<*, *> -> {
                currentValue = currentValue[evaluatedKey]
            }
            is List<*> -> {
                val index = evaluatedKey.toIntOrNull()
                if (index != null && index in currentValue.indices) {
                    currentValue = currentValue[index]
                } else {
                    throw IllegalArgumentException("Invalid index $nestedKey for list: $key")
                }
            }
            else -> {
                throw IllegalArgumentException("Cannot access nested key '$nestedKey' in value of type '${currentValue?.javaClass?.simpleName}' for key: $key")
            }
        }
    }

    return currentValue
}

fun expandVariablesRecursive(
    input: String,
    parameters: Map<String, Any>,
    maxDepth: Int = 10
): String {
    var result = input

    repeat(maxDepth) {
        val sb = StringBuilder()
        var i = 0

        while (i < result.length) {
            if (result.startsWith("\${", i)) {
                var braceCount = 1
                var j = i + 2
                while (j < result.length && braceCount > 0) {
                    if (result.startsWith("\${", j)) {
                        braceCount++
                        j += 2
                    } else if (result[j] == '}') {
                        braceCount--
                        j++
                    } else {
                        j++
                    }
                }

                if (braceCount == 0) {

                    val expr = result.substring(i + 2, j - 1)
                    val parts = expr.split(":", limit = 2)
                    val key = parts[0]
                    val default = parts.getOrNull(1)

                    val evaluatedValue = try {
                        val evaluated = evalExpressionRecursive(key, parameters).toString()
                        val value = getParametersValue(evaluated, parameters)
                        if (value != null) {
                            value.toString()
                        } else {
                            val evaluatedDefault = default?.let { evalExpressionRecursive(it, parameters) }
                            evaluatedDefault?.toString() ?: "\${$expr}"
                        }
                    } catch (_: Exception) {
                        "\${$expr}"
                    }

                    sb.append(evaluatedValue)
                    i = j
                } else {
                    sb.append(result[i])
                    i++
                }
            } else {
                sb.append(result[i])
                i++
            }
        }

        val newResult = sb.toString()
        if (newResult == result) return result
        result = newResult
    }

    return result
}
