# Androidテーマ・UI/UX調査とiOS比較

調査日：2026-09-19。対象：refactor/environment-routing のソースとAndroid公式資料。今回は設計判断の材料を整理するのみ。アプリコード・依存関係・配布物は変更していない。実機での全画面検証は未実施。

## 推奨方針（未承認）

KIROKUNの色・情報の順序・回答率の円グラフ・回答データの意味はiOSと共通にする。AndroidではMaterial 3 / Material 3 Expressiveの標準部品を使い、システムの戻る操作、起動、通知、入力操作に従う。Liquid Glassの見た目を独自に再現することは今回の対象にしない。テーマ変更と全面的なCompose移行は分離する。

## 公式ガイドの位置づけ

- Android 17 / API 37の挙動変更も確認対象。現コードは既にcompileSdk/targetSdk 37、minSdk 23（Android 6）。SDK指定が新しいこととUIが最新ガイドに対応していることは別。[Android 17](https://developer.android.com/about/versions/17)
- Material 3 ExpressiveはMaterial 3の拡張。Android OSのバージョンやメーカーのシステムUIと、アプリが採用するMaterialテーマは別であり、OS更新だけでアプリの見た目が更新されるわけではない。[Material 3](https://developer.android.com/develop/ui/compose/designsystems/material3)
- 既存のViews/XML向けMaterial Components 1.14.0はExpressiveテーマや部品を含む公式リリース。Compose専用と誤解しない。現コードにも1.14.0が指定済みだが、AppCompatテーマのままなので一括で適用されているわけではない。[公式リリース](https://github.com/material-components/material-components-android/releases/tag/1.14.0)
- メーカー固有の透明表現やOSのパネルの外観を、そのままアプリの必須デザインとみなさない。標準部品と公開APIの範囲で選ぶ。

## 現行ソースで確認した事実

| 対象 | 現状 | 次回の作業候補 |
|---|---|---|
| app/build.gradle | Kotlin、Views/XML、ViewBinding/DataBinding。Compose未導入 | 既存構成のままMaterialテーマを段階適用 |
| res/values/styles.xml | Theme.AppCompat.Light.DarkActionBar | Material 3系テーマへ移行、各部品の互換性を確認 |
| res/values/colors.xml | colorPrimary=#008577、lta_blue=#516496等 | 色を用途別に整理。文字・背景・選択・エラーを分離 |
| SplashActivity.kt | Handlerで1500ms待つ独自Activity | AndroidX SplashScreenへ移行し固定待ちを撤去 |
| dev/AndroidManifest.xml | DevはSplashActivityを除外してDevSetupActivityから起動 | ProtoとDevで共通の起動テーマを適用 |
| DevSetupActivity.kt | コード生成UI、ピクセル値の固定余白あり | 共通テーマ・dpベースの余白へ。認証方式は環境別に維持 |
| StatisticsViewHolder.kt | ProgressBarによる円形の回答率表示あり | 重要機能として維持。率・件数を読み上げ可能にする |
| likert_scale_fragment.xml | 5択RadioGroupが高さ32dp、横並び | 48dpの操作領域、狭幅・文字拡大・長文で検証 |
| popup_alert.xml | 幅300dpの独自ポップアップ | 標準Dialog/BottomSheetを検討 |
| WindowInsets.kt | システムバー・切り欠き・IMEの余白処理あり | 未対応と断定せず、二重余白・キーボード・各Activityを検証 |
| SurveyContainerActivity.kt | OnBackPressedDispatcherで終了確認済み | 予測型戻ると回答保持を検証。旧APIの全面置換は不要 |
| main/AndroidManifest.xml | Activityの縦向き固定、通知色lta_blue | 大画面の回転・リサイズ、通知用色/アイコンを確認 |

## iOSとの比較・選択表

| 項目 | 共通化する内容 | Androidで推奨する違い | 判断 |
|---|---|---|---|
| ブランド | KIROKUN、コーラル#FF5857、Devアイコンの識別 | 色をMaterialの役割へ割り当て、必要な濃淡を用意 | 共通＋Android適用 |
| Liquid Glass | 階層の明確さ、読みやすさ | Materialの面・形状・押下反応・標準モーション。ガラス模倣はしない | Android推奨 |
| トップ | KIROKUNヘッダー、中央のプロジェクト名、短い説明、円グラフ、調査一覧の順序 | Material Top App Barとリスト。円グラフを置き換えたり省略しない | 構成は共通 |
| 調査一覧 | 未回答は文字＋アイコン、回答済みは緑のアイコン、交互背景 | 押下時の標準フィードバック、48dp以上の操作領域 | 共通＋標準部品 |
| プロジェクト選択 | ログイン前に選択、接続中表示、メニューから変更 | Exposed Dropdown / 選択Dialog。iOSのPickerを模倣しない | Android推奨 |
| サブメニュー | 使い方・このアプリについて・連絡先等の内容 | オーバーフローメニュー／必要に応じBottomSheet。タブを増やす必要はない | 容器は選択可 |
| 調査開始 | 調査名・設問数・公開日・期限、値の行頭揃え | 標準Dialogか通常ページ。情報量が多ければ通常ページ | 選択可 |
| 回答画面 | 調査名、問題番号/全体数、必須・入力方式、設問順 | MaterialのTextField・RadioButton・Checkbox・Slider | Android推奨 |
| リッカート | 選択肢の値・順序・意味、両端ラベル | まず横並びの操作領域を確保。小画面は縦並び案も比較 | 見せ方の変更は研究者確認 |
| 回答確認 | 自分の回答一覧、「回答を送信」、送信中と再試行 | 標準進捗表示、二重送信防止、未送信状態保持 | 共通＋標準部品 |
| 戻る | 回答を不用意に失わない | OSの戻る/予測型戻る・上部Up・設問の「前へ」を区別 | Android対応必須 |
| 起動画面 | ブランド色、ロゴの識別 | SplashScreen APIの単色背景＋中央アイコン。全面ガラス画像をそのまま移植しない | Android推奨 |
| 日付・時刻・キーボード | 同じ入力値と表示情報 | Android標準の選択UI、IME、入力エラー表示 | Android推奨 |
| 通知 | 正しいプロジェクト/調査へ移動 | 通知チャンネル、権限要求、通知用単色アイコン、戻り先の履歴 | Android対応必須 |
| ダークモード | コーラルのブランドを維持 | 昼夜それぞれ文字と面の色を定義 | 対応範囲を選択 |
| 壁紙連動色 | — | Dynamic Colorは任意。色が変わるため今回は無効を推奨 | 選択可 |
| 画面サイズ | 同じ調査内容と主要導線 | 小画面・大画面・分割表示・折りたたみ・文字拡大に対応 | Android対応必須 |

Android 12以降の起動はOSのSplashScreenが基本で、AndroidX互換ライブラリはAPI 23以降に対応する。独自SplashActivityを併用すると二重表示になる場合がある。今回の方針では演出だけの固定待ちを設けない。[SplashScreen](https://developer.android.com/develop/ui/views/launch/splash-screen)、[移行ガイド](https://developer.android.com/develop/ui/views/launch/splash-screen/migrate)

API35以降を対象とするアプリのedge-to-edgeでは、ステータスバー・ナビゲーション・IMEを踏まえた配置が必要。API37対象では大画面（sw>=600dp）で縦向き固定等の制限を頼れない。[Insets](https://developer.android.com/develop/ui/views/layout/edge-to-edge)、[Android 17変更点](https://developer.android.com/about/versions/17/behavior-changes-17)

## 配色・アクセシビリティの判断

#FF5857と白#FFFFFFのコントラストは、sRGB相対輝度で計算すると約3.09:1。黒なら約6.79:1。白い小さい本文には不足する。Googleの指針は通常の小さい文字4.5:1以上、大きい文字3:1以上、操作領域48×48dp以上。[アクセシビリティ](https://developer.android.com/guide/topics/ui/accessibility/apps)

提案：ブランドの赤は変更せず、ヘッダーの大きい文字・装飾に使う。小さい説明文は白い面に濃色文字で表示する。ボタンは十分なコントラストを持つ組み合わせを別途設計する。「全面を濃い赤へ変更」は提案しない。最終判断では実際の文字サイズ・太さ・透明度・全状態を測定する。iOSの配色を機械的にコピーして適合済みとは扱わない。

円グラフは数値と回答済み/全件数でも理解可能にする。状態は色だけで識別させない。TalkBackの順序、選択状態、入力必須・エラーの読み上げを確認する。リッカートをスライダーへ置換すると回答行動が変わり得るため、テーマ変更の一環として勝手に変えない。

## コーディング規約・実装方針

1. Kotlin公式Androidスタイルを基準に、4スペース、命名・import・null安全・明示的な公開APIを統一。既存全ファイルの機械整形をUI改修に混ぜない。[Kotlin style guide](https://developer.android.com/kotlin/style-guide)
2. Activity/Fragmentは描画と入力、ViewModelは画面状態、Repositoryは通信という既存の分離を改善。UIスレッドを通信で塞がず、ライフサイクルに沿って非同期処理を管理する。新規設計ではCoroutines/Flow・単方向の状態管理を検討する。[Architecture recommendations](https://developer.android.com/topic/architecture/recommendations)
3. 回転・プロセス再生成・戻る操作で回答と設問位置を保持する。ViewModelだけでプロセス終了まで保持できると考えず、SavedStateHandleや適切な下書き保存を設計する。送信完了と未送信を分離する。
4. 文字はstrings/plurals、寸法はdp・文字はsp、色は役割付きtheme資源へ。日本語をコードに直書きせず、既存の言語資源を維持する。翻訳漏れはデフォルト資源と各言語を比較する。[Localization](https://developer.android.com/guide/topics/resources/localization)
5. 戻る・起動・通知はAndroidX/OS公開APIで実装。独自タイマー、標準操作の無効化による見た目調整を避ける。[Predictive back](https://developer.android.com/guide/navigation/custom-back/predictive-back-gesture)
6. Composeは新規UIの公式推奨だが今回の必須条件ではない。現行XMLでもMaterial 3/Expressiveを利用できるため、今回はViews継続を推奨。将来、設問エディタや入力形式増加に合わせ、別工程で段階移行する。
7. 安定版依存関係を基準にする。Expressive全APIがすべてのライブラリ版で安定とは仮定せず、利用部品ごとに確認する。今回、依存関係は変更しない。

## 判断してから進める順序

1. 標準部品＋共通の情報配置を採用するか決める。ダークモード、文字の配色、調査開始画面の容器を決める。
2. トップ・1つの回答画面・プロジェクト選択の比較案を提示。円グラフと既存順序を保つ。
3. Androidテーマを適用し、起動API、余白、通知、戻るを整理。Proto/DevのAPIやFirebaseは混在させない。
4. 全設問形式・最終確認・送信を適用。画面更新の前後で回答JSONと選択肢値が変わらないことを検証。
5. API23の互換性と最新API37、Android12の起動、Android13の通知、Android15/16以降の戻る/余白の境界を確認。小画面/大画面、200%文字、TalkBack、回転、分割、キーボード、オフライン・送信失敗、通知起動を含める。実行可能なOS/端末を確認して検証表を確定する。
6. 利用者確認後、配布・サーバー最終移行へ進む。

本資料は調査と提案。UI改修の採用判断、実装・実機検証は未完了。
