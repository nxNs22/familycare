# familycare

A senior-friendly care app for Romanian families, combining medication reminders, scam-call warnings and family check-ins in one place.

- [Market research](docs/market-research.md)
- [Technical plan](docs/technical-plan.md)
- [`android/`](android): Android prototype of the scam-call warnings (`core` = tested logic, `app` = Android integration)

## Build

Needs JDK 17+ and the Android SDK:

```sh
cd android
./gradlew :core:test :app:assembleDebug
```

CI builds the debug APK on every push. You can download it from the **Android** workflow run.
