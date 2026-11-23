package com.takaapoo.weatherer.ui.screens.radar

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import java.util.Locale

val radarMap =
    $$"""<!doctype html>
<html lang="en">
<head>
    <title>Open-Meteo Weather Map Layer - Temperature Example</title>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <link
        rel="stylesheet"
        href="https://unpkg.com/maplibre-gl@6.18.0/dist/maplibre-gl.css"
    />

    <style>
        html,
        body {
            margin: 0;
            padding: 0;
            width: 100%;
            height: 100%;
            overflow: hidden;
        }
        #map {
            position: fixed;
            inset: 0;
            width: 100%;
            height: 100%;
        }
        .maplibregl-ctrl-top-right {
            right: 0px !important;
            top: 100px !important;
            margin: -4px !important; /* Remove any default margins */
        }
        .maplibregl-ctrl button {
            background-color: #ff0000 !important;
        }
        .maplibregl-ctrl-group {
            border: 2px solid #ff0000 !important;
            border-radius: 8px;
            overflow: hidden !important;
            box-shadow: none !important;
        }
        .maplibregl-ctrl-group button + button {
            border-top: 1px solid #ff0000 !important;
        }
        .maplibregl-ctrl-group .maplibregl-ctrl-icon {
            filter: invert(1);
        }

    </style>
</head>
<body>
<div id="map"></div>
<script type="module">
    import * as maplibregl from 'https://unpkg.com/maplibre-gl@6.18.0/dist/maplibre-gl.mjs';
    maplibregl.setWorkerUrl(
        'https://appassets.androidplatform.net/assets/maplibre-gl-worker.mjs?v=6.18.0'
    );
    const OMWeatherMapLayer = await import(
        'https://unpkg.com/@openmeteo/weather-map-layer@0.3.2/dist/index.mjs' // x-release-please-version
    );
    
    const customColorScales = {
        temperature_2m: {
            type: 'breakpoint',
            unit: '°C',
            breakpoints: TEMPERATURE_breakpoints,
            colors: TEMPERATURE_colors
        },
        relative: {
            type: 'breakpoint',
            unit: '%',
            breakpoints: HUMIDITY_breakpoints,
            colors: HUMIDITY_colors
        },
        dew_point: null,
        precipitation_probability: {
            type: 'breakpoint',
            unit: '%',
            breakpoints: PRECIPITATIONPROBABILITY_breakpoints,
            colors: PRECIPITATIONPROBABILITY_colors
        },
        precipitation: {
            type: 'breakpoint',
            unit: 'mm',
            breakpoints: PRECIPITATION_breakpoints,
            colors: PRECIPITATION_colors
        },
        rain: null,
        showers: null,
        snowfall: null,
        cloud_cover: {
            type: 'breakpoint',
            unit: '%',
            breakpoints: CLOUDCOVER_breakpoints,
            colors: CLOUDCOVER_colors
        },
        visibility: {
            type: 'breakpoint',
            unit: 'm',
            breakpoints: VISIBILITY_breakpoints,
            colors: VISIBILITY_colors
        },
        wind: {
            type: 'breakpoint',
            unit: 'm/s',
            breakpoints: WIND_breakpoints,
            colors: WIND_colors
        },
        uv: {
            type: 'breakpoint',
            unit: '',
            breakpoints: UVINDEX_breakpoints,
            colors: UVINDEX_colors
        },
        freezing_level_height: {
            type: 'breakpoint',
            unit: 'm',
            breakpoints: FREEZINGLEVELHEIGHT_breakpoints,
            colors: FREEZINGLEVELHEIGHT_colors
        },
        pm10: {
            type: 'breakpoint',
            unit: 'μg/m³',
            breakpoints: PM10_breakpoints,
            colors: PM10_colors
        },
        pm2_5: {
            type: 'breakpoint',
            unit: 'μg/m³',
            breakpoints: PM2_5_breakpoints,
            colors: PM2_5_colors
        },
        carbon_monoxide: {
            type: 'breakpoint',
            unit: 'μg/m³',
            breakpoints: CO_breakpoints,
            colors: CO_colors
        },
        nitrogen_dioxide: {
            type: 'breakpoint',
            unit: 'μg/m³',
            breakpoints: NO2_breakpoints,
            colors: NO2_colors
        },
        sulphur_dioxide: {
            type: 'breakpoint',
            unit: 'μg/m³',
            breakpoints: SO2_breakpoints,
            colors: SO2_colors
        },
        ozone: {
            type: 'breakpoint',
            unit: 'μg/m³',
            breakpoints: Ozone_breakpoints,
            colors: Ozone_colors
        },
        
    };
    customColorScales.dew_point = customColorScales.temperature_2m;
    customColorScales.rain = customColorScales.precipitation;
    customColorScales.showers = customColorScales.precipitation;
    customColorScales.snowfall = customColorScales.precipitation;

    const omProtocolOptions = OMWeatherMapLayer.defaultOmProtocolSettings;
    omProtocolOptions.colorScales = customColorScales;
    maplibregl.addProtocol(
        'om',
        (params, abortController) => {
            return OMWeatherMapLayer.omProtocol(
                params,
                abortController,
                omProtocolOptions
            );
        }
    );
    const lightStyle = 'https://api.maptiler.com/maps/019fdc79-7a46-7267-8fcc-a695ba0577bb/style.json?key=FsJPLZYGDXzXfTCZDwyf';
    const darkStyle = 'https://api.maptiler.com/maps/01a1e2c8-5097-73cf-bf7c-8b105b516c36/style.json?key=FsJPLZYGDXzXfTCZDwyf';
    
    window.map = new maplibregl.Map({
        container: 'map',
        projection: 'globe',
        style: style,
        hash: true,
        center: [ centerLng, centerLat ],
        zoom: 4,
        dragRotate: false,        // Disables rotation via right-click and drag
        pitchWithRotate: false,
        touchPitch: false,
        aroundCenter: false       // Prevents rotation around the map center in v5
    });
    map.touchZoomRotate.disableRotation(); // Disables pinch-to-rotate only
    map.keyboard.disableRotation();     // Disables keyboard rotation (Shift + Arrow keys)
    
    window.currentLayerId = null;
    window.currentSourceId = null;
    window.currentOmUrl = null;
    window.weatherLayerRequestId = 0;
    window.elevationVisible = true;
    
    
    
    window.updateElevationVisible = function(visible) {
        window.elevationVisible = visible;
    }    
    window.setDarkStyle = function() {
        window.map.setStyle( darkStyle );
    }
    
    function waitForWeatherTilesLoaded(sourceId, requestId, timeoutMs = 90000) {
        console.log('[Weather] Waiting started:', sourceId);
        return new Promise((resolve, reject) => {
            let finished = false;
            let timeoutId;
            let checkIntervalId;

            const cleanup = () => {
                clearTimeout(timeoutId);
                clearInterval(checkIntervalId);

                window.map.off('sourcedata', onSourceData);
                window.map.off('idle', onIdle);
                window.map.off('error', onError);
                window.removeEventListener('offline', onOffline);
            };

            const finish = (success, value) => {
                console.log('[Weather] Promise settled:', sourceId, success, value, finished);
                if (finished) return;

                finished = true;
                cleanup();

                if (success) {
                    resolve(value);
                } else {
                    reject(value);
                }
            };

            const check = () => {
                if (finished) return;

                if (requestId !== window.weatherLayerRequestId) {
                    finish(true, false);
                    return;
                }

                if (navigator.onLine === false) {
                    finish(false, new Error('No network connection'));
                    return;
                }

                if (!window.map.getSource(sourceId)) {
                    finish(false, new Error('Source was removed'));
                    return;
                }

                if ( window.map.isSourceLoaded(sourceId) && window.map.areTilesLoaded() ) {
                    finish(true, true);
                    return;
                }
            };

            const onSourceData = event => {
                if (event.sourceId === sourceId) check();
            };

            const onIdle = () => check();

            const onError = event => {
                if (event && event.sourceId === sourceId) {
                    finish( false, event.error || new Error('Error loading source') );
                }
            };

            const onOffline = () => {
                finish(false, new Error('Network connection lost'));
            };

            if (navigator.onLine === false) {
                reject(new Error('No network connection'));
                return;
            }

            window.map.on('sourcedata', onSourceData);
            window.map.on('idle', onIdle);
            window.map.on('error', onError);
            window.addEventListener('offline', onOffline);

            timeoutId = setTimeout(() => {
                console.error( '[Weather] Timeout reached:', sourceId, requestId );
                finish( false, new Error('Timed out waiting for weather tiles') );
            }, timeoutMs);

            checkIntervalId = setInterval(check, 1000);
            check();
        });
    }
    
    window.removeWeatherLayer = function( layerId, sourceId ) {
        if ( layerId && window.map.getLayer(layerId) ) {
            window.map.removeLayer(layerId);
        }
        if ( sourceId && window.map.getSource(sourceId) ) {
            window.map.removeSource(sourceId);
        }
    }
    
    let darkMode
    window.updateWeatherLayer = function(variable, model, index, opacity, dark) {
        console.log('updateWeatherLayer: ', variable, model, dark);        
        return new Promise((resolve, reject) => {
            const requestId = ++window.weatherLayerRequestId;
            
            // If an earlier request was mid-transition, keep the currently displayed layer visible
            // while this request loads its tiles.
            if (window.currentLayerId && window.map.getLayer(window.currentLayerId) &&
                window.currentWeatherOpacity !== undefined) {
                window.map.setPaintProperty(window.currentLayerId, 'raster-opacity', window.currentWeatherOpacity);
            }
            const newOmUrl =
                'https://openmeteo.s3.amazonaws.com/data_spatial/' + encodeURIComponent(model) + '/latest.json' +
                '?time_step=valid_times_' + encodeURIComponent(index) + '&variable=' + encodeURIComponent(variable) +
                '&color_blend=true' + (dark ? '&dark=true' : '');
                
            const newSourceId = 'omSource_' + variable + '_' + requestId;
            const newLayerId = 'omLayer_' + variable + '_' + requestId;
            if (index == -1) {
                removeWeatherLayer( window.currentLayerId, window.currentSourceId );
                reject( new Error('Data not available for this time') );
                return;
            }
            
            const executeUpdate = async function() {
                try {
                    if ( requestId !== window.weatherLayerRequestId ) {
                        resolve('Request superseded');
                        return;
                    }
                    window.map.addSource(
                        newSourceId,
                        {
                            url: 'om://' + newOmUrl,
                            type: 'raster',
                            maxzoom: 12
                        }
                    );
                    window.map.addLayer(
                        {
                            id: newLayerId,
                            type: 'raster',
                            source: newSourceId,
                            paint: { 'raster-opacity': 0 }
                        },
                        (variable == 'cloud_cover') ? 'Bridge' : 'Hillshade'
                    );
//                    console.log('ids before wait: ', window.currentSourceId, window.currentLayerId);
                    const loaded = await waitForWeatherTilesLoaded( newSourceId, requestId );
//                    console.log('ids after wait: ', window.currentSourceId, window.currentLayerId);
                    if ( requestId !== window.weatherLayerRequestId ) {
                        removeWeatherLayer( newLayerId, newSourceId );
                        resolve( 'Request superseded' );
                        return;
                    }
                    if (!loaded) {
                        removeWeatherLayer( newLayerId, newSourceId );
                        resolve( 'Request superseded' );
                        return;
                    }
                    const previousLayerId = window.currentLayerId;
                    const previousSourceId = window.currentSourceId;
                    const transitionDuration = 400;
                    window.map.setPaintProperty(newLayerId, 'raster-opacity-transition', {
                        duration: transitionDuration
                    });
                    window.map.setPaintProperty(newLayerId, 'raster-opacity', opacity);
                    if (previousLayerId && window.map.getLayer(previousLayerId)) {
                        window.map.setPaintProperty(previousLayerId, 'raster-opacity-transition', {
                            duration: transitionDuration
                        });
                        window.map.setPaintProperty(previousLayerId, 'raster-opacity', 0);
                    }
                    await new Promise(resolveTransition => setTimeout(resolveTransition, transitionDuration));
                    if (requestId !== window.weatherLayerRequestId) {
                        // A newer request owns the transition now; remove only this request's layer.
                        removeWeatherLayer(newLayerId, newSourceId);
                        resolve('Request superseded');
                        return;
                    }
                    removeWeatherLayer(previousLayerId, previousSourceId);
                    window.currentLayerId = newLayerId;
                    window.currentSourceId = newSourceId;
                    window.currentOmUrl = newOmUrl;
                    window.currentWeatherOpacity = opacity;
//                    window.map.triggerRepaint();
                    updateAndroidQuantity(variable);
                    resolve( 'Weather layer updated to: ' + variable );
                } catch (error) {
                    removeWeatherLayer( newLayerId, newSourceId );
                    if ( requestId !== window.weatherLayerRequestId ) {
                        resolve( 'Request superseded' );
                        return;
                    }
                    updateAndroidQuantity(null);
                    snackbarToAndroid(error.message);
                    reject(error);
                }
            };
            if (window.map.isStyleLoaded()) {
                executeUpdate();
            } else {
                window.map.once('styledata', executeUpdate);
            }
        });
    };
    
    map.on( "idle", () => {
        sendToAndroid( "updateLoadingIndicatorVisible", false );
    });
//    map.on( "sourcedataloading", (event) => {
//        if ( event.sourceId && event.sourceId.startsWith('omSource_') ) {
//            sendToAndroid( "updateLoadingIndicatorVisible", true );
//        }
//    });
//    map.once( "styledata", () => {
//        setTimeout(() => {
//            map.addSource('terrainSource', {
//                type: 'raster-dem',
//                url: 'https://tiles.mapterhorn.com/tilejson.json'
//            });
//            map.setTerrain({
//                source: 'terrainSource',
//                exaggeration: 1
//            });
//        }, 50);
//    });
    const onLoad = async () => {
        try {
            const zoom = Math.min(Math.round(map.getZoom()), 12);
            quantityValue(zoom, result => {
//                console.log( 'Loaded value =', result.value, 'elevation =', result.elevation );      
            });
        } catch (error) {
            console.error( 'Error in move handler:', error );
        }
    };
    map.on( 'load', onLoad );
    // make tiles in this new zoom ready
    map.on( 'zoomend', onLoad );
    
    map.addControl(
        new maplibregl.NavigationControl({
            visualizePitch: false,
            showZoom: true,
            showCompass: false
        }),
        'top-right'
    );
    const onDrag = async () => {
        try {
            const rawZoom = map?.getZoom();
            const zoom = window.elevationVisible && rawZoom
              ? Math.min(Math.round(rawZoom), 12)
              : null;            
              
            quantityValue(zoom, result => {
                sendToAndroid( "updateQuantityValue", result.value, result.elevation );            
            });
        } catch (error) {
            console.error( 'Error in move handler:', error );
        }
    };
    map.on( 'drag', onDrag );
    
    window.map.once('style.load', () => {
        window.map.setProjection({
            type: 'globe'
        });
    });    
    map.on( 'moveend', async () => {
        try {
            quantityValue(12, result => {
                sendToAndroid( "updateQuantityValue", result.value, result.elevation );      
            });
        } catch (error) {
            console.error( 'Error in getMapElevation:', error );
        }
    });
    
    
    function sendToAndroid( functionName, value, elevation = null ) {
        const message = {
            function: functionName,
            value: value,
            elevation: elevation
        };
        AndroidBridge.postMessage( JSON.stringify(message) );
    }
    function updateAndroidQuantity( variable ) {
        const message = {
            quantity: variable
        };
        AndroidQuantityBridge.postMessage( JSON.stringify(message) );
        if (variable !== null) {
            quantityValue(12, result => {
                sendToAndroid( "updateQuantityValue", result.value, result.elevation );      
            });
        }
    }
    function snackbarToAndroid( text ) {
        const message = {
            text: text
        };
        AndroidSnackbarBridge.postMessage( JSON.stringify(message) );
    }
    
    
    
    
    async function getQuantityValue(lng, lat) {
        try {
            if (!window.currentOmUrl) { return null; }
            const result = await OMWeatherMapLayer.getValueFromLatLong( lat, lng, 'om://' + window.currentOmUrl );
            return result?.value ?? null;
        } catch (error) {
            return null;
        }
    }
    async function getMapElevation(lng, lat, zoom, fetchNeighborTiles = true) {
        try {
            return await getElevationFromMapternhorn( lng, lat, zoom, fetchNeighborTiles );
        } catch (error) {
            return null;
        }
    }
    
    let value = null;
    let elevation = null;

    async function quantityValue(zoom, onResult) {
        const { lng, lat } = map.getCenter();    
        getQuantityValue(lng, lat).then(result => {
            value = result;
            onResult({ value, elevation });
        });
    
        if (zoom !== null) { 
            getMapElevation(lng, lat, zoom).then(result => {
                elevation = result;
                onResult({ value, elevation });
            });
        }
//        const elevation2 = map.queryTerrainElevation(
//            { lng, lat },
//            { exaggerated: false }
//        ) ?? null;

    }
    
    window.goToLocation = function(lng, lat) {
        map.flyTo({
            center: [ lng, lat ],
            zoom: 7,
            bearing: 0,     
            speed: 0.5,
            essential: true
        });
    } 
    

    
    
    
    
    
    function lngLatToPixel(lng, lat, zoom, tileSize = 512) {
        const n = 2 ** zoom;

        const x = (lng + 180) / 360 * n;
        const latRad = lat * Math.PI / 180;
        const y = (1 - Math.asinh(Math.tan(latRad)) / Math.PI) / 2 * n;

        const tileX = Math.floor(x);
        const tileY = Math.floor(y);
        return {
            tileX,
            tileY,
            pixelX: (x - tileX) * tileSize,
            pixelY: (y - tileY) * tileSize
        };
    }
    function getTerrainTileUrl(z, x, y) {
//        return `https://tiles.mapterhorn.com/${z}/${x}/${y}.webp`;
        return `https://api.maptiler.com/tiles/terrain-rgb-v2/${z}/${x}/${y}.webp?key=FsJPLZYGDXzXfTCZDwyf`;
    }
    function terrariumToElevation(r, g, b) {
        return (r * 256 + g + b / 256) - 32768;
    }
    function mapTilerTerrainRgbToElevation(r, g, b) {
        return -10000 +
            ((r * 256 * 256 + g * 256 + b) * 0.1);
    }
    
    const MAX_TERRAIN_TILE_CACHE_SIZE = 100;
    const terrainTileCache = new Map();
    
    async function getTerrainTile(zoom, tileX, tileY, fetchNeighborTiles = true) {
        const key = `${zoom}/${tileX}/${tileY}`;    
        const cached = terrainTileCache.get(key);    
        if (cached) {
            // Mark this tile as recently used.
            terrainTileCache.delete(key);
            terrainTileCache.set(key, cached);
            if (fetchNeighborTiles) {
                prefetchNeighborTiles( zoom, tileX, tileY );
            }
    
            return cached;
        }
    
        const promise = loadTerrainTile( zoom, tileX, tileY ); 
        addTerrainTileToCache( key, promise );    

        if (fetchNeighborTiles) {
            prefetchNeighborTiles( zoom, tileX, tileY );
        }    
        try {
            return await promise;
        } catch (error) {
            terrainTileCache.delete(key);
            throw error;
        }
    }
    function addTerrainTileToCache(key, promise) {
        if (terrainTileCache.size >= MAX_TERRAIN_TILE_CACHE_SIZE) {
            const oldestKey = terrainTileCache.keys().next().value;
            terrainTileCache.delete(oldestKey);
        }
        terrainTileCache.set(key, promise);
    }
    function prefetchNeighborTiles(zoom, tileX, tileY) {
        const n = 2 ** zoom;
        for (let dy = -1; dy <= 1; dy++) {
            for (let dx = -1; dx <= 1; dx++) {
                if (dx === 0 && dy === 0) {
                    continue;
                }

                let x = tileX + dx;
                let y = tileY + dy;

                // Wrap horizontally.
                x = (x + n) % n;

                // No wrapping vertically.
                if (y < 0 || y >= n) {
                    continue;
                }

                const key = `${zoom}/${x}/${y}`;
                if (terrainTileCache.has(key)) {
                    continue;
                }
                const promise = loadTerrainTile( zoom, x, y );
                addTerrainTileToCache(key, promise);

                promise.catch(() => {
                    if (terrainTileCache.get(key) === promise) {
                        terrainTileCache.delete(key);
                    }
                });
            }
        }
    }
    
    async function loadTerrainTile(zoom, tileX, tileY) {
        const url = getTerrainTileUrl( zoom, tileX, tileY );
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error(
                `Failed to load terrain tile: ${response.status}`
            );
        }

        const blob = await response.blob();
        const bitmap = await createImageBitmap(blob);

        const canvas = document.createElement('canvas');
        canvas.width = bitmap.width;
        canvas.height = bitmap.height;

        const ctx = canvas.getContext('2d', {
            willReadFrequently: true
        });

        ctx.drawImage(bitmap, 0, 0);
        const imageData = ctx.getImageData( 0, 0, bitmap.width, bitmap.height );
        bitmap.close();
        return imageData;
    }
    
    async function getElevationFromMapternhorn(lng, lat, zoom = 12, fetchNeighborTiles = true) {
        const { tileX, tileY, pixelX, pixelY } = lngLatToPixel(lng, lat, zoom);
        const imageData = await getTerrainTile( zoom, tileX, tileY, fetchNeighborTiles );

        const x0 = Math.floor(pixelX);
        const y0 = Math.floor(pixelY);
        const x1 = Math.min(x0 + 1, imageData.width - 1);
        const y1 = Math.min(y0 + 1, imageData.height - 1);
        const fx = pixelX - x0;
        const fy = pixelY - y0;

        function getElevationAtPixel(x, y) {
            const index = (y * imageData.width + x) * 4;
            return mapTilerTerrainRgbToElevation(
                imageData.data[index],
                imageData.data[index + 1],
                imageData.data[index + 2]
            );
        }

        const e00 = getElevationAtPixel(x0, y0);
        const e10 = getElevationAtPixel(x1, y0);
        const e01 = getElevationAtPixel(x0, y1);
        const e11 = getElevationAtPixel(x1, y1);

        const elevation =
            e00 * (1 - fx) * (1 - fy) +
            e10 * fx       * (1 - fy) +
            e01 * (1 - fx) * fy +
            e11 * fx       * fy;
            
        return elevation;
    }
    
    
    sendToAndroid( "pageReady", null );    
    
</script>
</body>
</html>
""".trimIndent()


fun modifiedRadarMap(
    topPadding: Float,
    valueBarHeight: Float,
    borderColor: Color,
    surfaceColor: Color,
    isDark: Boolean,
    centerLat: Float,
    centerLng: Float,
): String {
    val adoptedBorderColor = borderColor.toArgb().toHexString().substring(2)
    val adoptedSurfaceColor = surfaceColor.toArgb().toHexString().substring(2)
    val darkStyle = if (isDark) "darkStyle" else "lightStyle"

    var modifiedString = radarMap.replace(
        oldValue = "top: 100px !important;",
        newValue = "top: ${topPadding + valueBarHeight}px !important;"
    ).replace(
        oldValue = "border: 2px solid #ff0000",
        newValue = "border: 2px solid #$adoptedBorderColor"
    ).replace(
        oldValue = "border-top: 1px solid #ff0000",
        newValue = "border-top: 1px solid #$adoptedBorderColor"
    ).replace(
        oldValue = "background-color: #ff0000",
        newValue = "background-color: #$adoptedSurfaceColor"
    ).replace(
        oldValue = "filter: invert(1);",
        newValue = "filter: invert(${if (isDark) 1 else 0});"
    ).replace(
        oldValue = "style: style",
        newValue = "style: $darkStyle"
    ).replace(
        oldValue = "centerLat",
        newValue = "$centerLat"
    ).replace(
        oldValue = "centerLng",
        newValue = "$centerLng"
    )

    RadarWeatherQuantity.entries.forEach { quantity ->
        val breakPoints = RadarWeatherQuantity.colorBreakpoints(quantity)
            .joinToString(",", "[", "]") {
                "%.3f".format(Locale.US, it)
            }
        val lightColors = RadarWeatherQuantity.colorScale(quantity, false)
        val darkColors = RadarWeatherQuantity.colorScale(quantity, true)
        val colors = if (lightColors == darkColors) {
            lightColors.joinToString(",", "[", "]") { color ->
                val intColor = color.toArgb()
                "[${intColor.red}, ${intColor.green}, ${intColor.blue}, ${color.alpha}]"
            }
        } else {
            val light = lightColors.joinToString(",", "[", "]") { color ->
                val intColor = color.toArgb()
                "[${intColor.red}, ${intColor.green}, ${intColor.blue}, ${color.alpha}]"
            }
            val dark = darkColors.joinToString(",", "[", "]") { color ->
                val intColor = color.toArgb()
                "[${intColor.red}, ${intColor.green}, ${intColor.blue}, ${color.alpha}]"
            }
            "{ light: $light , dark: $dark }"
        }

        modifiedString = modifiedString.replace(
            oldValue = "${quantity.name}_breakpoints",
            newValue = breakPoints
        ).replace(
            oldValue = "${quantity.name}_colors",
            newValue = colors
        )
    }

    return modifiedString
}