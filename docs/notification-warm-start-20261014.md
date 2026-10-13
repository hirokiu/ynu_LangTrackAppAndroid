# 通知タップ後の白画面修正

OPPO Android14 / 正式署名Proto 2.0.0(6)で、通知をタップするとSplashActivityの白画面に留まる報告。端末上でSplashActivityが前面のまま、クラッシュ記録なしを確認。旧実装はOSスプラッシュ終了コールバックと装飾アニメーション終了後にのみMainActivityへ遷移しており、OSのスプラッシュが出ない起動経路で遷移できない構造だった。

SplashActivityをOSの標準的なルーティング専用方式（keep-on-screen→Main開始→finish）へ変更。ガラス調アートはMainActivity内の装飾として表示し、遷移条件から分離。Dev共通の既存ヘルパーの入口は維持。

正式署名2.0.0(7)のAPK/AABビルド成功、署名検証成功。OPPOへ上書きインストール成功（認証情報を維持）。起動はMainActivityを経由し、保存されていた通知対象のSurveyContainerActivityへ到達することをdumpsysで確認。通知を再送しての利用者による再確認は待ち。

APK SHA256: a6d3f413f027bda45df5ca489035c11cb8d61f5868e6c1d732ba077275625a82
成果物: ../releases/android/20261014-warm-start/
版6は一般配布せず、版7の通知再確認後に候補を差し替える。
