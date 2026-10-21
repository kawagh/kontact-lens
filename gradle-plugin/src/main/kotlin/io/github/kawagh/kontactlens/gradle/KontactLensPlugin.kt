package io.github.kawagh.kontactlens.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer

/**
 * kontactLens の Gradle プラグイン。
 *
 * `kontactLensReport` タスクを登録する。タスクは PIT の `mutations.xml` を読み、
 * `build/reports/kontactlens/index.html` にレポートを出力する。
 *
 * - `java` プラグイン（Kotlin JVM プラグインも含む）があれば、`main` ソースセットのディレクトリからソースを探す。
 * - `info.solidsoft.pitest` プラグインがあれば、`pitest` タスクの後に実行する。
 *   PIT の設定では `outputFormats` に `XML` を含め、`timestampedReports = false` にしておく必要がある。
 */
class KontactLensPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val report = project.tasks.register(TASK_NAME, KontactLensReportTask::class.java) { task ->
            task.group = "verification"
            task.description = "Generates the kontactLens report from the PIT results."
            task.mutationsXml.convention(project.layout.buildDirectory.file("reports/pitest/mutations.xml"))
            task.outputFile.convention(project.layout.buildDirectory.file("reports/kontactlens/index.html"))
        }

        project.pluginManager.withPlugin("java") {
            val main = project.extensions.getByType(SourceSetContainer::class.java).getByName("main")
            // Kotlin のソースディレクトリも allSource に含まれる
            report.configure { it.sourceDirectories.from(main.allSource.sourceDirectories) }
        }

        project.pluginManager.withPlugin("info.solidsoft.pitest") {
            report.configure { it.dependsOn(PITEST_TASK_NAME) }
        }
    }

    private companion object {
        const val TASK_NAME = "kontactLensReport"
        const val PITEST_TASK_NAME = "pitest"
    }
}
