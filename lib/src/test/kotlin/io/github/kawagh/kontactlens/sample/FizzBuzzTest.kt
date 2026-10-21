package io.github.kawagh.kontactlens.sample

import kotlin.test.Test
import kotlin.test.assertEquals

// 15 の倍数をわざとテストしていない。n % 15 の変異が生き残り、"FizzBuzz" を返す行はテストが通らない。
class FizzBuzzTest {
    @Test fun one() {
        assertEquals("1", fizzBuzz(1))
    }

    @Test fun three() {
        assertEquals("Fizz", fizzBuzz(3))
    }

    @Test fun five() {
        assertEquals("Buzz", fizzBuzz(5))
    }
}
