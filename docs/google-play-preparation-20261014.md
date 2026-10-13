# Google Play配布準備 2026-10-14

ユーザーがGoogle Play配布を希望。開発者アカウントの登録状況は本人確認中。アカウント作成、契約同意、支払い、一般公開は未実施。

## 成果物

- Proto 2.0.0 / versionCode 6
- 正式鍵でbundleProtoRelease成功、jarsigner verify成功（自己署名証明書の警告はAndroid署名方式による）。
- `../releases/android/20261014/KIROKUN-Proto-2.0.0-6.aab`
- SHA256 `b4c406613ca8f1625fe004dda0e07f3155f994a3ec5297448368704cd8ed9aaf`
- アップロード・Playから生成されたAPKでの実機確認は未実施。

## 必要な準備

1. Play Consoleアカウント種別・登録日・本人確認・本番公開権限を確認する。
2. APK配布とPlay版の更新互換性を維持するため、Play App Signingで既存の正式アプリ署名鍵を使用する構成を検討する。Google生成の異なる署名鍵を無確認で選択しない。秘密鍵のGoogleへの移管は具体的な手順を示して本人の承認後に実施する。アップロード鍵は別途分離可能。
3. FirebaseにPlayのアプリ署名証明書を登録し、内部テストで新規登録、通知、回答、更新を確認する。
4. 説明、スクリーンショット、プライバシーポリシー、データセーフティ、アプリアクセス（審査専用アカウント）、対象年齢、コンテンツレーティング、アカウント削除要件との適合を確認する。未確認の申告を推測して送信しない。
5. 対象となる新規個人アカウントでは12名以上が連続14日間参加するクローズドテストと、本番アクセス申請が必要。月末の一般公開はアカウント条件・審査次第で確約できない。従来予定のHTTPS APK配布は代替として準備を維持する。

公式情報:
- https://support.google.com/googleplay/android-developer/answer/14151465
- https://developer.android.com/studio/publish/app-signing
