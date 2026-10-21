package io.github.kawagh.kontactlens.sample

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

// 15 の倍数をわざとテストしていない。n % 15 の変異が生き残り、"FizzBuzz" を返す行はテストが通らない。
class FizzBuzzTest {
    @Test
    fun `1 はそのまま数字を返す`() {
        assertEquals("1", fizzBuzz(1))
    }

    @Test
    fun `3 は Fizz を返す`() {
        assertEquals("Fizz", fizzBuzz(3))
    }

    @Test
    fun `5 は Buzz を返す`() {
        assertEquals("Buzz", fizzBuzz(5))
    }
}
