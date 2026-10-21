package io.github.kawagh.kontactlens

/**
 * レポートに表示する、mutator の短い名前と説明。
 *
 * @property name PIT の設定で使う mutator の名前（例: `MATH`）。対応表に無い mutator では、クラス名から `Mutator` を除いたもの。
 * @property label 変異の内容を表す短い説明（例: `演算子を入れ替える`）。対応表に無い mutator では空文字列。
 */
data class MutatorInfo(val name: String, val label: String) {
    companion object {
        // PIT のデフォルトの mutator のクラス名（パッケージを除く）と、表示する名前・説明の対応
        private val known = mapOf(
            "ConditionalsBoundaryMutator" to MutatorInfo("CONDITIONALS_BOUNDARY", "比較の境界をずらす"),
            "NegateConditionalsMutator" to MutatorInfo("NEGATE_CONDITIONALS", "条件を反転する"),
            "MathMutator" to MutatorInfo("MATH", "演算子を入れ替える"),
            "IncrementsMutator" to MutatorInfo("INCREMENTS", "増減を逆にする"),
            "InvertNegsMutator" to MutatorInfo("INVERT_NEGS", "符号を反転する"),
            "VoidMethodCallMutator" to MutatorInfo("VOID_METHOD_CALLS", "メソッド呼び出しを消す"),
            "BooleanTrueReturnValsMutator" to MutatorInfo("TRUE_RETURNS", "常に true を返す"),
            "BooleanFalseReturnValsMutator" to MutatorInfo("FALSE_RETURNS", "常に false を返す"),
            "PrimitiveReturnsMutator" to MutatorInfo("PRIMITIVE_RETURNS", "0 を返す"),
            "EmptyObjectReturnValsMutator" to MutatorInfo("EMPTY_RETURNS", "空の値を返す"),
            "NullReturnValsMutator" to MutatorInfo("NULL_RETURNS", "null を返す"),
        )

        /** [mutatorClass]（PIT の mutator の完全修飾クラス名）に対応する表示用の情報を返す。 */
        fun of(mutatorClass: String): MutatorInfo {
            val simpleName = mutatorClass.substringAfterLast('.')
            return known[simpleName] ?: MutatorInfo(simpleName.removeSuffix("Mutator"), "")
        }
    }
}
