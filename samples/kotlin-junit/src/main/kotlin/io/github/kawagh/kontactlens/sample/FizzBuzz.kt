package io.github.kawagh.kontactlens.sample

/**
 * FizzBuzz の文字列を返す。
 *
 * 変異テストの観察用サンプル。
 */
@Suppress("MagicNumber") // 3、5、15 は FizzBuzz の規則そのものなので、定数にしない
fun fizzBuzz(n: Int): String {
    if (n % 15 == 0) return "FizzBuzz"
    if (n % 3 == 0) return "Fizz"
    if (n % 5 == 0) return "Buzz"
    return n.toString()
}
