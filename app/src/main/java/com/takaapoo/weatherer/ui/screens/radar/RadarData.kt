package com.takaapoo.weatherer.ui.screens.radar

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
    
//    maplibregl.setRTLTextPlugin(
//        'https://unpkg.com/@mapbox/mapbox-gl-rtl-text@0.3.0/dist/mapbox-gl-rtl-text.js',
//        true
//    );
    const customColorScales = {
        temperature_2m: {
            type: 'breakpoint',
            unit: '°C',
            breakpoints: [
                -80, -65, -50, -40, -32, -28, -24, -20, -17.5, -15, -12.5, -10, -8, -6, -4, -2,
                0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34, 36, 38, 40, 42, 44, 46, 48, 50
            ],
            colors: {
                light: [
                    [74, 13, 0, 1],
                    [130, 1, 29, 1],
                    [185, 4, 114, 1],
                    [221, 6, 193, 1],
                    [207, 6, 241, 1],
                    [163, 5, 243, 1],
                    [118, 4, 246, 1],
                    [71, 3, 249, 1],
                    [41, 2, 250, 1],
                    [11, 1, 252, 1],
                    [1, 22, 253, 1],
                    [0, 52, 255, 1],
                    [35, 99, 251, 1],
                    [69, 139, 247, 1],
                    [102, 171, 245, 1],
                    [134, 197, 245, 1],
                    [114, 232, 165, 1],
                    [78, 232, 133, 1],
                    [40, 233, 96, 1],
                    [17, 220, 61, 1],
                    [9, 191, 36, 1],
                    [4, 160, 15, 1],
                    [0, 128, 0, 1],
                    [40, 160, 0, 1],
                    [96, 192, 0, 1],
                    [167, 223, 0, 1],
                    [255, 255, 0, 1],
                    [255, 237, 0, 1],
                    [255, 219, 0, 1],
                    [255, 201, 0, 1],
                    [255, 183, 0, 1],
                    [255, 165, 0, 1],
                    [255, 138, 0, 1],
                    [255, 110, 0, 1],
                    [255, 83, 0, 1],
                    [255, 55, 0, 1],
                    [255, 27, 0, 1],
                    [255, 0, 0, 1],
                    [228, 0, 10, 1],
                    [201, 0, 18, 1],
                    [174, 0, 23, 1],
                    [147, 0, 26, 1]
                ],
                dark: [
                    [255, 0, 207, 1],
                    [220, 0, 191, 1],
                    [190, 0, 163, 1],
                    [159, 0, 138, 1],
                    [148, 0, 173, 1],
                    [115, 0, 174, 1],
                    [82, 0, 175, 1],
                    [49, 0, 176, 1],
                    [28, 0, 176, 1],
                    [7, 0, 177, 1],
                    [0, 15, 178, 1],
                    [0, 36, 178, 1],
                    [0, 59, 200, 1],
                    [0, 87, 221, 1],
                    [0, 117, 243, 1],
                    [10, 149, 255, 1],
                    [0, 242, 105, 1],
                    [0, 217, 78, 1],
                    [0, 191, 55, 1],
                    [0, 166, 36, 1],
                    [0, 140, 21, 1],
                    [0, 115, 8, 1],
                    [0, 90, 0, 1],
                    [28, 112, 0, 1],
                    [67, 134, 0, 1],
                    [117, 156, 0, 1],
                    [178, 178, 0, 1],
                    [178, 166, 0, 1],
                    [178, 153, 0, 1],
                    [178, 141, 0, 1],
                    [178, 128, 0, 1],
                    [178, 116, 0, 1],
                    [178, 97, 0, 1],
                    [178, 77, 0, 1],
                    [178, 58, 0, 1],
                    [178, 38, 0, 1],
                    [178, 19, 0, 1],
                    [178, 0, 0, 1],
                    [188, 0, 15, 1],
                    [203, 0, 20, 1],
                    [229, 0, 33, 1],
                    [255, 0, 51, 1]
                ]
            }
        },
        relative: {
            type: 'breakpoint',
            unit: '%',
            breakpoints: [0, 10, 20, 30, 40, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95],
            colors: [
                [0, 128, 0, 0],
                [0, 128, 0, 0.1],
                [0, 128, 0, 0.2],
                [0, 128, 0, 0.3],
                [0, 128, 0, 0.4],
                [0, 128, 0, 0.5],
                [0, 141, 28, 0.667],
                [0, 153, 61, 0.833],
                [0, 166, 100, 1],
                [0, 179, 143, 1],
                [0, 192, 192, 1],
                [0, 163, 204, 1],
                [0, 130, 217, 1],
                [0, 92, 230, 1],
                [0, 48, 242, 1]
            ]
        },
        dew_point: null,
        precipitation_probability: {
            type: 'breakpoint',
            unit: '%',
            breakpoints: [0, 1, 5, 10, 20, 30, 45, 60, 75, 100],
            colors: [
                [0, 0, 0, 0],
                [3, 47, 74, 0.2],
                [135, 206, 250, 0.357],
                [91, 149, 230, 0.5],
                [63, 236, 180, 0.567],
                [81, 245, 32, 0.633],
                [253, 188, 18, 0.748],
                [248, 122, 71, 0.867],
                [247, 80, 80, 0.948],
                [144, 0, 0, 1]
            ]
        },
        precipitation: {
            type: 'breakpoint',
            unit: 'mm',
            breakpoints: [0, 0.1, 0.5, 1, 2, 3, 4.5, 6, 8, 10, 12, 16, 20, 25, 30, 40, 60, 85, 125],
            colors: [
                [243, 250, 255, 0],
                [135, 206, 250, 0.4],
                [76, 136, 251, 0.6],
                [0, 0, 255, 0.622],
                [4, 115, 227, 0.667],
                [7, 200, 200, 0.711],
                [9, 161, 47, 0.778],
                [31, 163, 9, 0.826],
                [87, 192, 7, 0.872],
                [161, 223, 4, 0.911],
                [255, 255, 0, 0.943],
                [255, 198, 0, 0.986],
                [255, 142, 0, 1],
                [255, 71, 0, 1],
                [255, 0, 0, 1],
                [219, 0, 13, 1],
                [147, 0, 26, 1],
                [122, 0, 22, 1],
                [83, 0, 15, 1]
            ]
        },
        rain: null,
        showers: null,
        snowfall: null,
        cloud_cover: {
            type: 'breakpoint',
            unit: '%',
            breakpoints: [ 0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100 ],
            colors: {
                light: [
                    [11, 18, 32, 0],
                    [31, 43, 64, 0.141],
                    [59, 71, 89, 0.4],
                    [89, 100, 116, 0.475],
                    [120, 131, 148, 0.55],
                    [156, 163, 175, 0.625],
                    [177, 183, 193, 0.7],
                    [198, 203, 210, 0.775],
                    [214, 219, 226, 0.85],
                    [226, 232, 239, 0.925],
                    [241, 245, 249, 1]
                ],
                dark: [
                    [255, 255, 255, 0],
                    [249, 251, 253, 0.141],
                    [244, 247, 250, 0.4],
                    [233, 239, 244, 0.475],
                    [220, 226, 233, 0.55],
                    [209, 213, 219, 0.625],
                    [188, 193, 202, 0.7],
                    [166, 173, 184, 0.775],
                    [138, 147, 162, 0.85],
                    [84, 85, 93, 0.925],
                    [31, 35, 33, 1]
                ]
            }
        },
        visibility: {
            type: 'breakpoint',
            unit: 'm',
            breakpoints: [0, 50, 100, 200, 500, 1000, 2000, 4000, 7000, 10000, 15000, 20000, 30000, 500000],
            colors: [
                [106, 62, 0, 1],
                [121, 71, 0, 1],
                [136, 81, 0, 1],
                [166, 101, 0, 1],
                [255, 165, 0, 1],
                [241, 192, 0, 1],
                [192, 213, 0, 1],
                [47, 156, 0, 0.95],
                [0, 161, 117, 0.8],
                [0, 39, 211, 0.5],
                [62, 154, 255, 0.25],
                [167, 235, 255, 0],
                [255, 255, 255, 0],
                [255, 255, 255, 0]
            ]
        },
        wind: {
            type: 'breakpoint',
            unit: 'm/s',
            breakpoints: [0, 0.5, 1, 2, 3, 4, 6, 8, 10, 12.5, 15, 17.5, 20, 25, 30, 40, 50, 60],
            colors: [
                [255, 255, 255, 0],
                [194, 232, 255, 0.075],
                [133, 209, 255, 0.15],
                [12, 162, 255, 0.3],
                [0, 198, 137, 0.4],
                [0, 128, 0, 0.5],
                [96, 192, 0, 0.7],
                [255, 255, 0, 1],
                [255, 229, 0, 1],
                [255, 197, 0, 1],
                [255, 165, 0, 1],
                [242, 130, 25, 1],
                [231, 107, 48, 1],
                [212, 91, 91, 1],
                [224, 63, 63, 1],
                [255, 0, 0, 1],
                [192, 0, 0, 1],
                [129, 0, 0, 1]
            ]
        },
        uv: {
            type: 'breakpoint',
            unit: '',
            breakpoints: [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12],
            colors: [
                [101, 141, 27, 1],
                [132, 189, 0, 1],
                [151, 215, 0, 1],
                [247, 234, 72, 1],
                [252, 227, 0, 1],
                [255, 205, 0, 1],
                [236, 161, 84, 1],
                [255, 130, 0, 1],
                [239, 51, 64, 1],
                [218, 41, 28, 1],
                [191, 13, 62, 1],
                [75, 30, 136, 1],
                [98, 53, 159, 1]
            ]
        },
        freezing_level_height: {
            type: 'breakpoint',
            unit: 'm',
            breakpoints: [
                -4800, -3600, -2400, -1600, -960, -640, -320, 0, 200, 400, 600, 800, 960, 1120, 
                1280, 1440, 1600, 1760, 1920, 2080, 2240, 2400, 2560, 2720, 2880, 3040, 3200, 3360, 
                3520, 3680, 3840, 4000, 4160, 4320, 4480, 4640, 4800, 4960, 5120, 5280, 5440, 5600
            ],
            colors: [
                [74, 13, 0, 1],
                [130, 1, 29, 1],
                [185, 4, 114, 1],
                [221, 6, 193, 1],
                [207, 6, 241, 1],
                [163, 5, 243, 1],
                [118, 4, 246, 1],
                [71, 3, 249, 1],
                [41, 2, 250, 1],
                [11, 1, 252, 1],
                [1, 22, 253, 1],
                [0, 52, 255, 1],
                [35, 99, 251, 1],
                [69, 139, 247, 1],
                [102, 171, 245, 1],
                [134, 197, 245, 1],
                [114, 232, 165, 1],
                [78, 232, 133, 1],
                [40, 233, 96, 1],
                [17, 220, 61, 1],
                [9, 191, 36, 1],
                [4, 160, 15, 1],
                [0, 128, 0, 1],
                [40, 160, 0, 1],
                [96, 192, 0, 1],
                [167, 223, 0, 1],
                [255, 255, 0, 1],
                [255, 237, 0, 1],
                [255, 219, 0, 1],
                [255, 201, 0, 1],
                [255, 183, 0, 1],
                [255, 165, 0, 1],
                [255, 138, 0, 1],
                [255, 110, 0, 1],
                [255, 83, 0, 1],
                [255, 55, 0, 1],
                [255, 27, 0, 1],
                [255, 0, 0, 1],
                [228, 0, 10, 1],
                [201, 0, 18, 1],
                [174, 0, 23, 1],
                [147, 0, 26, 1]
            ]
        },
        pm10: {
            type: 'breakpoint',
            unit: 'μg/m³',
            breakpoints: [27.5, 105, 205, 305, 390, 515, 1052.5],
            colors: [
                [0, 228, 0, 1],
                [255, 255, 0, 1],
                [255, 126, 0, 1],
                [255, 0, 0, 1],
                [185, 77, 196, 1],
                [179, 2, 51, 1],
                [79, 0, 23, 1]   
            ]
        },
        pm2_5: {
            type: 'breakpoint',
            unit: 'μg/m³',
            breakpoints: [4.5, 22.25, 45.5, 90.5, 175.5, 362.75, 900],
            colors: [
                [0, 228, 0, 1],
                [255, 255, 0, 1],
                [255, 126, 0, 1],
                [255, 0, 0, 1],
                [185, 77, 196, 1],
                [179, 2, 51, 1],
                [79, 0, 23, 1]
            ]
        },
        carbon_monoxide: {
            type: 'breakpoint',
            unit: 'μg/m³',
            breakpoints: [2807.24, 8733.64, 13724.3, 17467.3, 28696.3, 50530.3],
            colors: [
                [0, 228, 0, 1],
                [255, 255, 0, 1],
                [255, 126, 0, 1],
                [255, 0, 0, 1],
                [185, 77, 196, 1],
                [179, 2, 51, 1]                
            ]
        },
        nitrogen_dioxide: {
            type: 'breakpoint',
            unit: 'μg/m³',
            breakpoints: [55.335, 157.807, 471.372, 1034.97, 1946.97, 3381.58],
            colors: [
                [0, 228, 0, 1],
                [255, 255, 0, 1],
                [255, 126, 0, 1],
                [255, 0, 0, 1],
                [185, 77, 196, 1],
                [179, 2, 51, 1]                
            ]
        },
        sulphur_dioxide: {
            type: 'breakpoint',
            unit: 'μg/m³',
            breakpoints: [49.9432, 156.964, 371.007, 699.205, 1298.52, 2297.39],
            colors: [
                [0, 228, 0, 1],
                [255, 255, 0, 1],
                [255, 126, 0, 1],
                [255, 0, 0, 1],
                [185, 77, 196, 1],
                [179, 2, 51, 1]                
            ]
        },
        ozone: {
            type: 'breakpoint',
            unit: 'μg/m³',
            breakpoints: [58.7973, 192.428, 310.022, 395.546, 652.116, 1079.73],
            colors: [
                [0, 228, 0, 1],
                [255, 255, 0, 1],
                [255, 126, 0, 1],
                [255, 0, 0, 1],
                [185, 77, 196, 1],
                [179, 2, 51, 1]                
            ]
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
        center: [ 44.384, 33.308 ],
        zoom: 3,
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
//            console.log('Promise variable: ', variable, model);
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
