package io.github.kawagh.kontactlens.sample;

/**
 * FizzBuzz の文字列を返す。
 *
 * <p>変異テストの観察用サンプル。Kotlin 版と同じロジックにして、変異を見比べられるようにしている。
 */
public final class FizzBuzz {
    private FizzBuzz() {}

    public static String fizzBuzz(int n) {
        if (n % 15 == 0) return "FizzBuzz";
        if (n % 3 == 0) return "Fizz";
        if (n % 5 == 0) return "Buzz";
        return String.valueOf(n);
    }
}
