# StudyCircle (المحفظ والمتعلم)

StudyCircle is a cross-platform mobile application built with **Kotlin Multiplatform** and **Compose Multiplatform** designed for managing Quran and study circles, tracking daily reading/recitation tasks, and streamlining supervisor reviews.

---

## Features

- 📖 **Study & Quran Circles**: Create, join, and manage reading and recitation circles with unique join codes.
- 📋 **Task Management**: Assign daily reading or recitation tasks, track progress by passages, and submit evidence.
- ✅ **Supervisor Review Queue**: Review pending join requests and task submissions with instant approval/rejection flows.
- 🌙 **Adaptive Design & Custom Typography**: Fully supports both Dark and Light modes with custom Arabic typography (**Amiri** for headers and **Cairo** for UI text).
- 🔥 **Streak Tracking**: Keep students motivated with daily streak counters and achievement badges.

---

## Tech Stack

- **Cross-Platform UI**: Compose Multiplatform (Android & iOS)
- **Architecture**: Clean Architecture / MVI (Model-View-Intent) pattern with shared UseCases and ViewModels
- **Dependency Injection**: Koin
- **Concurrency**: Kotlin Coroutines & Flow
- **Design System**: Material 3 with customized Islamic/Quranic theme palette

---

## Project Structure

```text
StudyCircle/
├── androidApp/          # Android application entry point & UI implementations
├── iosApp/              # iOS application entry point (SwiftUI / MainViewController)
├── shared/              # Kotlin Multiplatform shared module (Domain, Data, MVI, UseCases)
└── backend/             # Companion backend services
```

---

## Getting Started

### Prerequisites
- Android Studio (Koala or newer recommended)
- Xcode (for running the iOS app on macOS)
- JDK 17+

### Building & Running

#### Android
Open the project in Android Studio and run the `:androidApp` configuration, or build via terminal:
```bash
./gradlew :androidApp:assembleDebug
```

#### iOS
Open the `iosApp` directory in Xcode:
```bash
open iosApp/iosApp.xcodeproj
```
Or run the shared iOS tests:
```bash
./gradlew :shared:iosSimulatorArm64Test
```

---

## License

This project is open-source and available under the MIT License.
