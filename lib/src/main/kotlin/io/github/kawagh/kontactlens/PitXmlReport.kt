package io.github.kawagh.kontactlens

import java.io.InputStream
import java.nio.file.Path
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamConstants
import javax.xml.stream.XMLStreamReader
import kotlin.io.path.inputStream

/**
 * PIT が出力する `mutations.xml` を読み込む。
 *
 * PIT を `outputFormats = setOf("XML")` を含めて実行すると、レポートのディレクトリに `mutations.xml` が出力される。
 */
object PitXmlReport {
    /**
     * [path] にある `mutations.xml` を読み込み、変異の一覧をレポートに書かれた順に返す。
     *
     * @throws IllegalArgumentException 必須の要素が欠けているなど、PIT のレポートとして解釈できない場合。
     */
    fun read(path: Path): List<Mutation> = path.inputStream().use(::read)

    /**
     * [input] から `mutations.xml` の内容を読み込み、変異の一覧をレポートに書かれた順に返す。[input] は閉じない。
     *
     * @throws IllegalArgumentException 必須の要素が欠けているなど、PIT のレポートとして解釈できない場合。
     */
    fun read(input: InputStream): List<Mutation> {
        val reader = xmlInputFactory.createXMLStreamReader(input)
        try {
            val mutations = mutableListOf<Mutation>()
            while (reader.hasNext()) {
                if (reader.next() == XMLStreamConstants.START_ELEMENT && reader.localName == "mutation") {
                    mutations += readMutation(reader)
                }
            }
            return mutations
        } finally {
            reader.close()
        }
    }

    // レポートは信頼できない入力として扱い、DTD と外部エンティティを無効にする（XXE 対策）
    private val xmlInputFactory: XMLInputFactory =
        XMLInputFactory.newFactory().apply {
            setProperty(XMLInputFactory.SUPPORT_DTD, false)
            setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false)
        }

    // reader が <mutation> の開始タグを指している状態で呼び、終了タグまで読み進める
    private fun readMutation(reader: XMLStreamReader): Mutation {
        val status = reader.requiredAttribute("status")
        val detected = reader.requiredAttribute("detected")
        val numberOfTestsRun = reader.getAttributeValue(null, "numberOfTestsRun")

        val texts = mutableMapOf<String, String>()
        val indexes = mutableListOf<Int>()
        val blocks = mutableListOf<Int>()
        while (true) {
            when (reader.next()) {
                XMLStreamConstants.START_ELEMENT ->
                    when (val name = reader.localName) {
                        // 子要素を並べるだけの入れ物なので、中身の <index> と <block> で読む
                        "indexes", "blocks" -> Unit
                        "index" -> indexes += reader.elementText.trim().toInt()
                        "block" -> blocks += reader.elementText.trim().toInt()
                        else -> texts[name] = reader.elementText
                    }
                XMLStreamConstants.END_ELEMENT -> if (reader.localName == "mutation") break
            }
        }

        fun required(name: String): String =
            texts[name] ?: throw IllegalArgumentException("<mutation> に <$name> がありません")

        return Mutation(
            sourceFile = required("sourceFile"),
            mutatedClass = required("mutatedClass"),
            mutatedMethod = required("mutatedMethod"),
            methodDescription = required("methodDescription"),
            lineNumber = required("lineNumber").trim().toInt(),
            mutator = required("mutator"),
            description = required("description"),
            status = parseStatus(status),
            detected = detected.toBooleanStrict(),
            numberOfTestsRun = numberOfTestsRun?.toInt() ?: 0,
            indexes = indexes,
            blocks = blocks,
            // fullMutationMatrix を有効にすると <killingTest> の代わりに <killingTests> が出力される
            killingTests = splitTests(texts["killingTests"] ?: texts["killingTest"]),
            succeedingTests = splitTests(texts["succeedingTests"]),
            coveringTests = splitTests(texts["coveringTests"]),
        )
    }

    private fun XMLStreamReader.requiredAttribute(name: String): String =
        getAttributeValue(null, name) ?: throw IllegalArgumentException("<mutation> に $name 属性がありません")

    private fun parseStatus(value: String): MutationStatus =
        MutationStatus.entries.find { it.name == value }
            ?: throw IllegalArgumentException("未知の status です: $value")

    // 複数のテストは "|" 区切りで 1 つの要素に入っている
    private fun splitTests(value: String?): List<String> = value?.split('|')?.filter { it.isNotEmpty() } ?: emptyList()
}
