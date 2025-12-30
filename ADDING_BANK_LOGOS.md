# 🏦 Adding Real Bank Logos

I've created placeholder vector drawables for each bank. Here's how to replace them with real logos:

## 📁 Logo Files Created

I've created these placeholder drawable XML files:
- `composeApp/src/commonMain/composeResources/drawable/bank_hapoalim.xml` - Red diamond (placeholder)
- `composeApp/src/commonMain/composeResources/drawable/bank_leumi.xml` - Blue circle (placeholder)
- `composeApp/src/commonMain/composeResources/drawable/bank_mizrahi.xml` - Orange circle (placeholder)
- `composeApp/src/commonMain/composeResources/drawable/bank_discount.xml` - Purple rectangle (placeholder)

## 🎨 How to Add Real Bank Logos

### Option 1: Convert SVG to Android Vector Drawable (Recommended)

1. **Get Bank Logo SVGs**:
   - Download official bank logos (make sure you have permission!)
   - Or create simplified versions

2. **Convert SVG to XML Vector**:
   - Open Android Studio
   - Right-click on `composeApp/src/commonMain/composeResources/drawable/`
   - Select `New → Vector Asset`
   - Choose "Local file (SVG, PSD)"
   - Select your SVG file
   - Save as `bank_hapoalim.xml` (overwrite the placeholder)

3. **Repeat for each bank**

### Option 2: Manual XML Editing

If you have the Bank Hapoalim logo you showed me (red diamond), I can help you create the exact vector XML.

The current placeholder in `bank_hapoalim.xml` is:
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="100dp"
    android:height="100dp"
    android:viewportWidth="100"
    android:viewportHeight="100">

    <!-- Red diamond - replace with actual logo paths -->
    <path
        android:pathData="M50,10 L90,50 L50,90 L10,50 Z"
        android:fillColor="#E30613"/>
</vector>
```

### Option 3: Use PNG Images (Easier but less scalable)

1. **Add PNG files**:
   ```
   composeApp/src/commonMain/composeResources/drawable-mdpi/bank_hapoalim.png
   composeApp/src/commonMain/composeResources/drawable-hdpi/bank_hapoalim.png
   composeApp/src/commonMain/composeResources/drawable-xhdpi/bank_hapoalim.png
   ```

2. **Different sizes**:
   - mdpi: 48x48 px
   - hdpi: 72x72 px
   - xhdpi: 96x96 px
   - xxhdpi: 144x144 px

## 🔄 Updating the Code to Use Images

To switch from emoji to actual images, update `CoinOverlay.kt`:

### Current Code (Uses Emoji):
```kotlin
Text(
    text = com.keren.virtualmoney.game.Coin.getIcon(coin.type),
    fontSize = (48 * coin.scale).sp
)
```

### Updated Code (Uses Drawable Resources):
```kotlin
Image(
    painter = painterResource(getBankLogo(coin.type)),
    contentDescription = when (coin.type) {
        CoinType.BANK_HAPOALIM -> "Bank Hapoalim"
        CoinType.BANK_LEUMI -> "Bank Leumi"
        CoinType.BANK_MIZRAHI -> "Bank Mizrahi"
        CoinType.BANK_DISCOUNT -> "Bank Discount"
    },
    modifier = Modifier.fillMaxSize()
)
```

And add this helper function:
```kotlin
@Composable
private fun getBankLogo(type: CoinType): DrawableResource = when (type) {
    CoinType.BANK_HAPOALIM -> Res.drawable.bank_hapoalim
    CoinType.BANK_LEUMI -> Res.drawable.bank_leumi
    CoinType.BANK_MIZRAHI -> Res.drawable.bank_mizrahi
    CoinType.BANK_DISCOUNT -> Res.drawable.bank_discount
}
```

## 📝 Bank Logo Specifications

### Bank Hapoalim (בנק הפועלים)
- **Colors**: Red (#E30613), White
- **Shape**: Diamond/rhombus
- **Style**: Simple geometric shape

### Bank Leumi (בנק לאומי)
- **Colors**: Blue (#003D7A), White
- **Shape**: Circle or rectangle
- **Style**: Modern, clean

### Bank Mizrahi-Tefahot (בנק מזרחי טפחות)
- **Colors**: Orange (#FF6B00), Green
- **Shape**: Circle or abstract
- **Style**: Professional

### Bank Discount (בנק דיסקונט)
- **Colors**: Purple (#8B4789), Pink
- **Shape**: Rectangle or abstract
- **Style**: Bold, modern

## 🚀 Quick Test

After adding real logos:
```bash
./gradlew installDebug
```

## ⚠️ Important Legal Notes

1. **Copyright**: Make sure you have permission to use bank logos
2. **Trademark**: Bank logos are trademarked - this is for educational/portfolio use only
3. **Attribution**: Consider adding credit if required
4. **Commercial Use**: If publishing commercially, get explicit permission

## 📧 Need Help?

If you have the actual bank logo files (SVG, PNG, or other formats), I can help you integrate them into the project!

---

**Current Status**: Using emoji placeholders 🏛️🏦💰💳

**Ready for**: Real vector drawables or PNG images
