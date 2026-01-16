# 📱 App Icon Integration Guide

## Generated Icon Location
Your custom app icon has been generated and saved in the `.gemini` folder.

## 🤖 Android Integration

### Step 1: Resize Icon
Use an online tool like **AppIcon.co** or **MakeAppIcon**:
1. Upload the 1024x1024 icon
2. Select "Android" platform
3. Download the generated pack

### Step 2: Replace Default Icons
Copy the resized icons to these locations:

```
VirtualMoney/composeApp/src/androidMain/res/
├── mipmap-mdpi/
│   └── ic_launcher.png (48x48)
├── mipmap-hdpi/
│   └── ic_launcher.png (72x72)
├── mipmap-xhdpi/
│   └── ic_launcher.png (96x96)
├── mipmap-xxhdpi/
│   └── ic_launcher.png (144x144)
└── mipmap-xxxhdpi/
    └── ic_launcher.png (192x192)
```

Also replace round icons:
```
mipmap-*/ic_launcher_round.png (same sizes)
```

### Step 3: Verify AndroidManifest.xml
File: `/composeApp/src/androidMain/AndroidManifest.xml`

Should already have:
```xml
<application
    android:icon="@mipmap/ic_launcher"
    android:roundIcon="@mipmap/ic_launcher_round"
    ...>
```

## 🍎 iOS Integration

### Step 1: Create AppIcon.appiconset Directory
```bash
cd /Users/kerenlint/MyProjects/AndroidStudioProjects/VirtualMoney/iosApp/iosApp
mkdir -p Assets.xcassets/AppIcon.appiconset
```

### Step 2: Add Icon Files
Use **AppIcon.co** to generate iOS icons from your 1024x1024 master.

Place in `Assets.xcassets/AppIcon.appiconset/`:
- icon-20@2x.png (40x40)
- icon-20@3x.png (60x60)
- icon-29@2x.png (58x58)
- icon-29@3x.png (87x87)
- icon-40@2x.png (80x80)
- icon-40@3x.png (120x120)
- icon-60@2x.png (120x120)
- icon-60@3x.png (180x180)
- icon-1024.png (1024x1024)

### Step 3: Create Contents.json
File: `Assets.xcassets/AppIcon.appiconset/Contents.json`

```json
{
  "images": [
    {
      "filename": "icon-20@2x.png",
      "idiom": "iphone",
      "scale": "2x",
      "size": "20x20"
    },
    {
      "filename": "icon-20@3x.png",
      "idiom": "iphone",
      "scale": "3x",
      "size": "20x20"
    },
    {
      "filename": "icon-29@2x.png",
      "idiom": "iphone",
      "scale": "2x",
      "size": "29x29"
    },
    {
      "filename": "icon-29@3x.png",
      "idiom": "iphone",
      "scale": "3x",
      "size": "29x29"
    },
    {
      "filename": "icon-40@2x.png",
      "idiom": "iphone",
      "scale": "2x",
      "size": "40x40"
    },
    {
      "filename": "icon-40@3x.png",
      "idiom": "iphone",
      "scale": "3x",
      "size": "40x40"
    },
    {
      "filename": "icon-60@2x.png",
      "idiom": "iphone",
      "scale": "2x",
      "size": "60x60"
    },
    {
      "filename": "icon-60@3x.png",
      "idiom": "iphone",
      "scale": "3x",
      "size": "60x60"
    },
    {
      "filename": "icon-1024.png",
      "idiom": "ios-marketing",
      "scale": "1x",
      "size": "1024x1024"
    }
  ],
  "info": {
    "author": "xcode",
    "version": 1
  }
}
```

## ⚡ Quick Command (from project root)

### Android:
```bash
# After generating icons with AppIcon.co
cp ~/Downloads/AppIcon/android/mipmap-*/*.png composeApp/src/androidMain/res/
```

### iOS:
```bash
# After generating icons
cp ~/Downloads/AppIcon/ios/AppIcon.appiconset/* iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/
```

## 🧪 Testing

### Android:
```bash
./gradlew clean
./gradlew installDebug
# Check device home screen for new icon
```

### iOS:
```bash
open iosApp/iosApp.xcodeproj
# Build and run in Xcode
# Check simulator home screen
```

## 🔍 Verification Checklist

- [ ] Android: Icon appears on all density screens
- [ ] Android: Round icon displays correctly
- [ ] iOS: Icon shows in Xcode asset catalog
- [ ] iOS: Icon appears on simulator home screen
- [ ] Both: Icon looks clear and sharp (not pixelated)
- [ ] Both: Colors match the branding

## 🎨 Icon Design Reference

Your generated icon features:
- **Symbol:** Israeli Shekel (₪)
- **Colors:** Gold coin on blue-cyan gradient
- **Style:** Modern, 3D, premium
- **Theme:** Financial/AR gaming

## 📐 Size Reference

| Platform | Purpose | Size |
|----------|---------|------|
| Android | mdpi | 48x48 |
| Android | hdpi | 72x72 |
| Android | xhdpi | 96x96 |
| Android | xxhdpi | 144x144 |
| Android | xxxhdpi | 192x192 |
| iOS | 20pt @2x | 40x40 |
| iOS | 20pt @3x | 60x60 |
| iOS | 29pt @2x | 58x58 |
| iOS | 29pt @3x | 87x87 |
| iOS | 40pt @2x | 80x80 |
| iOS | 40pt @3x | 120x120 |
| iOS | 60pt @2x | 120x120 |
| iOS | 60pt @3x | 180x180 |
| iOS | App Store | 1024x1024 |

##  🛠️ Recommended Tools

1. **AppIcon.co** (Recommended)
   - Free, web-based
   - Generates all sizes automatically
   - Exports for Android and iOS

2. **MakeAppIcon**
   - Alternative option
   - Also generates all needed sizes

3. **Android Studio Image Asset Studio**
   - Built into Android Studio
   - Tools > Image Asset
   - Can generate adaptive icons

## 🚀 Done!

After integration, rebuild and test on both platforms to verify the icon displays correctly!
