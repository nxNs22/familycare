# Technical plan

_October 2026. Builds on the [market research](market-research.md)._

## Goal

Ship a Romanian, Android-first app for one senior and their family. It covers:

1. warnings about scam calls, without call interception or recording audio
2. medication reminders with one-tap confirmation
3. a daily "I'm OK" signal
4. a family dashboard showing alerts and missed items

The prototype in [`android/`](../android) covers point 1 so it can be tested on real phones first. That's the riskiest part technically.

## Architecture

```
 Senior's phone (Android app)                    Cloud                        Family
┌──────────────────────────────┐   events   ┌──────────────────┐   push   ┌──────────────────┐
│ CallScreeningService         │──────────▶ │ API + database   │────────▶ │ Family app / web │
│ Call-ended receiver          │  (HTTPS)   │ (EU region)      │          │ dashboard        │
│ Medication reminders         │ ◀───────── │ Scam-number list │ ◀─────── │ (manage meds,    │
│ "I'm OK" check-in            │  settings  │ SMS fallback     │ settings │  view alerts)    │
└──────────────────────────────┘            └──────────────────┘          └──────────────────┘
```

- **`core` module** (plain Kotlin, no Android): scam rules, call thresholds, Romanian texts. It's tested on the JVM and can be shared later by a Kotlin Multiplatform family app or the backend.
- **`app` module**: Android integration only, covering the call-screening role, the call log, notifications and the senior-facing screens.
- **Backend (not built yet)**: a small API in an EU region. It needs:
  - pairing between the senior and family members
  - an event feed (missed dose, alert about a long unknown call, missed check-in)
  - the list of numbers reported as scams
  - push notifications to the family (FCM)
  - an SMS fallback through a Romanian SMS provider
  - Firebase or Supabase would get us there fastest. Decide when we start the dashboard.

## Scam-call warnings: how the prototype works

| Step | Android API | File |
|---|---|---|
| An incoming call rings | `CallScreeningService` (the app holds `ROLE_CALL_SCREENING`) | [`ScamScreeningService.kt`](../android/app/src/main/java/ro/familycare/app/ScamScreeningService.kt) |
| Is the caller risky? Checks for a hidden number, not a contact, a foreign number, a premium-rate number (0900/0903/0906) or a reported scam number | `ContactsContract.PhoneLookup` | [`CallRiskEvaluator.kt`](../android/core/src/main/kotlin/ro/familycare/core/CallRiskEvaluator.kt) |
| Warn the senior while the phone rings | High-priority notification with a "Sună acum: Andrei" (call now: Andrei) action, which opens a full-screen warning | [`Notifications.kt`](../android/app/src/main/java/ro/familycare/app/Notifications.kt), [`WarningActivity.kt`](../android/app/src/main/java/ro/familycare/app/WarningActivity.kt) |
| The call ends | `PHONE_STATE` broadcast reaches idle, then the app reads the newest entry in `CallLog.Calls` | [`CallEndedReceiver.kt`](../android/app/src/main/java/ro/familycare/app/CallEndedReceiver.kt), [`CallLogChecker.kt`](../android/app/src/main/java/ro/familycare/app/CallLogChecker.kt) |
| Was it a long call with an unknown number? (10 minutes by default, in either direction) | | [`LongCallDetector.kt`](../android/core/src/main/kotlin/ro/familycare/core/LongCallDetector.kt) |
| Alert the family | **Prototype:** SMS to the trusted person plus a local notification. **Product:** an event sent to the backend | [`FamilyNotifier.kt`](../android/app/src/main/java/ro/familycare/app/FamilyNotifier.kt) |

The app never blocks, silences or records a call. It only warns. That keeps false positives harmless, and the app doesn't need microphone access.

### Things to confirm on real phones

These are the open questions the prototype is meant to answer:

1. **Does the heads-up warning show on top of the ringing screen?** Check the stock dialer, Samsung One UI and Xiaomi HyperOS. If it doesn't, try showing it right after the call is answered, or try a `SYSTEM_ALERT_WINDOW` overlay.
2. **Is `onScreenCall` called for unknown callers only, or for all calls?** On Android 10+, a screening app that isn't the default dialer is bound only for numbers not in contacts. The code checks contacts as well, either way.
3. **Does the manifest `PHONE_STATE` receiver fire reliably** after the app hasn't been opened for days, given battery optimisation on Xiaomi and Samsung? If not, use a foreground service with `TelephonyCallback`, or schedule a WorkManager check from `onScreenCall`.
4. **Call-log timing:** is a 3-second delay enough for the dialer to write the entry?
5. **Dual SIM** and VoLTE calls: are the number and duration still correct?

### Manual test script

1. Build the APK: download the `familycare-debug-apk` artifact from the **Android** GitHub Actions run, or run `cd android && ./gradlew :app:assembleDebug` with the Android SDK installed. Install it on an Android 10+ phone with a SIM.
2. Open **FamilyCare**. Enter a trusted name and number, then tap **Salvează** (Save), **1. Acordă permisiunile** (grant the permissions) and **2. Activează filtrarea apelurilor** (turn on call screening). Both status lines should show ✓.
3. Call the phone from a number that **isn't** in its contacts. A warning appears with a **Sună acum: …** (call now) button.
4. Call from a saved contact. No warning should appear.
5. Call from a hidden number (`#31#` prefix). The warning should say **număr ascuns** (hidden number).
6. Set the threshold to 1 minute, then talk for over a minute with an unknown number. The trusted person should get an SMS, and **Evenimente recente** (recent events) should log it.

## Google Play and legal

- **Call log and SMS permissions** are restricted on Google Play. `READ_CALL_LOG` is allowed for the "caller ID, spam detection and blocking" use case, but needs a declaration form and a review. **`SEND_SMS` will probably be refused:** move family alerts to the backend with push notifications before going to the Play Store. The prototype's SMS is for side-loaded testing only.
- **GDPR:** call metadata is personal data, and the senior is the data subject. We need:
  - explicit consent from the senior during setup ("your family will be told about long calls with unknown numbers")
  - minimal data: send the event, duration and partly masked number, never contacts or call content
  - EU hosting
  - a privacy policy
  - the senior able to pause monitoring
- **iPhone:** later, and with fewer features: a CallKit Call Directory extension with the reported-scam list, an "I just got a strange call" button and medication reminders. There's no detection of long calls on iOS.

## Roadmap

| Phase | Scope | Done when |
|---|---|---|
| **0. Call-warning prototype** (this branch) | Screening, warning, long-call SMS alert | The test script passes on 3 phones (stock Android, Samsung, Xiaomi) |
| **1. Senior app MVP** | Medication reminders (AlarmManager exact alarms, "Am luat" (taken) button, a reminder again after 15 minutes), daily "I'm OK" check-in, onboarding done by the family member | 5 Romanian families use it for 2 weeks |
| **2. Backend + family dashboard** | Pairing via QR code, event feed, push notifications, editing medications remotely, alert about missed doses or check-ins, SMS removed from the app | Play Store internal testing |
| **3. Scam intelligence** | Shared reported-number list (user reports, plus bank or police partners if possible), warning texts for each scam type | Public beta |
| **4. Monetisation + expansion** | Family subscription; Moldova, then Bulgaria and Hungary | — |

## Tech choices

- **Kotlin, native Android, min SDK 29** (needed for the call-screening role). There are no AndroidX dependencies in the prototype, so the build stays small. Add Jetpack Compose in phase 1 for the senior screens.
- **The family app** can be a simple web dashboard at first, since many adult children use iPhones. Only build a native app if push notifications through the browser aren't enough.
- **Tests:** the JVM unit tests in `core` run in CI on every push. Add instrumented tests once the UI settles.
