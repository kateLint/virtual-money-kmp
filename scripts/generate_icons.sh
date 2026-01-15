#!/bin/bash

# App Icon Generator Script for VirtualMoney
# Requires: ImageMagick (convert command)

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

# Colors matching the app theme
BG_COLOR="#1A1A2E"
COIN_COLOR="#FFD700"
COIN_DARK="#B8860B"
ACCENT="#0F3460"

# Check if ImageMagick is installed
if ! command -v convert &> /dev/null; then
    echo "ImageMagick is not installed. Please install it:"
    echo "  macOS: brew install imagemagick"
    echo "  Ubuntu: sudo apt-get install imagemagick"
    exit 1
fi

# Create temp directory
TEMP_DIR=$(mktemp -d)
trap "rm -rf $TEMP_DIR" EXIT

echo "Generating app icons..."

# Generate base 1024x1024 icon
generate_base_icon() {
    local size=$1
    local output=$2

    convert -size ${size}x${size} xc:none \
        -fill "gradient:${BG_COLOR}-${ACCENT}" -draw "rectangle 0,0 ${size},${size}" \
        -fill "${COIN_COLOR}" \
        -draw "circle $((size/2)),$((size/2)) $((size/2)),$((size/8))" \
        -fill "${COIN_DARK}" \
        -draw "circle $((size/2)),$((size/2)) $((size/2)),$((size/6))" \
        -fill "${COIN_COLOR}" \
        -stroke "${COIN_DARK}" -strokewidth 3 \
        -font Helvetica-Bold -pointsize $((size/8)) \
        -gravity center -draw "text 0,0 '\$'" \
        "$output"
}

# Generate iOS icons
echo "Generating iOS icons..."
IOS_ICON_DIR="${PROJECT_DIR}/iosApp/iosApp/Assets.xcassets/AppIcon.appiconset"

# 1024x1024 for App Store
generate_base_icon 1024 "${IOS_ICON_DIR}/app-icon-1024.png"

echo "iOS icon generated: ${IOS_ICON_DIR}/app-icon-1024.png"

# Generate Android icons
echo "Generating Android icons..."
ANDROID_RES="${PROJECT_DIR}/composeApp/src/androidMain/res"

# Android icon sizes
declare -A ANDROID_SIZES=(
    ["mdpi"]=48
    ["hdpi"]=72
    ["xhdpi"]=96
    ["xxhdpi"]=144
    ["xxxhdpi"]=192
)

for density in "${!ANDROID_SIZES[@]}"; do
    size=${ANDROID_SIZES[$density]}
    output_dir="${ANDROID_RES}/mipmap-${density}"
    mkdir -p "$output_dir"

    generate_base_icon $size "${output_dir}/ic_launcher.png"

    # Round icon (same as square for now, Android will handle masking)
    cp "${output_dir}/ic_launcher.png" "${output_dir}/ic_launcher_round.png"

    echo "Generated ${density} icons (${size}x${size})"
done

echo ""
echo "Icon generation complete!"
echo ""
echo "Note: For production, consider using:"
echo "  - Android Studio's Image Asset Studio for better adaptive icons"
echo "  - Xcode's Asset Catalog for iOS icons"
echo "  - Or professional icon design tools like Figma/Sketch"
