# Market research: elderly care apps

_Snapshot: October 2026. Prices and features change; re-check before relying on them._

## 1. Medication reminders + family dashboard (crowded)

| App | What it does | Notes |
|---|---|---|
| **Medisafe** | Medication reminders, adherence tracking | The category leader. Free consumer app; Premium is $4.99/month on the App Store. Medication only: no shared calendars or family messaging. ([caringvillage](https://caringvillage.com/2026/05/13/elderly-care-apps-for-families/), [caringvillage](https://caringvillage.com/?p=14523)) |
| **PillsCircle** | SMS reminder at dose time with one big "taken" button | The parent doesn't need to install an app. US only. ([alternativeto](https://alternativeto.net/software/pillscircle/about/)) |
| HiveLink, CareMinder, MySeniorCareHub, EchoCare, Elli Cares | Reminders plus a family view | Very similar to each other. Most come from solo founders and have few reviews. |

**Takeaway:** medication reminders alone won't stand out. They're a feature people expect to find, not the reason to choose us.

## 2. Scam-call protection (newer, growing fast)

| App | What it does | Notes |
|---|---|---|
| **Truecaller** (family feature) | A designated admin gets alerts about suspected fraud calls to relatives and can end the call remotely | Huge install base, so it's a serious competitor on scam calls. ([techloy](https://www.techloy.com/how-to-use-truecallers-new-family-feature-to-stop-scam-calls/)) |
| **OnGuardAI** | Monitors calls in real time and texts the user and a family member when fraud patterns show up | Founder says the economics only work where carriers don't charge for call forwarding. ([betakit](https://betakit.com/?p=408872)) |
| **Phonely** (UK) | AI call screening; a trusted person can join the call live | UK only |
| **ScamBlocker** (UK) | AI call screening | UK only |

**Catch:** real-time interception usually depends on carrier infrastructure (call forwarding), and iOS restricts what apps can do with calls.

## 3. Romania specifically

- **Parents are OK**: a Romanian startup that alerts the family when a parent's phone stays unusually inactive. It doesn't use GPS, the camera or the microphone. It does this one thing and isn't a full care app. ([startupcafe](https://startupcafe.ro/doi-antreprenori-romani-lansare-aplicatie-siguranta-seniori-105898))
- We found **no Romanian-language app** that combines medications, scam protection and family check-ins.

## 4. Where the opening is

1. **Local market first.** Competitors are mostly English-only and focused on the US or UK. Start with Romania, then the rest of Eastern Europe, using:
   - local scam scripts: fake bank or "security department" calls, and the "your son/daughter had an accident" scam (_metoda accidentul_)
   - local pharmacies and Romanian drug names
2. **All-in-one simplicity.** Today a family needs 2–3 separate apps. We'd offer one senior app with big buttons and one family dashboard.
3. **Scam warnings before call blocking.** Real-time interception is expensive and hard on iPhone. A version that's practical today:
   - a warning screen for calls from unknown numbers
   - a one-tap "Call my son/daughter first" button
   - a family alert when the parent has a long call with an unknown number

## 5. Technical notes for the scam-warning feature

These need checking against current platform docs before we build anything.

- **Android** (most phones in Romania) is where this can actually be built:
  - `CallScreeningService` (with the call-screening role) can see incoming calls before they ring and label or reject them.
  - Call state and call log access (`READ_PHONE_STATE`, `READ_CALL_LOG`) can supply duration and number for "long call with an unknown number" alerts. Google Play restricts call-log permissions to apps whose core function needs them, so this needs a policy justification.
  - A full-screen warning before or after the call is possible, but the Android version and the OEM skin (Samsung, Xiaomi) affect how reliably it shows.
- **iOS** offers much less:
  - CallKit's Call Directory extension can only block numbers or add labels from a list prepared in advance.
  - Apps can't see live call duration or the number for normal cellular calls, so there's no live warning screen.
  - On iPhone, use the labels from the list, education content, and an "I just got a strange call" button that alerts the family.
- **No audio processing at first.** Avoiding microphone access keeps privacy simple (GDPR, trust from older users) and avoids carrier costs, which is the same choice Parents are OK made.

## 6. Proposed MVP scope (Romania, Android-first)

**Senior app** (big buttons, Romanian only, works without an account):
- medication reminders with a single "Am luat" (taken) confirmation, plus an SMS fallback (the PillsCircle approach) for parents who won't use the app
- warning before calls from unknown numbers, with a "Call [child] first" button
- daily "I'm OK" check-in, done automatically from phone activity or by tapping

**Family dashboard:**
- missed doses, missed check-ins, and alerts about long calls with unknown numbers
- a shared medication list and simple notes, with no full calendar in the first version

**Left out on purpose for now:** live call interception, AI voice analysis, GPS tracking, cameras.

## 7. Open questions

- Will Romanian families pay for this? At what price, compared with Medisafe's $4.99/month?
- Distribution: pharmacies, GPs, banks (who lose money to scams), or telecom partners (Orange, Vodafone/Digi)?
- Can we partner with a bank or the police on a regularly updated list of known scam numbers?
- Are there legal limits (GDPR, consent) on notifying the family about a parent's calls? We need explicit consent from the senior.
