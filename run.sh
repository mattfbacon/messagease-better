#!/bin/sh
set +euxo pipefail
./gradlew build || exit $?
./gradlew installDebug || exit $?
adb shell -- am start -n nz.felle.messageasebetter/nz.felle.messageasebetter.SettingsActivity || exit $?
adb logcat -e nz.felle.messageasebetter -T "$(date +'%m-%d %H:%M:%S.%3N')"
