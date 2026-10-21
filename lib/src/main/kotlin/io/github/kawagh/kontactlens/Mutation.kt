package io.github.kawagh.kontactlens

/**
 * PIT が作った変異 1 件と、その検出結果。
 *
 * PIT の `mutations.xml` の `<mutation>` 要素 1 つに対応する。
 *
 * @property sourceFile 変異したクラスのソースファイル名。パッケージのディレクトリは含まない（例: `FizzBuzz.kt`）。
 * @property mutatedClass 変異したクラスの完全修飾名（例: `io.github.kawagh.kontactlens.sample.FizzBuzzKt`）。
 * @property mutatedMethod 変異したメソッド名。
 * @property methodDescription 変異したメソッドの JVM のメソッド記述子（例: `(I)Ljava/lang/String;`）。
 * @property lineNumber 変異した命令に対応するソースの行番号。inline 関数を展開した箇所では、ファイルに存在しない行番号になることがある。
 * @property mutator 変異の種類を表す、PIT の mutator のクラス名（例: `org.pitest.mutationtest.engine.gregor.mutators.MathMutator`）。
 * @property description PIT による変異の説明（例: `Replaced integer modulus with multiplication`）。
 * @property status 検出結果。
 * @property detected テストが変異を検出したかどうか。
 * @property numberOfTestsRun この変異に対して実行したテストの数。
 * @property indexes 変異した命令の、メソッド内での位置。
 * @property blocks 変異した命令が属する、メソッド内の基本ブロックの番号。
 * @property killingTests 変異を検出したテスト。PIT を `fullMutationMatrix` なしで実行した場合は、最初に検出した 1 件だけになる。
 * @property succeedingTests 変異があっても成功してしまったテスト。`fullMutationMatrix` を有効にした場合だけ得られる。
 * @property coveringTests 変異した箇所を実行するテスト。`fullMutationMatrix` を有効にした場合だけ得られる。
 */
data class Mutation(
    val sourceFile: String,
    val mutatedClass: String,
    val mutatedMethod: String,
    val methodDescription: String,
    val lineNumber: Int,
    val mutator: String,
    val description: String,
    val status: MutationStatus,
    val detected: Boolean,
    val numberOfTestsRun: Int,
    val indexes: List<Int>,
    val blocks: List<Int>,
    val killingTests: List<String>,
    val succeedingTests: List<String>,
    val coveringTests: List<String>,
)

/**
 * 変異の検出結果。PIT の `DetectionStatus` に対応する。
 */
enum class MutationStatus {
    /** テストが失敗し、変異を検出できた。 */
    KILLED,

    /** 変異した箇所をテストが実行したが、すべて成功してしまい検出できなかった。 */
    SURVIVED,

    /** 変異によってテストが終わらなくなり、時間切れになった。検出できたものとして扱われる。 */
    TIMED_OUT,

    /** 変異後のクラスが JVM で読み込めなかった。 */
    NON_VIABLE,

    /** 変異によってメモリが不足した。検出できたものとして扱われる。 */
    MEMORY_ERROR,

    /** 解析が始まる前に終了した。 */
    NOT_STARTED,

    /** 解析を始めたが、結果が出る前に終了した。 */
    STARTED,

    /** テストの実行中にエラーが起きた。 */
    RUN_ERROR,

    /** 変異した箇所を実行するテストが 1 つも無い。 */
    NO_COVERAGE,

    /** 変異しても元のコードと同じ振る舞いになる（等価変異）と判定された。 */
    EQUIVALENT,
}
