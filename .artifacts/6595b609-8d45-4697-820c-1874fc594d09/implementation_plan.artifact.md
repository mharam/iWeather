# Implementation Plan - Fix JavaScript Layer Loading Issue in Radar WebView

The issue where weather layers fail to load when the variable is changed quickly is caused by a race condition and a "blocking" wait mechanism in the JavaScript code. Specifically, the `waitForWeatherTilesLoaded` function only checks for supersession when its specific source generates data or when the map becomes `idle`. If multiple sources are added in quick succession, the map stays busy, `idle` is delayed, and `areTilesLoaded()` (which is a global check) remains false because earlier, superseded sources are still loading and haven't been removed yet.

## User Review Required

> [!IMPORTANT]
> Based on your feedback, I have removed the redundant safety checks (Hillshade existence and initial debouncing) and will focus on the core logic: Fast Supersession and Immediate Cleanup.

## Proposed Changes

### [Radar Data]

#### [MODIFY] [RadarData.kt](file:///D:/My Apps/iWeather/app2/app/src/main/java/com/takaapoo/weatherer/ui/screens/radar/RadarData.kt)

I will improve the JavaScript logic in `RadarData.kt` to handle rapid updates more efficiently:

1.  **Fast Supersession:** Modify `waitForWeatherTilesLoaded` to check for supersession on *every* `sourcedata` event, not just those from its own source. This ensures that a superseded request exits and removes its resources as soon as the map shows any activity from a newer request.
2.  **Immediate Cleanup:** Ensure that superseded requests resolve immediately and remove their sources/layers, allowing `areTilesLoaded()` to become true for the latest request much faster.

```javascript
// Key changes in RadarData.kt JS:

function waitForWeatherTilesLoaded(sourceId, requestId) {
    return new Promise((resolve, reject) => {
        // ...
        const check = () => {
            if (finished) return;
            // CHECK SUPERSESSION FIRST
            if (requestId !== window.weatherLayerRequestId) {
                cleanup();
                resolve(false);
                return;
            }
            // ... existing source/tile check ...
        };
        const onSourceData = event => {
            // Check for supersession on ANY source data event
            check();
        };
        // ...
    });
}
```

## Verification Plan

### Manual Verification
1.  **Rapid Variable Switching:** Deploy the app and navigate to the Radar screen. Quickly switch between different weather variables (e.g., Temperature -> Humidity -> Precipitation) before the previous ones finish loading.
2.  **Observe Loading:** Verify that the loading indicator (LinearProgressIndicator) behaves correctly and that the final selected variable eventually loads its map layer.
3.  **Check for Residual Layers:** Ensure that only one weather layer is visible on the map at a time and that old layers are correctly removed.
4.  **Log Analysis:** Check WebView console logs (via Logcat "WebViewConsole") to ensure no "Error updating weather layer" or "Source already exists" errors are occurring.
