package io.github.kawagh.kontactlens.sample;

import static io.github.kawagh.kontactlens.sample.FizzBuzz.fizzBuzz;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// 15 の倍数をわざとテストしていない。n % 15 の変異が生き残り、"FizzBuzz" を返す行はテストが通らない。
class FizzBuzzTest {
    @Test
    @DisplayName("1 はそのまま数字を返す")
    void one() {
        assertEquals("1", fizzBuzz(1));
    }

    @Test
    @DisplayName("3 は Fizz を返す")
    void three() {
        assertEquals("Fizz", fizzBuzz(3));
    }

    @Test
    @DisplayName("5 は Buzz を返す")
    void five() {
        assertEquals("Buzz", fizzBuzz(5));
    }
}
