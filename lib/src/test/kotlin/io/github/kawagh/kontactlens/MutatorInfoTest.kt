package io.github.kawagh.kontactlens

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MutatorInfoTest :
    FunSpec({
        test("PIT のデフォルトの mutator は、設定で使う名前と説明にする") {
            MutatorInfo.of(
                "org.pitest.mutationtest.engine.gregor.mutators.returns.EmptyObjectReturnValsMutator",
            ) shouldBe
                MutatorInfo("EMPTY_RETURNS", "空の値を返す")
        }

        test("知らない mutator は、クラス名から Mutator を除いた名前にする") {
            MutatorInfo.of("com.example.mutators.SwapArgumentsMutator") shouldBe MutatorInfo("SwapArguments", "")
        }
    })
