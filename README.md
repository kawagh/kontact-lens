# kontactLens

[PIT](https://pitest.org/) の変異テストの結果を、ソースコードと並べて見せる HTML レポートを作ります。

- ソースを全文表示し、変異した行の下に「変異後の行」と「検出結果（KILLED / SURVIVED / NO_COVERAGE）」を並べます。
- 変異後の行は、PIT の mutator と説明文から推定しています（PIT はソース上の変化を記録しないため）。
- Kotlin と Java、Kotest と JUnit のプロジェクトに対応しています。

## 使い方

Gradle プラグインとして使います。PIT（[gradle-pitest-plugin](https://github.com/szpak/gradle-pitest-plugin)）を使っているプロジェクトに、`+` の行を追加します。

まだ Gradle Plugin Portal には公開していないため、このリポジトリを clone して `includeBuild` で読み込みます。

```diff
 // settings.gradle.kts
 pluginManagement {
+    includeBuild("path/to/kontact-lens")
     repositories {
         gradlePluginPortal()
     }
 }
```

kontactLens は PIT の XML のレポートを読むので、PIT の設定にも 2 行追加します。

```diff
 // build.gradle.kts
 plugins {
     kotlin("jvm") version "2.3.20"
     id("info.solidsoft.pitest") version "1.19.0"
+    id("io.github.kawagh.kontactlens")
 }

 pitest {
     targetClasses = setOf("com.example.*")
+    outputFormats = setOf("XML")
+    timestampedReports = false
 }
```

PIT 標準の HTML レポートも出したい場合は、`outputFormats = setOf("XML", "HTML")` にします。

```sh
./gradlew kontactLensReport
```

`pitest` の後に実行され、`build/reports/kontactlens/index.html` に出力されます。

## サンプル

`samples/` に、同じ FizzBuzz を Kotlin + Kotest、Kotlin + JUnit、Java + JUnit で書いたプロジェクトがあります。15 の倍数をわざとテストしていないため、SURVIVED と NO_COVERAGE の変異を観察できます。

```sh
./gradlew sampleReports  # lib/build/reports/kontactlens/ にサンプルごとのレポートを出力
```

## 開発

```sh
./gradlew build                   # テスト、detekt を含むビルド
./gradlew detekt --auto-correct   # コードの整形
```

| ディレクトリ | 内容 |
| --- | --- |
| `lib/` | PIT の `mutations.xml` の読み込みと、HTML レポートの生成 |
| `gradle-plugin/` | Gradle プラグイン（`kontactLensReport` タスク） |
| `samples/` | 変異を観察するためのサンプル |

## ライセンス

[MIT](LICENSE)
