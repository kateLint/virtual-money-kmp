# Firebase Setup Guide

## Prerequisites

1. Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Enable the following services:
   - Authentication (Google, Apple, Anonymous)
   - Cloud Firestore
   - Realtime Database

---

## Android Setup

### 1. Add google-services.json

1. In Firebase Console, go to Project Settings
2. Add an Android app with package name: `com.keren.virtualmoney`
3. Download `google-services.json`
4. Place it in: `composeApp/src/androidMain/google-services.json`

### 2. SHA-1 Certificate (Required for Google Sign-In)

```bash
# Debug certificate
cd android
./gradlew signingReport
```

Add the SHA-1 fingerprint to your Firebase Android app settings.

---

## iOS Setup

### 1. Add GoogleService-Info.plist

1. In Firebase Console, go to Project Settings
2. Add an iOS app with bundle ID: `com.keren.virtualmoney`
3. Download `GoogleService-Info.plist`
4. Place it in: `iosApp/iosApp/GoogleService-Info.plist`

### 2. Add to Xcode Project

1. Open `iosApp/iosApp.xcodeproj` in Xcode
2. Drag `GoogleService-Info.plist` into the project
3. Ensure "Copy items if needed" is checked
4. Add to target: `iosApp`

### 3. Configure URL Schemes (Required for Google Sign-In)

In `iosApp/iosApp/Info.plist`, add:

```xml
<key>CFBundleURLTypes</key>
<array>
    <dict>
        <key>CFBundleURLSchemes</key>
        <array>
            <string>com.googleusercontent.apps.YOUR_CLIENT_ID</string>
        </array>
    </dict>
</array>
```

Replace `YOUR_CLIENT_ID` with the value from `GoogleService-Info.plist` (`REVERSED_CLIENT_ID`).

---

## Firebase Console Configuration

### Authentication

Enable these sign-in providers:
- Anonymous
- Google
- Apple (requires Apple Developer account)

### Firestore Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can read/write their own profile
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == userId;

      match /{document=**} {
        allow read: if request.auth != null;
        allow write: if request.auth.uid == userId;
      }
    }

    // Leaderboards - anyone can read
    match /leaderboards/{type}/{document=**} {
      allow read: if request.auth != null;
      allow write: if false; // Only Cloud Functions
    }

    // Challenges - anyone can read
    match /challenges/{type}/{document=**} {
      allow read: if request.auth != null;
      allow write: if false;
    }

    // Game config - read only
    match /gameConfig/{docId} {
      allow read: if request.auth != null;
      allow write: if false;
    }
  }
}
```

### Realtime Database Rules

```json
{
  "rules": {
    "lobbies": {
      "$lobbyId": {
        ".read": "auth != null",
        ".write": "auth != null"
      }
    },
    "games": {
      "$gameId": {
        ".read": "auth != null",
        "players": {
          "$odId": {
            ".write": "auth.uid == $odId"
          }
        }
      }
    },
    "matchmaking": {
      "queues": {
        "$gameMode": {
          "$odId": {
            ".read": "auth != null",
            ".write": "auth.uid == $odId"
          }
        }
      }
    },
    "presence": {
      "$odId": {
        ".read": "auth != null",
        ".write": "auth.uid == $odId"
      }
    }
  }
}
```

---

## Testing Firebase Connection

After setup, run the app and check:

1. Anonymous sign-in works
2. Firestore reads/writes succeed
3. Realtime Database connects

Check logcat (Android) or Xcode console (iOS) for Firebase initialization logs.

---

## Troubleshooting

### Android

- **"Default FirebaseApp is not initialized"**: Ensure `google-services.json` is in the correct location
- **Google Sign-In fails**: Check SHA-1 fingerprint is added to Firebase

### iOS

- **Firebase not initializing**: Ensure `GoogleService-Info.plist` is added to Xcode target
- **Google Sign-In fails**: Check URL scheme is configured correctly

---

## Next Steps

Once Firebase is configured:
1. Test authentication in the app
2. Verify Firestore writes work
3. Test Realtime Database for multiplayer
