# Username authentication preparation (2026-10-05)

Proto login first checks the selected server's `auth/options`. An explicit enabled response uses `auth/username-login`, then Firebase custom-token sign-in. Only HTTP 401 permits legacy Firebase email/password fallback, preserving unchanged respondents. HTTP 429, service failures, network failures and malformed responses do not downgrade authentication. Explicitly disabled capabilities, or HTTP 404 from a pre-module server, use legacy authentication. Entered email addresses use Firebase directly. Password whitespace is retained.

Both Proto and Dev resolve Firebase identity via `/api/me` before loading assignments or registering the device. No email-prefix guessing occurs in these builds. Topic subscriptions use the returned user ID. The historical `legacy` build retains its original email-derived identity and remote URL behavior. No server, credentials, Firebase configuration or notification flags were changed.

Deployment order: update and verify the server (including `/api/me` and all retained respondent UID mappings), then distribute the app. Verify existing respondents, both migrating researchers, a newly invited account, restored sessions, expired sessions and notification delivery in a dedicated test environment before credential removal. Dev offers its existing Google entry screen plus username sign-in for invited-account tests, with device registration and topic subscription disabled for Dev. Proto Google sign-in and in-app registration/recovery entry links remain follow-up work. Registration/recovery itself currently occurs on the Web.

Local validation: Proto and Dev debug APK builds; Proto unit tests including three authentication routing tests (explicit enabled/disabled, broken capabilities, fallback restricted to 401). These tests do not replace live Firebase, API, device or notification end-to-end verification. No live account was used for this change.

Read-only deployment check (2026-10-05, coordinator): Proto on port 8081 returned 404 for both `/api/me` and `/api/auth/options`; Dev on 8082 returned 401 for unauthenticated `/api/me` (route exists), and 404 for options. This app must not be distributed to Proto users yet: server rollout and respondent identity mapping verification are prerequisites.

## Live Dev integration — 2026-10-06

`InvitedAccountDevTest.usernameLoginAndAnswerRoundTrip` passed on isolated read-only
emulator instances at API 23 and API 37. It invokes the application's actual
AccountAuthentication and Repository: username/custom-token login, /me resolution,
assignment retrieval, Japanese answer submission, and persisted-answer reread.
The server independently confirmed both mobile answers; the two temporary assignments
were removed. Existing researcher/respondent accounts were not changed.

The first API 23 run revealed that Android 6 ignores networkSecurityConfig, while
the Dev manifest denied cleartext. DevDebug now allows cleartext via the manifest
for that OS; on API 24+ the network security config still limits it to localhost.
The API URL remains the fixed localhost SSH/adb-reverse endpoint. This change is
limited to DevDebug; Proto and DevRelease manifests are unchanged.

The test skips unless files/kirokun-private-qa.json is explicitly installed into
a dedicated Dev emulator. It checks package, Firebase project and API URL before
using it, and refuses to replace a different signed-in identity. Required string
fields: username, password, uid, userId, androidAssignment. Never commit the file.
For adb transfer, use non-PTY exec-in with stdout suppressed; interactive shell
input can echo secrets. Test cleanup signs out; remove the private fixture afterward.

These are application SDK/network integration tests, not tapped UI/notification tests.
Dev still disables push service, device registration and topic subscription.
Proto distribution remains blocked on server /me rollout and all respondent UID
mapping verification. User-driven Google additional linking remains deferred.

## Notification preparation (2026-10-06)

Replaced automatic activity launch on message receipt with a standard notification
and immutable activity PendingIntent. Added a monochrome small icon and localized
channel name. Foreground app startup initializes the same default channel used by
FCM background notifications. Denied permission/disabled notifications are respected;
notification bodies and tokens are no longer printed by the messaging service.
Dev still removes this service and skips registration/subscriptions.

ProtoDebug/DevDebug and test APK builds passed. NotificationConstructionTest passed
on API 37: Japanese content, private visibility, auto-cancel, immutable activity tap
intent and channel setting preservation. No notification was posted by this test.
Actual FCM receipt, background/terminated behavior and tapping remain unverified.
User will connect a physical device later. No app distribution or server update occurred.

Reference: https://firebase.google.com/docs/cloud-messaging/android/receive-messages
and https://developer.android.com/develop/ui/views/notifications/navigation .

## Physical Android push — 2026-10-06

Android 14 (CPH2603): Dev Firebase accepted a token-targeted notification, the
DevDebug-only receiver recorded actual receipt, and a second notification sent
while the user was on Home opened KIROKUN Dev when tapped. The user confirmed
opening; the app also recorded the notification payload on launch.

DevPushProvisioningTest requires explicit pushQa=true and checks the package and
Firebase project before enabling a single-device token. DevPushQaService exists
only in devDebug and requires the private QA flag plus the QA payload marker.
Normal Dev never subscribes to a topic or updates server user device tokens.
Provisioning uses the native notification permission prompt (this phone refused
shell permission grants). Token files are private and never printed.

After verification, disableSingleDeviceTest passed: automatic registration off,
QA flag cleared, Firebase token deleted and app-side records removed. Temporary
Android token files on Mac and server were also deleted. No regular scheduler,
Proto users or production Firebase configuration were changed.

ProtoDebug and DevRelease builds passed. Release build exposed a historical JPEG
named lunds_universitet_small.png; renamed it to .jpg without changing the bytes.
No end-to-end login/answer test was performed on this physical device in this pass.

## OPPO connection and launch correction — 2026-10-06

DevDebug now defaults to the authenticated HTTPS Dev API, matching DevRelease and
physical iPhone builds. Local-only emulator integration tests explicitly use
`-PdevLocalTunnel=true` with the SSH/adb reverse path; their loopback assertion remains.
No credentials, Firebase identity, notification flags or Proto endpoint changed.
The standard system splash icon now has a 132dp square inside a 288dp drawable,
so its corners fit inside Android's 192dp safe circle. Both Dev and Proto share it.
Android's system splash is a central icon, not the iOS full-screen ribbon artwork;
this correction does not claim pixel-identical launch layouts across platforms.
Reference: https://developer.android.com/develop/ui/views/launch/splash-screen

## Shared glass launch artwork — 2026-10-06

The iOS KirokunLaunchRibbon PNG is copied unchanged to drawable-nodpi and shared by
Dev and Proto. Android retains its system SplashScreen API and safe-area logo,
then uses its exit callback for a 450ms full-window branding fade. FIT_CENTER
preserves the entire image, with theme-color margins on different aspect ratios.
There is no fixed sleep, network-dependent splash hold, or extra branding text.
Dev authentication can run concurrently; successful navigation waits only for the
exit animation completion and rechecks session generation and UID. Proto routes
from its existing launcher after that callback. Launcher icons remain distinct.
DevDebug/ProtoDebug builds passed. OPPO visual confirmation remains necessary;
this is an Android transition, not an override of its OS-owned splash.

### OPPO visual follow-up

The inset icon was still visibly cropped on OPPO, so the OS splash now uses an
explicit transparent vector: theme background only, followed by the existing glass
artwork transition. No launcher icon fallback or extra minimum display timer is
requested. Drawer filled buttons explicitly use kirokun_brand/kirokun_on_brand;
brand text and project selector no longer inherit the dark red primary color.
The answered overview logo is removed and its 100dp title gap reduced to 16dp.
DevDebug and ProtoDebug builds passed; installed on OPPO for user visual confirmation.

## Version and submenu alignment — 2026-10-07

Android versionName is now 2.0.0, matching iOS MARKETING_VERSION. Android versionCode
increases from 3 to 4; platform-specific build counters are not reset to match iOS.
Instructions, About and Contact use kirokun_brand for headers, kirokun_on_brand for
header text/close icons, white surfaces and neutral body text. Programmatic heading
spans use the brand color rather than legacy dark red. Layout/content are preserved.
DevDebug and ProtoDebug builds passed; Dev installed on OPPO. Store release not performed.


## 通知から対象Surveyへの遷移（2026-10-07）

通知にkirokunAssignmentId・kirokunUserId・kirokunEnvironmentを付与。
グループ配信はAssignmentResultsではなく、本人一覧に返る親AssignmentのIDを使用。
アプリはタップ情報をログイン後まで保持（最大1時間）、環境と宛先を照合し、
認証後に取得した本人のSurvey一覧にある対象だけを開く。一度消費した情報は再利用しない。
回答可能なら回答開始、回答済みなら結果、期限切れなら期限情報。見つからない場合は案内。
Androidの前面通知もpayloadを維持し、PendingIntentを通知ごとに分離する。

検証: API TypeScriptビルド、隔離MongoDB＋FCMモックの個人/グループID・送信結果・再試行テスト成功。
Android Dev/Protoビルド成功。OPPOで環境違い・宛先違い・ID形式・期限・一度限り消費テスト成功。
iOS Dev実機ビルド成功。回答済み検証Survey宛のPush1通をDev iPhoneへ送信受付成功。
本人による回答済み画面への遷移確認待ち。通常API配信は未デプロイ・無効のまま。
新サーバーのops/state/push-qa-ios-20261007.jsonとMac非公開一時ファイルに検証トークン保持。
確認後はiOS QA cleanup起動でトークン失効・ファイル削除し、通常起動へ戻すこと。
未回答・期限切れ・削除済みの画面遷移、ログアウトからの復帰、Android実Push遷移は未確認。

### Android実Push遷移の準備（2026-10-07）

DevDebug専用受信サービスにも通知dataの引き継ぎを追加（通常受信サービスは対応済み）。
DevDebugとinstrumentationビルド成功、OPPOへ配置し単体Push登録テスト成功。
検証トークンはMac非公開ファイルと新サーバーops/state/push-qa-android-20261007.jsonで保持。
ホーム画面への移動確認後、回答済み検証Survey宛の1台限定通知を送信予定。
完了後はdisableSingleDeviceTestでトークン失効・QAファイル削除し、両一時ファイルも削除する。
通常のDev/Proto通知スケジューラー・研究者データは変更していない。


## 未回答バッジ（2026-10-07）

共通定義: 公開日時<=現在<期限、datasetなし。同一配信IDの重複は1件。
不正な日付は件数へ含めない。一覧取得成功時に更新し、回答成功後は一覧を再取得。
利用者切替・ログアウトでは消去。通信失敗時には以前の件数が残る場合がある。
iOSは標準setBadgeCountを使用。Androidは低重要度・無音の専用通知にsetNumberを設定し、
通知本文にも件数を表示。数字/点の表示はホームアプリ依存。0件で件数通知を削除。
Androidの一覧取得後は古い個別Survey通知を整理して件数の重複表示を避ける。
通知/バッジの権限は利用者設定を尊重する。

Android Dev/Protoビルドと件数境界の単体テスト成功。OPPOへ配置済み。
iOS Dev実機ビルド成功。件数XCTestは端末ロック解除待ち。
終了中の自動更新・期限到来による自動再計算は今回未対応。サーバーPushへの件数付与も未実装。
現時点で「常にリアルタイムの件数」とは案内しない。実機の数字表示・回答後0件の確認は次の操作。
Androidの前回実Push遷移は本人確認済み、検証トークン失効と一時ファイル削除完了。


### 実機バッジ確認完了（2026-10-07）

検証用Survey `qa_badge_manual_20261007` について、利用者から表示確認を受領。
OPPOでは数字ではなく点のバッジが表示された。続いて「回答後、バッジも消えました」と報告を受領。
Dev DBを読み取り照合し、回答保存済み・回答数1・当該利用者の公開中かつ期限内の個人未回答配信0件を確認した。
回答内容自体は記録していない。検証データの削除やProtoの変更は行っていない。
iOSの件数計算XCTestは接続したiPhone上で成功済み。
この確認は回答後の一覧更新に伴う消去を対象とする。アプリ終了中や期限到来時の自動更新は未対応のまま。


### 濃い赤の共通テーマ指定を修正（2026-10-07）
ログイン、ログアウト確認、回答ボタンなどが参照するcolorPrimary/colorPrimaryDark/colorAccent/lta_blueをkirokun_brand (#FF5857)への参照に統一。半透明の押下色も同色へ変更。Dev/Protoビルド成功、OPPOへDev修正版を上書き配置。見た目の本人確認待ち。

### ログアウト確認の余白（2026-10-07）
画面幅の75%指定を、利用可能幅から左右24dpを引き上限480dpとする指定へ変更。
共通OneChoicePopupの内側を24dp、ボタン間を12dp、ボタン幅を均等配分にし、
狭い画面では文字を折り返せるwrap_content高さを使用。本文の7行上限も撤去。
Dev/Protoビルド・差分チェック成功。OPPOへ上書き配置。実機の見た目確認待ち。


### Proto実機確認の準備（2026-10-07）
Protoサーバーに/meと45名UID対応、更新APIを反映済み。通常通知・配信準備と新規認証モジュールは停止中。
ビルド成功済みの最新版ProtoDebugをOPPOへ既存データを保持して上書き配置・起動成功。通常アプリID com.alchembright.dev.langtrackapp、Devとは別アプリ。
本人に従来アカウントでのログイン・Proto表示・Survey一覧を確認依頼中。回答送信・Proto実Pushはまだ未確認。一般配布は行っていない。

本人がiOS/Androidともにログアウト→従来認証で再ログイン→Proto Survey一覧表示を確認済み。
続いてhiroki_u専用にqa_proto_answer_20261007_ios / qa_proto_answer_20261007_androidを用意。各header/open/footerの3ページ、個人情報不要の端末名入力1問。既存データ変更なし、通常通知停止。回答送信確認待ち。
