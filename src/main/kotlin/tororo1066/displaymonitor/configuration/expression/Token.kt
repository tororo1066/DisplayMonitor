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



//fun tokenize(input: String): List<Token> {
//    val tokens = mutableListOf<Token>()
//    // 正規表現
//    val regex = Regex("""\s*(@?[a-zA-Z_][a-zA-Z0-9_]*|[-+]?\d+(\.\d+)?|"([^"\\]|\\.)*"|\+|-|%|\*|/|\(|\)|,|==|!=|>|<|>=|<=|\s+|&&|\|\||#[0-9a-fA-F]{3,8})\s*""")
//    val rawTokens = regex.findAll(input).map { it.groupValues[1] }.toList()
//
//    var i = 0
//    while (i < rawTokens.size) {
//        val token = rawTokens[i]
//        when {
//            // クォート付き文字列
//            token.startsWith("\"") -> {
//                val content = token.drop(1).dropLast(1).replace("\\\"", "\"").replace("\\n", "\n")
//                tokens += Token.StringLiteral(content)
//            }
//            // 数値
//            token.matches(Regex("""-?\d+(\.\d+)?""")) -> {
//                tokens += Token.Number(token.toDouble())
//            }
//            // 関数 (@func)
//            token.startsWith("@") -> {
//                val name = token.substring(1)
//                val next = rawTokens.getOrNull(i + 1)
//                if (next == "(") {
//                    tokens += Token.Function(FuncMeta(name, 0))
//                } else {
//                    throw IllegalArgumentException("Invalid function call: $token")
//                }
//            }
//            // 演算子
//            token in listOf("+", "-", "*", "/", "%") -> tokens += Token.Operator(token)
//            token == "(" -> tokens += Token.LParen
//            token == ")" -> tokens += Token.RParen
//            token == "," -> tokens += Token.Comma
//            // 比較演算子
//            token in listOf("==", "!=", ">", "<", ">=", "<=") -> tokens += Token.Comparison(token)
//            // 論理演算子
//            token in listOf("&&", "||") -> tokens += Token.Logical(token)
//            // その他 → 変数 or 非クォート文字列 → StringLiteralとして解釈
//            else -> {
//                if (token.isNotEmpty()) {
//                    tokens += Token.StringLiteral(token)
//                }
//            }
//        }
//        i++
//    }
//
//    return tokens
//}

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
                pos++
            }
            c.isDigit() || c == '.' || (c == '-' && (tokens.isEmpty() || tokens.last() is Token.Operator || tokens.last() is Token.LParen)) -> {
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
    variables: Map<String, Any>
): Any {
    try {
        val expandExpr = expandVariablesRecursive(expr, variables)
        try {
            val tokens = tokenize(expandExpr)
            val rpn = toRPN(tokens)
            return evaluateRPN(rpn) { innerExpr ->
                // ネスト式（関数引数など）を再帰的に評価
                evalExpressionRecursive(innerExpr, variables)
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
    nestedEval: (String) -> Any
): Any {
    val stack = ArrayDeque<Any>()

    for (token in rpn) {
        when (token) {
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
                stack.push(formatNumber(func.eval(evaluatedArgs)))
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
//                    val expr = result.substring(i + 2, j - 1)
//                    val evaluatedKey = try {
//                        val key = evalExpressionRecursive(
//                            expr,
//                            parameters
//                        ).toString()
//                        parameters[key]?.toString() ?: "\${$expr}"
//                    } catch (_: Exception) {
//                        "\${$expr}"
//                    }

                    val expr = result.substring(i + 2, j - 1)
                    val parts = expr.split(":", limit = 2)
                    val key = parts[0]
                    val default = parts.getOrNull(1)

                    val evaluatedValue = try {
                        val evaluated = evalExpressionRecursive(key, parameters).toString()
                        val value = parameters[evaluated]
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
