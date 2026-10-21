package io.github.kawagh.kontactlens.sample

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

// 15 の倍数をわざとテストしていない。n % 15 の変異が生き残り、"FizzBuzz" を返す行はテストが通らない。
class FizzBuzzTest :
    FunSpec({
        test("1 はそのまま数字を返す") {
            fizzBuzz(1) shouldBe "1"
        }

        test("3 は Fizz を返す") {
            fizzBuzz(3) shouldBe "Fizz"
        }

        test("5 は Buzz を返す") {
            fizzBuzz(5) shouldBe "Buzz"
        }
    })
