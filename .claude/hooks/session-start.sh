#!/bin/bash
set -euo pipefail

if [ "${CLAUDE_CODE_REMOTE:-}" != "true" ]; then
    exit 0
fi

ANDROID_SDK_ROOT=/opt/android-sdk

if [ -d "$ANDROID_SDK_ROOT" ]; then
    echo "sdk.dir=$ANDROID_SDK_ROOT" > "$CLAUDE_PROJECT_DIR/local.properties"
    {
        echo "export ANDROID_SDK_ROOT=$ANDROID_SDK_ROOT"
        echo "export ANDROID_HOME=$ANDROID_SDK_ROOT"
        echo "export PATH=\"\$PATH:$ANDROID_SDK_ROOT/platform-tools:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin\""
    } >> "$CLAUDE_ENV_FILE"
else
    echo "Android SDK not found at $ANDROID_SDK_ROOT -- see scripts/cloud-environment-setup.sh" \
        "and set it as this environment's setup script to enable Android builds." >&2
fi
