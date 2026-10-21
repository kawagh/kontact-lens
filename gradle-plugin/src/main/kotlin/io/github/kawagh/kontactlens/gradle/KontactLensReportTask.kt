package io.github.kawagh.kontactlens.gradle

import io.github.kawagh.kontactlens.HtmlReport
import io.github.kawagh.kontactlens.PitXmlReport
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * PIT の `mutations.xml` とソースから、kontactLens の HTML レポートを作るタスク。
 */
abstract class KontactLensReportTask : DefaultTask() {
    /** PIT が出力した `mutations.xml`。 */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val mutationsXml: RegularFileProperty

    /** ソースを探すディレクトリ（例: `src/main/kotlin`）。存在しないディレクトリは無視される。 */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceDirectories: ConfigurableFileCollection

    /** 出力する HTML ファイル。 */
    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val output = outputFile.get().asFile.toPath()
        HtmlReport.write(
            PitXmlReport.read(mutationsXml.get().asFile.toPath()),
            sourceDirectories.files.map { it.toPath() },
            output,
        )
        logger.lifecycle("kontactLens のレポートを出力しました: ${output.toUri()}")
    }
}
