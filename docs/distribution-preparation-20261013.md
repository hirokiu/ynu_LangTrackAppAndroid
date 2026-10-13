# Android正式配布準備 2026-10-13

## 確認結果

旧配布APK（2025-11-08）も2026-10-12確認版もAndroid Debug署名。旧版のSHA256は `de9093b6cab795e8246a49ec4dc5171c1f785ff1b1a6653e7d8abf1003d3cf1a`。
正式署名への切替は原則として旧版への上書き不可となるため、利用者へ再インストールと再ログインを案内する必要がある。サーバー上の回答は維持されるが、未送信回答など端末内データを失う可能性がある。署名方針は上松へ確認中。

## 実装

Releaseからdebug署名へのフォールバックを削除。通常のassembleProtoReleaseは署名なし候補を生成できる。正式配布用は以下を設定して `./gradlew :app:prepareProtoDistribution` を実行する。

- KIROKUN_KEYSTORE: リポジトリ外の正式鍵ファイル絶対パス
- KIROKUN_KEY_ALIAS
- KIROKUN_STORE_PASSWORD
- KIROKUN_KEY_PASSWORD

秘密情報をコマンド履歴、Git、チャットに書かない。シェルの保護された入力または秘密管理から環境変数へ渡す。鍵を作成したら、別保管先へ暗号化バックアップし、今後の更新で同じ鍵を使う。

`ANDROID_SDK_ROOT=... bash ops/verify-distribution.sh APK EXPECTED_SHA256` で署名検証、証明書一致、Debug証明書拒否、debuggable拒否、アプリID確認を実施。Proto接続先はビルド設定と実機の両方で確認する。

## 検証

- assembleProtoRelease成功。2.0.0/build5の未署名APKを生成（インストール・一般配布不可）。
- 正式署名未設定のverifyDistributionSigningは意図どおり失敗。
- 既存Debug署名の確認版APKはverify-distribution.shで意図どおり拒否。
- 正式鍵での署名、実機確認、公開は未実施。

## 公開まで

1. 正式鍵へ切り替える方針を確定し、鍵の作成・保全と必要なFirebase指紋登録を行う。
2. 最終配布時はversionCodeを現行確認版より大きくする。
3. 新規招待ユーザーでインストール、ログイン、通知受信、回答、再ログインを確認。
4. HTTPSダウンロード先を用意し、ファイルのSHA256と版番号を記録。未署名/検証用APKを公開しない。
5. 参加者にはブラウザーのインストール許可が必要なこと、インストール後に許可を戻せることを平易に案内。

根拠: https://developer.android.com/studio/publish/app-signing
