package io.github.kawagh.kontactlens

import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.isRegularFile
import kotlin.io.path.readLines
import kotlin.io.path.writeText

/**
 * 変異の一覧とソースファイルから、ブラウザで開ける 1 枚の HTML レポートを作る。
 *
 * レポートでは、ファイルごとにソースを全文表示し、変異した行の下に変異後の行と検出結果を並べる。
 */
object HtmlReport {
    /**
     * [mutations] を、[sourceRoots] から探したソースファイルと合わせて HTML にする。
     *
     * ソースファイルは、変異したクラスのパッケージと [Mutation.sourceFile] から
     * `<sourceRoot>/io/github/.../FizzBuzz.kt` の形で探し、最初に見つかったものを使う。
     * 見つからないファイルは、ソースを表示せずに変異の一覧だけを表示する。
     */
    fun render(mutations: List<Mutation>, sourceRoots: List<Path>): String {
        val sources = mutations.map(::sourcePath).distinct().mapNotNull { path ->
            sourceRoots.map { it.resolve(path) }.firstOrNull { it.isRegularFile() }?.let { path to it.readLines() }
        }.toMap()

        val data = buildString {
            append("const SOURCES = ")
            append(jsonObject(sources.mapValues { (_, lines) -> jsonArray(lines.map(::jsonString)) }))
            append(";\nconst MUTATIONS = ")
            append(jsonArray(mutations.map { toJson(it, sources) }))
            append(";")
        }
        return template.replace(DATA_PLACEHOLDER, data)
    }

    /** [render] の結果を [output] に書き出す。出力先のディレクトリが無ければ作る。 */
    fun write(mutations: List<Mutation>, sourceRoots: List<Path>, output: Path) {
        output.toAbsolutePath().parent?.createDirectories()
        output.writeText(render(mutations, sourceRoots))
    }

    private const val DATA_PLACEHOLDER = "/*KONTACTLENS_DATA*/"

    private val template: String by lazy {
        checkNotNull(HtmlReport::class.java.getResource("report.html")) { "レポートのテンプレートがありません" }.readText()
    }

    // ソースルートからの相対パス（例: io/github/kawagh/kontactlens/sample/FizzBuzz.kt）
    private fun sourcePath(mutation: Mutation): String {
        val packageName = mutation.mutatedClass.substringBeforeLast('.', missingDelimiterValue = "")
        val packageDirectory = packageName.replace('.', '/')
        return if (packageDirectory.isEmpty()) mutation.sourceFile else "$packageDirectory/${mutation.sourceFile}"
    }

    private fun toJson(mutation: Mutation, sources: Map<String, List<String>>): String {
        val path = sourcePath(mutation)
        val line = sources[path]?.getOrNull(mutation.lineNumber - 1)
        val info = MutatorInfo.of(mutation.mutator)
        return jsonObject(
            mapOf(
                "file" to jsonString(path),
                "line" to mutation.lineNumber.toString(),
                "mutator" to jsonString(info.name),
                "what" to jsonString(info.label),
                "to" to (line?.let { estimateMutatedLine(mutation, it) }?.let(::jsonString) ?: "null"),
                "description" to jsonString(mutation.description),
                "status" to jsonString(mutation.status.name),
                "detected" to mutation.detected.toString(),
            ),
        )
    }

    private fun jsonObject(entries: Map<String, String>): String =
        entries.entries.joinToString(",", "{", "}") { (key, value) -> jsonString(key) + ":" + value }

    private fun jsonArray(values: List<String>): String = values.joinToString(",", "[", "]")

    // <script> の中に埋め込むので、< > & もエスケープして </script> などでページが壊れないようにする
    private fun jsonString(value: String): String = buildString {
        append('"')
        for (c in value) {
            when (c) {
                '"' -> append("\\\"")
                '\\' -> append("\\\\")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                '<', '>', '&', ' ', ' ' -> append("\\u%04x".format(c.code))
                else -> if (c < ' ') append("\\u%04x".format(c.code)) else append(c)
            }
        }
        append('"')
    }
}
