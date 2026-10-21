package io.github.kawagh.kontactlens

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotContain
import java.nio.file.Path
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText

// テスト用の XML は、samples/kotlin-kotest の FizzBuzz と、gradle init が生成した Library（現在は削除済み）に
// PIT 1.30.1 を実行して得たもの
private fun readResource(name: String): List<Mutation> =
    checkNotNull(PitXmlReportTest::class.java.getResourceAsStream("/pit/$name")) { "$name がありません" }
        .use(PitXmlReport::read)

private fun readXml(xml: String): List<Mutation> = PitXmlReport.read(xml.byteInputStream())

private const val FIZZ_BUZZ_TEST = "io.github.kawagh.kontactlens.sample.FizzBuzzTest"

// Kotest の PIT プラグインが出力するテストケースの ID
private fun fizzBuzzTestCase(name: String) = "$FIZZ_BUZZ_TEST.$FIZZ_BUZZ_TEST/$name"

class PitXmlReportTest :
    FunSpec({
        test("レポートのすべての変異を書かれた順に読む") {
            val mutations = readResource("mutations.xml")

            mutations shouldHaveSize 11
            mutations.map { it.sourceFile } shouldBe List(10) { "FizzBuzz.kt" } + "Library.kt"
            mutations.groupingBy { it.status }.eachCount() shouldBe
                mapOf(MutationStatus.KILLED to 9, MutationStatus.SURVIVED to 1, MutationStatus.NO_COVERAGE to 1)
        }

        test("Path から読める") {
            val path = Path.of(checkNotNull(PitXmlReportTest::class.java.getResource("/pit/mutations.xml")).toURI())

            PitXmlReport.read(path) shouldBe readResource("mutations.xml")
        }

        test("SURVIVED の変異のすべての項目を読む") {
            val survived = readResource("mutations.xml").single { it.status == MutationStatus.SURVIVED }

            survived shouldBe Mutation(
                sourceFile = "FizzBuzz.kt",
                mutatedClass = "io.github.kawagh.kontactlens.sample.FizzBuzzKt",
                mutatedMethod = "fizzBuzz",
                methodDescription = "(I)Ljava/lang/String;",
                lineNumber = 9,
                mutator = "org.pitest.mutationtest.engine.gregor.mutators.MathMutator",
                description = "Replaced integer modulus with multiplication",
                status = MutationStatus.SURVIVED,
                detected = false,
                numberOfTestsRun = 3,
                indexes = listOf(5),
                blocks = listOf(0),
                killingTests = emptyList(),
                succeedingTests = emptyList(),
                coveringTests = emptyList(),
            )
        }

        test("fullMutationMatrix なしでは、検出したテストを 1 件だけ読む") {
            val killed = readResource("mutations.xml").first { it.status == MutationStatus.KILLED }

            killed.detected shouldBe true
            killed.numberOfTestsRun shouldBe 3
            killed.killingTests shouldBe listOf(fizzBuzzTestCase("3 は Fizz を返す"))
        }

        test("fullMutationMatrix ありでは、| 区切りのテストを分けて読む") {
            val survived = readResource("mutations-full-matrix.xml").single { it.status == MutationStatus.SURVIVED }

            survived.killingTests.shouldBeEmpty()
            survived.succeedingTests shouldBe listOf(
                fizzBuzzTestCase("1 はそのまま数字を返す"),
                fizzBuzzTestCase("3 は Fizz を返す"),
                fizzBuzzTestCase("5 は Buzz を返す"),
            )
            // Kotest の PIT プラグインは、カバレッジをテストケースではなく Spec 単位で記録する
            survived.coveringTests shouldBe listOf(FIZZ_BUZZ_TEST)
        }

        test("NO_COVERAGE の変異は実行したテストが無い") {
            val noCoverage = readResource(
                "mutations-full-matrix.xml",
            ).single { it.status == MutationStatus.NO_COVERAGE }

            noCoverage.detected shouldBe false
            noCoverage.numberOfTestsRun shouldBe 0
            noCoverage.coveringTests.shouldBeEmpty()
        }

        test("複数の index と block を読む") {
            val mutation = readXml(
                """
            <mutations>
            <mutation detected='true' status='KILLED' numberOfTestsRun='1'><sourceFile>A.kt</sourceFile><mutatedClass>A</mutatedClass><mutatedMethod>f</mutatedMethod><methodDescription>()V</methodDescription><lineNumber>3</lineNumber><mutator>M</mutator><indexes><index>2</index><index>7</index></indexes><blocks><block>0</block><block>1</block></blocks><killingTest>T</killingTest><description>d</description></mutation>
            </mutations>
                """.trimIndent(),
            ).single()

            mutation.indexes shouldBe listOf(2, 7)
            mutation.blocks shouldBe listOf(0, 1)
        }

        test("変異が無いレポートは空の一覧になる") {
            readXml("<mutations/>").shouldBeEmpty()
        }

        test("必須の要素が無ければ例外を投げる") {
            val error = shouldThrow<IllegalArgumentException> {
                readXml(
                    "<mutations><mutation detected='true' status='KILLED'>" +
                        "<sourceFile>A.kt</sourceFile></mutation></mutations>",
                )
            }
            error.message shouldBe "<mutation> に <mutatedClass> がありません"
        }

        test("外部エンティティを解決しない") {
            val secret = createTempFile().apply { writeText("SECRET") }
            try {
                val xml =
                    """<?xml version="1.0"?><!DOCTYPE mutations [<!ENTITY xxe SYSTEM "${secret.toUri()}">]>""" +
                        "<mutations><mutation detected='true' status='KILLED'>" +
                        "<sourceFile>&xxe;</sourceFile><mutatedClass>A</mutatedClass>" +
                        "<mutatedMethod>f</mutatedMethod><methodDescription>()V</methodDescription>" +
                        "<lineNumber>1</lineNumber>" +
                        "<mutator>M</mutator><description>d</description></mutation></mutations>"

                val result = runCatching { readXml(xml) }

                result.getOrNull().orEmpty().forEach { it.sourceFile shouldNotContain "SECRET" }
            } finally {
                secret.deleteIfExists()
            }
        }

        test("未知の status なら例外を投げる") {
            val error = shouldThrow<IllegalArgumentException> {
                readXml(
                    "<mutations><mutation detected='true' status='EXPLODED'>" +
                        "<sourceFile>A.kt</sourceFile><mutatedClass>A</mutatedClass>" +
                        "<mutatedMethod>f</mutatedMethod><methodDescription>()V</methodDescription>" +
                        "<lineNumber>1</lineNumber>" +
                        "<mutator>M</mutator><description>d</description></mutation></mutations>",
                )
            }
            error.message shouldBe "未知の status です: EXPLODED"
        }
    })
