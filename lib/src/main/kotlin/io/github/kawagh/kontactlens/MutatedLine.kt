package io.github.kawagh.kontactlens

/**
 * 変異後のソースの行を推定する。
 *
 * PIT はバイトコードを変異させるので、ソース上でどう変わったかは記録されない。
 * mutator の種類と PIT の説明文から、[line] のどこがどう変わったかを組み立てる。
 * 同じ演算子が 1 行に複数あるときは、最初のものが変わったとみなす。
 *
 * @param mutation 変異。
 * @param line 変異した行の、変異前のソース。
 * @return 変異後の行。推定できない mutator や、行に該当する箇所が見つからない場合は `null`。
 */
fun estimateMutatedLine(mutation: Mutation, line: String): String? = when (mutation.mutator.substringAfterLast('.')) {
    "MathMutator" -> replaceMathOperator(mutation.description, line)
    "NegateConditionalsMutator" -> replaceOperator(line, negated)
    "ConditionalsBoundaryMutator" -> replaceOperator(line, boundaryShifted)
    "BooleanTrueReturnValsMutator" -> replaceReturnValue(line, "true")
    "BooleanFalseReturnValsMutator" -> replaceReturnValue(line, "false")
    "PrimitiveReturnsMutator" -> replaceReturnValue(line, "0")
    "NullReturnValsMutator" -> replaceReturnValue(line, "null")
    "EmptyObjectReturnValsMutator" -> emptyValue(mutation)?.let { replaceReturnValue(line, it) }
    else -> null
}

private val negated = mapOf("==" to "!=", "!=" to "==", "<" to ">=", ">=" to "<", ">" to "<=", "<=" to ">")
private val boundaryShifted = mapOf("<" to "<=", "<=" to "<", ">" to ">=", ">=" to ">")

// PIT の MathMutator の説明文（例: "Replaced integer modulus with multiplication"）に出てくる演算の名前
private val mathOperators = mapOf(
    "addition" to "+",
    "subtraction" to "-",
    "multiplication" to "*",
    "division" to "/",
    "modulus" to "%",
)

private val mathDescription = Regex("""Replaced \w+ (\w+) with (\w+)""")

private fun replaceMathOperator(description: String, line: String): String? {
    val (from, to) = mathDescription.find(description)?.destructured ?: return null
    val fromOperator = mathOperators[from] ?: return null
    val toOperator = mathOperators[to] ?: return null
    return replaceOperator(line, mapOf(fromOperator to toOperator))
}

// 前後に空白がある演算子だけを対象にして、ジェネリクスの <> や ->、+= などと取り違えないようにする
private fun replaceOperator(line: String, replacements: Map<String, String>): String? {
    val operators = replacements.keys.sortedByDescending { it.length }.joinToString("|") { Regex.escape(it) }
    val match = Regex("""(?<=\s)($operators)(?=\s)""").find(line) ?: return null
    return line.replaceRange(match.range, replacements.getValue(match.value))
}

// "return 式" の式の部分を置き換える。Java の末尾の ; は残す
private val returnStatement = Regex("""\breturn\s+(.+?)(\s*;)?\s*$""")

private fun replaceReturnValue(line: String, value: String): String? {
    val match = returnStatement.find(line) ?: return null
    val expression = checkNotNull(match.groups[1])
    return line.replaceRange(expression.range, value)
}

// 説明文（例: replaced return value with "" for ...）から、代わりに返す値を取り出す
private val emptyValueDescription = Regex("""replaced return value with (.+?) for """)

private fun emptyValue(mutation: Mutation): String? {
    val value = emptyValueDescription.find(mutation.description)?.groupValues?.get(1) ?: return null
    // Collections.emptyList のようなメソッド名は、呼び出しの形にする
    return if (value.matches(Regex("""[\w.]+""")) && value.first().isUpperCase()) "$value()" else value
}
