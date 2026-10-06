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
