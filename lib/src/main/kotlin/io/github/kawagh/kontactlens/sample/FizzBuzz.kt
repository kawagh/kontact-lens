package io.github.kawagh.kontactlens.sample

/**
 * FizzBuzz の文字列を返す。
 *
 * 変異テストの観察用サンプル。
 */
fun fizzBuzz(n: Int): String {
    if (n % 15 == 0) return "FizzBuzz"
    if (n % 3 == 0) return "Fizz"
    if (n % 5 == 0) return "Buzz"
    return n.toString()
}
