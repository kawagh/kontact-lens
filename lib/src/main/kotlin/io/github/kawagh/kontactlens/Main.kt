package io.github.kawagh.kontactlens

import java.nio.file.Path
import kotlin.system.exitProcess

/**
 * PIT の `mutations.xml` から HTML レポートを作るコマンド。
 *
 * 使い方: `<mutations.xml> <出力する HTML> <ソースのディレクトリ>...`
 */
fun main(args: Array<String>) {
    if (args.size < 3) {
        System.err.println("使い方: <mutations.xml> <出力する HTML> <ソースのディレクトリ>...")
        exitProcess(1)
    }
    val (report, output) = args.take(2).map { Path.of(it) }
    val sourceRoots = args.drop(2).map { Path.of(it) }

    HtmlReport.write(PitXmlReport.read(report), sourceRoots, output)
    println("レポートを出力しました: ${output.toAbsolutePath()}")
}
