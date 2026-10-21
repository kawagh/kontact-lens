package io.github.kawagh.kontactlens.gradle

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File

private fun File.write(path: String, content: String) {
    resolve(path).apply { parentFile.mkdirs() }.writeText(content.trimIndent())
}

// PIT を実行せずに済むよう、PIT が出力したことにした mutations.xml を置いたプロジェクトを作る
private fun createProject(dir: File): File = dir.apply {
    write("settings.gradle.kts", """rootProject.name = "demo"""")
    write(
        "build.gradle.kts",
        """
        plugins {
            java
            id("io.github.kawagh.kontactlens")
        }
        """,
    )
    write(
        "src/main/java/demo/Calc.java",
        """
        package demo;

        public class Calc {
            public static int rest(int n) {
                return n % 3;
            }
        }
        """,
    )
    write(
        "build/reports/pitest/mutations.xml",
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <mutations>
        <mutation detected='false' status='SURVIVED' numberOfTestsRun='1'><sourceFile>Calc.java</sourceFile><mutatedClass>demo.Calc</mutatedClass><mutatedMethod>rest</mutatedMethod><methodDescription>(I)I</methodDescription><lineNumber>5</lineNumber><mutator>org.pitest.mutationtest.engine.gregor.mutators.MathMutator</mutator><indexes><index>2</index></indexes><blocks><block>0</block></blocks><killingTest/><description>Replaced integer modulus with multiplication</description></mutation>
        </mutations>
        """,
    )
}

class KontactLensPluginTest :
    FunSpec({
        test("main ソースセットのソースと mutations.xml から HTML レポートを出力する") {
            val project = createProject(tempdir())

            val result = GradleRunner.create()
                .withProjectDir(project)
                .withPluginClasspath()
                .withArguments("kontactLensReport")
                .build()

            result.task(":kontactLensReport")?.outcome shouldBe TaskOutcome.SUCCESS
            val html = project.resolve("build/reports/kontactlens/index.html").readText()
            html shouldContain "\"demo/Calc.java\":["
            html shouldContain "\"to\":\"        return n * 3;\""
        }
    })
