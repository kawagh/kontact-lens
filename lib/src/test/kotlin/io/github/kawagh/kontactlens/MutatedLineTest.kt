package io.github.kawagh.kontactlens

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

private const val MUTATORS = "org.pitest.mutationtest.engine.gregor.mutators"

private fun mutation(mutator: String, description: String = "") = Mutation(
    sourceFile = "A.kt",
    mutatedClass = "A",
    mutatedMethod = "f",
    methodDescription = "()V",
    lineNumber = 1,
    mutator = "$MUTATORS.$mutator",
    description = description,
    status = MutationStatus.KILLED,
    detected = true,
    numberOfTestsRun = 1,
    indexes = emptyList(),
    blocks = emptyList(),
    killingTests = emptyList(),
    succeedingTests = emptyList(),
    coveringTests = emptyList(),
)

class MutatedLineTest : FunSpec({
    context("MATH") {
        test("説明文に書かれた演算子に入れ替える") {
            val math = mutation("MathMutator", "Replaced integer modulus with multiplication")
            estimateMutatedLine(math, """    if (n % 15 == 0) return "FizzBuzz"""") shouldBe """    if (n * 15 == 0) return "FizzBuzz""""
        }

        test("前後に空白の無い演算子（+= など）は対象にしない") {
            val math = mutation("MathMutator", "Replaced integer addition with subtraction")
            estimateMutatedLine(math, "    total += n").shouldBeNull()
        }

        test("説明文が読めなければ推定しない") {
            estimateMutatedLine(mutation("MathMutator", "Replaced bitwise AND with OR"), "a & b").shouldBeNull()
        }
    }

    context("NEGATE_CONDITIONALS") {
        test("== を != にする") {
            estimateMutatedLine(mutation("NegateConditionalsMutator"), "if (n % 3 == 0) return") shouldBe "if (n % 3 != 0) return"
        }

        test(">= は < にする") {
            estimateMutatedLine(mutation("NegateConditionalsMutator"), "return age >= 18") shouldBe "return age < 18"
        }

        test("比較が無ければ推定しない") {
            estimateMutatedLine(mutation("NegateConditionalsMutator"), "if (isAdult) return").shouldBeNull()
        }
    }

    test("CONDITIONALS_BOUNDARY は境界をずらす") {
        estimateMutatedLine(mutation("ConditionalsBoundaryMutator"), "return age >= 18") shouldBe "return age > 18"
    }

    context("戻り値の変異") {
        test("TRUE_RETURNS は true を返す") {
            estimateMutatedLine(mutation("returns.BooleanTrueReturnValsMutator"), "    return age >= 18") shouldBe "    return true"
        }

        test("Java の末尾の ; は残す") {
            estimateMutatedLine(mutation("returns.PrimitiveReturnsMutator"), "return a + b;") shouldBe "return 0;"
        }

        test("EMPTY_RETURNS は説明文に書かれた空の値を返す") {
            val empty = mutation("returns.EmptyObjectReturnValsMutator", "replaced return value with \"\" for A::f")
            estimateMutatedLine(empty, """if (n % 3 == 0) return "Fizz";""") shouldBe """if (n % 3 == 0) return "";"""
        }

        test("EMPTY_RETURNS のメソッド名は呼び出しの形にする") {
            val empty = mutation("returns.EmptyObjectReturnValsMutator", "replaced return value with Collections.emptyList for A::f")
            estimateMutatedLine(empty, "return items;") shouldBe "return Collections.emptyList();"
        }

        test("return が無ければ推定しない（Kotlin の式本体の関数など）") {
            estimateMutatedLine(mutation("returns.BooleanTrueReturnValsMutator"), "fun isPositive(x: Int) = x > 0").shouldBeNull()
        }
    }

    test("対応していない mutator は推定しない") {
        estimateMutatedLine(mutation("VoidMethodCallMutator"), "println(n)").shouldBeNull()
    }
})
