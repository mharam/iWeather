# WebView Improvements and Emulator Troubleshooting

The user is experiencing an issue where the map doesn't render on emulators (only controls show), while it works fine on physical devices. This is a common symptom of WebGL initialization failure. Additionally, the user requested improvements for a snippet of `WebView` settings.

## User Review Required

> [!IMPORTANT]
> **WebGL Support on Emulators**: The primary cause of the map not rendering is likely the Emulator's Graphics settings. MapLibre GL JS (used in your HTML) requires WebGL, which often fails in emulators using "Software" rendering. Please check your AVD settings as described below.

> [!WARNING]
> **Security Settings**: The requested `MIXED_CONTENT_ALWAYS_ALLOW` and `allowFileAccess = true` pose security risks. Since your map resources are all served via HTTPS, I recommend using more restrictive settings.

## Proposed Changes

### [Component Name] Radar Screen

#### [MODIFY] [RadarScreen.kt](file:///D:/My Apps/iWeather/app2/app/src/main/java/com/takaapoo/weatherer/ui/screens/radar/RadarScreen.kt)
- Refactor `WebView` settings to use `apply`.
- Improve security by disabling unnecessary file access and mixed content.
- Ensure `domStorageEnabled` is active (required for MapLibre).
- Add `safeBrowsingEnabled` for production safety.

## Verification Plan

### Manual Verification
1. **Emulator Graphics**:
   - Open AVD Manager -> Edit Device -> Emulated Performance -> Graphics.
   - Set to **Hardware - GLES 2.0**.
   - Restart the emulator and run the app.
2. **Logcat Monitoring**:
   - Filter Logcat for `WebViewConsole`.
   - Look for errors like `Failed to initialize WebGL` or `Context lost`.
3. **Settings Verification**:
   - Verify that the map still loads correctly on a physical device with the new, more restrictive security settings.
