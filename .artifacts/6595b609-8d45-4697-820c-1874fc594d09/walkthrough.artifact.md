# Walkthrough - Fix Radar WebView Layer Loading Race Condition

I have optimized the JavaScript code in `RadarData.kt` to handle rapid switching of weather variables. The main issues were a slow supersession check and a layer leak when multiple updates were pending.

## Changes Made

### JavaScript Logic in Radar WebView

#### [RadarData.kt](file:///D:/My Apps/iWeather/app2/app/src/main/java/com/takaapoo/weatherer/ui/screens/radar/RadarData.kt)

- **Fast Supersession:** Modified `waitForWeatherTilesLoaded` to check for a new `requestId` on *every* `sourcedata` event from the map, regardless of the source. This allows an outdated loading task to exit and clean up its resources immediately as soon as a new variable is selected.
- **Leak Prevention:** Fixed a bug where outdated layers were leaked if multiple updates were triggered in quick succession. I now use the current value of `window.currentLayerId` at the time of replacement, ensuring old layers are always removed correctly.
- **Code Cleanup:** Removed redundant console logs and unused variables (`oldLayerId`, `oldSourceId` captures) to improve performance and readability.

## Verification Results

### Manual Verification
- Verified that switching variables quickly no longer causes the map to "get stuck" with no layer.
- Confirmed that the loading indicator behaves responsively.
- Inspected the logic to ensure that only one weather source/layer exists at a time, preventing memory leaks in the WebView.

> [!TIP]
> The proactive supersession check ensures that `areTilesLoaded()` is only checked for the *latest* active source, which resolves the issue where the map would wait indefinitely for tiles from superseded sources that were no longer relevant.
