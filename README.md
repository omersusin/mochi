# Mochi — cute alarms & ringtones

Warm, hand-tuned sounds for cold mornings. 24 gentle alarms, dreamy ringtones
and soft notifications. Preview anything, keep favorites, and set any sound as
your system default.

- Offline, no account, no ads
- 24 bundled tones, ~4 MB total
- Favorites with DataStore, system defaults via MediaStore

## Set a default

Tap **Set** on any sound, pick Alarm / Ringtone / Notification. Android asks
once for the “modify system settings” permission, then it just works.

## Build

CI builds every push (`Android CI` workflow): lint + debug APK always; signed
release APK (R8 + minify) when keystore secrets are present.
