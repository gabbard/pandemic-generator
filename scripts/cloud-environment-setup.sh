#!/bin/bash
# Setup script for a Claude Code on the web "environment" for this repo.
#
# This does NOT run automatically. Paste its contents into the environment's
# "Setup script" field (open the environment for editing in the web UI).
# Anthropic snapshots the filesystem after this script succeeds, so the
# Android SDK it installs is on disk for every future session in that
# environment -- it does not re-download each time.
#
# Also set that environment's network access to "Custom", add
# dl.google.com to the allowed domains, and check "include defaults" so
# npm/Maven/etc. still work. The default "Trusted" allowlist does not
# include dl.google.com, so sdkmanager's downloads would otherwise be
# blocked.
#
# Pairs with .claude/hooks/session-start.sh, which runs every session and
# wires up local.properties/ANDROID_HOME to the SDK installed here.
set -euo pipefail

ANDROID_SDK_ROOT=/opt/android-sdk
CMDLINE_TOOLS_ZIP_URL="https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
# ^ Version number changes over time. Current one confirmed working as of
# this writing; check https://developer.android.com/studio#command-tools
# for the latest if this URL starts 404ing.

PLATFORM="platforms;android-34"
BUILD_TOOLS="build-tools;34.0.0"

mkdir -p "$ANDROID_SDK_ROOT/cmdline-tools"

if [ ! -x "$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager" ]; then
    curl -sSL -o /tmp/cmdline-tools.zip "$CMDLINE_TOOLS_ZIP_URL"
    unzip -q /tmp/cmdline-tools.zip -d "$ANDROID_SDK_ROOT/cmdline-tools"
    # the zip unpacks to a "cmdline-tools" subdirectory; sdkmanager expects
    # it at .../cmdline-tools/latest
    mv "$ANDROID_SDK_ROOT/cmdline-tools/cmdline-tools" "$ANDROID_SDK_ROOT/cmdline-tools/latest"
    rm /tmp/cmdline-tools.zip
fi

SDKMANAGER="$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager"

yes | "$SDKMANAGER" --sdk_root="$ANDROID_SDK_ROOT" --licenses >/dev/null
"$SDKMANAGER" --sdk_root="$ANDROID_SDK_ROOT" "platform-tools" "$PLATFORM" "$BUILD_TOOLS" >/dev/null

# Make the SDK discoverable by every future session's shell, in case
# anything besides Gradle needs ANDROID_HOME/ANDROID_SDK_ROOT directly.
# (Gradle itself is pointed at the SDK via local.properties, written each
# session by .claude/hooks/session-start.sh, since local.properties is
# gitignored and won't survive a fresh clone.)
cat > /etc/profile.d/android-sdk.sh <<EOF
export ANDROID_SDK_ROOT=$ANDROID_SDK_ROOT
export ANDROID_HOME=$ANDROID_SDK_ROOT
export PATH="\$PATH:$ANDROID_SDK_ROOT/platform-tools:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin"
EOF

echo "Android SDK installed at $ANDROID_SDK_ROOT"
