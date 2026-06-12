# Itinera

A cross-platform travel itinerary and ticket organizer for Android and iOS, built with **Kotlin Multiplatform** and **Compose Multiplatform**. One shared Kotlin codebase drives the full UI and logic on both platforms.

Itinera helps frequent travellers keep every trip in one place — plan day-by-day legs and places to visit, track tickets and documents, convert currencies with live exchange rates, and run a pre-departure checklist, all with a clean, themeable interface available in 40+ languages.


## Features

- **Truly cross-platform** — a single `shared` module written in Kotlin renders the entire UI and business logic on both Android and iOS via Compose Multiplatform.
- **Trip planning** — organize trips into day-numbered legs (travel between places) and activities (places to visit), grouped automatically by date.
- **Swipe-to-reveal actions** — pin, edit, archive, or delete trips with custom anchored-drag gestures and spring animations.
- **Live currency conversion** — real-time exchange rates from the [Frankfurter API](https://frankfurter.dev) (European Central Bank data), with swap, loading, and error states.
- **Remote trip imagery** — trip cover photos fetched by destination from the Unsplash API, with a graceful colour fallback.
- **Documents & tickets** — keep booking references and travel documents attached to each trip.
- **Pre-departure checklist** — add items with keyword-based category suggestions.
- **Appearance** — full light / dark / system theming wired to a single theme source.
- **Localization** — UI translated across 40+ languages with an English fallback layer.
- **Profile & settings** — editable profile, language and currency preferences, and an archived-trips view.

## Tech stack

| Area | Technology |
|------|------------|
| Language | Kotlin (Multiplatform) |
| UI | Compose Multiplatform, Material 3 |
| Networking | Ktor client (OkHttp on Android, Darwin on iOS) |
| Serialization | kotlinx.serialization |
| Images | Coil 3 (with Ktor network loader) |
| Dates | kotlinx-datetime |
| Concurrency | kotlinx.coroutines |
| Build | Gradle (Kotlin DSL) with version catalog |

## Project structure

```
shared/                      # Shared Kotlin Multiplatform module
  src/commonMain/            # UI + logic shared by both platforms
    kotlin/com/itinera/app/
      data/                  # Repository, sample data, API clients
      i18n/                  # Strings & localization
      model/                 # Data models
      ui/screens/            # All app screens (Compose)
      ui/components/         # Shared UI components
      ui/theme/              # Theme
  src/androidMain/           # Android-specific actuals
  src/iosMain/               # iOS-specific actuals
androidApp/                  # Android application entry point
iosApp/                      # iOS application (Xcode project)
```

## Building and running

### Prerequisites

- A recent **JDK** (17+)
- **Android Studio** (latest stable) for the Android app
- **Xcode** (on macOS) for the iOS app

### Android

1. Clone the repo and open the project root in Android Studio.
2. Let Gradle sync.
3. Select the `androidApp` run configuration and run it on an emulator or device.

Or from the command line:

```bash
./gradlew :androidApp:installDebug
```

### iOS

1. Open `iosApp/iosApp.xcodeproj` in Xcode.
2. Select an iOS Simulator (or a connected device) and run.

Gradle builds the shared framework automatically as part of the Xcode build.

### Unsplash API key (optional)

Trip cover photos use the Unsplash API. Without a key the app runs fine — trip cards simply show a colour fallback instead of photos.

To enable photos, add your free [Unsplash access key](https://unsplash.com/developers) to `local.properties` (which is gitignored and never committed):

```properties
unsplashAccessKey=your_access_key_here
```

The build reads this value and generates a `Secrets` object at compile time, so the key stays out of source control.

## License

Released under the [MIT License](LICENSE).
