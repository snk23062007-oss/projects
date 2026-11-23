# NEXUS — Android Second Brain (MVP)

NEXUS is a native Android prototype for a personal AI second brain. It uses Kotlin and Jetpack Compose with a dark navy/cyan/violet interface.

## Current MVP
- Futuristic home dashboard
- Local quick-capture notes persisted in a Room database
- Memory list and basic search backed by local Room storage
- Gemini chat gateway with local memory context and encrypted on-device API-key storage
- Actions screen scaffold (no device actions run yet)

## Build
1. Install Android Studio.
2. Open the `NEXUS` directory as a project.
3. Allow Gradle sync and install the Android SDK matching `compileSdk`.
4. Run the `app` configuration on an emulator or Android phone.

## Privacy and API keys
No Gemini API key is included in this repository. For the personal prototype, enter your own key through the in-app settings; it is encrypted with Android Keystore. For a public release, use a protected backend because keys embedded in or used directly by a client app can still be extracted. Never commit secrets to source control.

## Roadmap
1. UI + local capture
2. Persistent structured memory and context retrieval
3. Gemini API integration with source-aware answers
4. Voice input/output
5. Permission-gated reminders and Android actions
6. Export, deletion, accessibility and reliability testing

This is an early MVP with Room-backed local memory, not a full system-level Android assistant. Android permissions and OS restrictions apply.
