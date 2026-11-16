# DisplayMonitor 開発ガイド

## プロジェクト概要

DisplayMonitor は Paper 1.20.4 向けの Kotlin 製 Minecraft プラグインである。YAML で定義した **Action** を順に実行し、Minecraft の Display Entity を中心とする **Element** を生成・操作する。外部プラグイン向けの公開契約は `DisplayMonitorAPI` モジュールに置く。

## モジュールと主な配置

| 場所 | 役割 |
| --- | --- |
| `src/main/kotlin` | プラグイン本体（`tororo1066.displaymonitor`） |
| `DisplayMonitorAPI/src/main/java` | 他プラグインが依存する Java API。互換性に注意して変更する |
| `GenDocs` | アノテーションから `docs-data.json` を生成する CLI |
| `src/main/resources` | `plugin.yml`、初期設定、翻訳リソース |
| `docs-data.json` | Action / Element / 型 / 式関数の生成済みドキュメント |

Java 17（toolchain）、Kotlin 2.2.0、Paper API を使用する。ルートの `shadowJar` が配布用 JAR を作成し、API と実行時依存の一部を同梱する。

## 実行モデル

- `DisplayMonitor` が起点。遅延初期化で Function / Action / Variable / Workspace / Element の各 Storage、設定、コマンドを登録する。
- `WorkspaceStorage` の標準 Workspace は `DisplayMonitor`。`plugins/DisplayMonitor/actions/**/*.yml` の Action 定義を読み込む。
- `ElementStorage` は `plugins/DisplayMonitor/elements/**/*.yml` の Element プリセットを読み込む。YAML の `type` は登録済み Element クラス名を指す。
- `ActionConfiguration` は `actions:` と `triggers:` を保持し、`ActionRunner` が各 Action の `class` から実装を引いて順番に実行する。
- `ActionRunner` は原則非同期で動く。Bukkit / Paper のスレッド制約がある操作は既存 Action と同様にメインスレッドへ切り替える。`AbstractAction.checkAsync()` の意味を変えないこと。
- 実行状態は `IActionContext`（caster、target、location、parameters、stop 等）で運ぶ。トリガー実行では Workspace ごとに PublicContext を複製する。

## YAML と式の記法

Action 定義の基本形は次のとおり。

```yaml
example:
  triggers:
    - trigger: some-event
  actions:
    - class: Message
      message: "Hello"
```

- Action の識別子は `class:`。組み込み Action 名は `ActionStorage` に登録する。
- Element プリセットはトップレベル名の下に `type:` と Settable パラメータを書く。生成時の override はプリセット値の後に適用される。
- `AdvancedConfiguration` は文字列を式として再帰評価する。評価に失敗した場合は元の文字列を返すため、式の仕様を変更すると設定互換性へ直結する。
- 式関数は `configuration/expression/functions` の `AbstractFunction` 実装から自動登録される。関数を足す場合は公開名の衝突に注意する。
- `@Settable` のフィールドは YAML から設定可能。`name` が空ならフィールド名をキーに使う。入れ子の `CustomSettable` と `childOnly` は `SettableProcessor` の再帰処理に従う。
- `AllowedPlayers` は `allowedPlayers` / `disallowedPlayers` に UUID またはプレイヤー名を指定する。許可リストが空なら拒否リスト以外を許可し、許可・拒否の両方に一致した場合は拒否を優先する。

## 拡張時の規約

- Action を追加する場合は `IAbstractAction` を実装（通常は `AbstractAction` を継承）し、`ActionStorage` に安定した `class` 名で登録する。中断・非同期・例外処理は `ActionRunner` の契約に合わせる。
- Element を追加する場合は `IAbstractElement` を実装（通常は `AbstractElement` を継承）し、`ElementStorage` に `type` 名を登録する。設定可能な値には `@Settable`、利用者に見せる値には `@ParameterDoc` を付ける。
- API を拡張する場合はまず `DisplayMonitorAPI` の interface / event を検討する。公開 API の破壊的変更は避け、実装側と API 側を同時に検証する。
- 利用者向けに追加・変更する Action、Element、パラメータ、式関数は `@ClassDoc` と `@ParameterDoc` を更新する。`GenDocs` がこれらを `docs-data.json` に反映する。
- 翻訳キーを追加する場合は `src/main/resources/translations/en_us.properties` と `ja_jp.properties` の両方を更新する。

## コマンド・データ・再読み込み

- 管理コマンドは `/dmonitor`（権限 `dmonitor.op`）。`runLoaded` は Workspace 内の Action を実行し、`reload` は Config / actions / elements / variables を再読込する。
- 永続データの配置先はプラグインの data folder。`actions/`、`elements/`、`variables/` が読み込み対象である。
- プラグイン起動後、`displayentity` PersistentData を持つ既存 Entity はクリーンアップされる。このキーや起動時のクリーンアップ方針を変更する際は残存 Entity への影響を確認する。

## 検証と生成

```powershell
.\gradlew.bat test
.\gradlew.bat build
.\gradlew.bat GenDocs:shadowJar
```

- ユニットテストは `src/test/kotlin`。新しい純粋ロジック（式、当たり判定、設定変換など）にはここへテストを追加する。
- `docs-data.json` は `GenDocs` の生成物。GitHub Actions は `master` への push 時に更新する。ドキュメントの完全な項目一覧を `AGENTS.md` に重複させず、生成元のアノテーションとこの JSON を正とする。
- ビルドや公開で GitHub Packages を使うため、ローカルでは `GITHUB_USERNAME` / `GITHUB_TOKEN` が必要になる場合がある。秘密情報はコミットしない。

## 変更時の注意

- 既存 YAML のキー、`class` / `type` 名、式関数名はユーザー設定の公開仕様として扱う。
- 非同期コンテキストから Bukkit API を直接触らない。同期化が必要な処理は既存の `runTask` 等のパターンを使用する。
- 反射による no-arg constructor 生成と Settable のコピーに依存しているため、Action / Element の生成可能性を壊さない。
