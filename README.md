# 🎙️ Speech-to-Text AI Assistant

An Android application designed to act as a personal voice assistant. It leverages speech recognition to understand user commands and perform various actions—from making phone calls and sending messages to opening the camera and interacting with media. The assistant is built to be intuitive, responsive, and functional both online and offline for core commands.

---

## 📚 Table of Contents

- [Features](#features)
- [Permissions Required](#permissions-required)
- [Technology & Requirements](#technology--requirements)
- [Setup & Installation](#setup--installation)
- [How It Works](#how-it-works)
- [Voice Commands](#voice-commands)
  - [Offline Commands](#offline-commands-work-without-internet)
  - [Online Commands](#online-commands-require-internet)
- [Project Structure](#project-structure)
- [Future Improvements](#future-improvements)

---

## ✨ Features

- **Speech Recognition**: Core functionality to listen for and interpret voice commands.
- **Contact Interaction**: Search for contacts and initiate phone calls.
- **Camera & Media**: Open the camera, and access images and videos from the gallery.
- **Device Administration**: Advanced device control (e.g., locking the screen) via Device Admin permissions.
- **Hybrid Functionality**: Offline support for essential commands.

---

## 🔐 Permissions Required

To function correctly, the app requests the following permissions:

- `RECORD_AUDIO`: Capture voice commands.
- `INTERNET` & `ACCESS_NETWORK_STATE`: For online recognition and network tasks.
- `READ_CONTACTS`: Search and call contacts.
- `CALL_PHONE`: Initiate phone calls.
- `CAMERA`: Open the device's camera.
- `READ_EXTERNAL_STORAGE`, `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`: Access gallery media.
- `BIND_DEVICE_ADMIN`: Enable screen-locking and other admin features.

---

## 🛠️ Technology & Requirements

- **Platform**: Android
- **Languages**: Java / Kotlin
- **Core APIs**:
  - `SpeechRecognizer` for voice input
  - `TelephonyManager` & `ContactsContract` for calls and contacts
  - `Camera` & `MediaStore` intents for media access
  - `DevicePolicyManager` for admin-level control
- **Minimum SDK**: 24 (check `build.gradle`)
- **Target SDK**: 35 (as per `AndroidManifest.xml`)

---

## 🚀 Setup & Installation

1. **Clone the Repository**  
   ```bash
   git clone <your-repository-url>


- Open in Android Studio
Open the cloned folder in Android Studio.
- Build the Project
Sync Gradle files via File > Sync Project with Gradle Files.
- Run the App
- Connect an Android device or use an emulator.
- Click ▶️ Run 'app' in Android Studio.
- Grant Permissions
- Accept all requested permissions on first launch.
- Manually enable Device Admin if prompted.

⚙️ How It Works
- The app listens for a trigger (e.g., button press) to start SpeechRecognizer.
- Recognized speech is parsed into a string.
- Command-matching logic identifies keywords and executes corresponding actions.
- Online: Uses network-based recognition for higher accuracy.
- Offline: Uses on-device recognition for basic commands.

🗣️ Voice Commands
🔌 Offline Commands (Work without Internet)
|  |  | 
|  |  | 
|  |  | 
|  |  | 
|  |  | 


🌐 Online Commands (Require Internet)
|  |  | 
|  |  | 
|  |  | 
|  |  | 
|  |  | 


Note: Command phrasing is flexible—keyword detection drives execution.


🗂️ Project Structure
- MainActivity.java: Core logic for speech recognition and command handling.
- DeviceAdmin.java: Handles device admin privileges.
- activity_main.xml: UI layout for the main screen.
- strings.xml: User-facing strings and app name.
- device_admin_policies.xml: Security policies for admin features.
- AndroidManifest.xml: Declares app components and permissions.

🚧 Future Improvements
- Background Listening: Add hotword detection (e.g., "Hey Assistant").
- Natural Language Understanding: Integrate advanced NLP for conversational commands.
- Expanded Integrations: Support SMS, WhatsApp, Spotify, smart home devices.
- UI/UX Enhancements: Add animations and visual feedback during listening.
