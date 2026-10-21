package io.github.kawagh.kontactlens

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlin.io.path.createDirectories
import kotlin.io.path.readText
import kotlin.io.path.writeText

private fun fizzBuzzMutation(status: MutationStatus = MutationStatus.SURVIVED) = Mutation(
    sourceFile = "FizzBuzz.kt",
    mutatedClass = "io.github.kawagh.kontactlens.sample.FizzBuzzKt",
    mutatedMethod = "fizzBuzz",
    methodDescription = "(I)Ljava/lang/String;",
    lineNumber = 2,
    mutator = "org.pitest.mutationtest.engine.gregor.mutators.MathMutator",
    description = "Replaced integer modulus with multiplication",
    status = status,
    detected = status == MutationStatus.KILLED,
    numberOfTestsRun = 3,
    indexes = listOf(5),
    blocks = listOf(0),
    killingTests = emptyList(),
    succeedingTests = emptyList(),
    coveringTests = emptyList(),
)

class HtmlReportTest : FunSpec({
    test("パッケージのディレクトリからソースを探し、変異後の行と一緒に埋め込む") {
        val root = tempdir().toPath()
        root.resolve("io/github/kawagh/kontactlens/sample").createDirectories().resolve("FizzBuzz.kt")
            .writeText("fun fizzBuzz(n: Int): String {\n    if (n % 15 == 0) return \"FizzBuzz\"\n}\n")

        val html = HtmlReport.render(listOf(fizzBuzzMutation()), listOf(root))

        html shouldContain "\"io/github/kawagh/kontactlens/sample/FizzBuzz.kt\":["
        html shouldContain "\"to\":\"    if (n * 15 == 0) return \\\"FizzBuzz\\\"\""
        html shouldContain "\"mutator\":\"MATH\""
        html shouldContain "\"status\":\"SURVIVED\""
    }

    test("ソースが見つからなければ、ソース無しで変異だけを埋め込む") {
        val html = HtmlReport.render(listOf(fizzBuzzMutation()), listOf(tempdir().toPath()))

        html shouldContain "const SOURCES = {};"
        html shouldContain "\"to\":null"
    }

    test("ソース中の </script> でページが壊れないようにエスケープする") {
        val root = tempdir().toPath()
        root.resolve("io/github/kawagh/kontactlens/sample").createDirectories().resolve("FizzBuzz.kt")
            .writeText("val html = \"</script><script>alert(1)</script>\"\n    if (n % 15 == 0) return \"FizzBuzz\"\n")

        val html = HtmlReport.render(listOf(fizzBuzzMutation()), listOf(root))

        // テンプレート自身の閉じタグ 1 つだけが残り、ソース中のものはエスケープされている
        html.split("</script>").size shouldBe 2
        html shouldContain "\\u003c/script\\u003e"
    }

    test("指定したファイルに書き出す") {
        val output = tempdir().toPath().resolve("out/report.html")

        HtmlReport.write(listOf(fizzBuzzMutation(MutationStatus.KILLED)), emptyList(), output)

        output.readText().startsWith("<!doctype html>") shouldBe true
    }
})
