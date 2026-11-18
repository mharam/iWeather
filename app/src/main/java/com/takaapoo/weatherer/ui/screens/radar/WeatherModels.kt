package com.takaapoo.weatherer.ui.screens.radar

enum class WeatherModels(
    val title: String,
    val serverName: String,
    val metadata: String = serverName,
    val global: Boolean
) {
    CAMS_GLOBAL(
        title = "CAMS atmospheric forecasts",
        serverName = "cams_global",
        global = true,
    ),

    CHMI_EUROPE(
        title = "CHMI (Europe)",
        serverName = "chmi_seamless",
        metadata = "chmi_aladin_central_europe_2km",
        global = false,
    ),

//    CHMI_CZECH(
//        title = "CHMI (Czech Republic)",
//        serverName = "chmi_aladin_cz_1km",
//        global = false,
//    ),

    DMI_DENMARK(
        title = "DMI (Europe)",
        serverName = "dmi_harmonie_arome_europe",
        global = false,
    ),

    DWD_GLOBAL(
        title = "DWD ICON",
        serverName = "dwd_icon_seamless",
        metadata = "dwd_icon",
        global = true,
    ),

    DWD_GLOBAL_ENSEMBLE(
        title = "DWD ICON Ensemble",
        serverName = "dwd_icon_eps",
        global = true,
    ),

//    DWD_EUROPE(
//        title = "DWD ICON (Europe)",
//        serverName = "dwd_icon_eu",
//        global = false,
//    ),

    DWD_EUROPE_ENSEMBLE(
        title = "DWD ICON Ensemble (Europe)",
        serverName = "dwd_icon_eu_eps",
        global = false,
    ),

//    DWD_D2(
//        title = "DWD ICON (Germany)",
//        serverName = "dwd_icon_d2",
//        global = false,
//    ),

    DWD_D2_ENSEMBLE(
        title = "DWD ICON Ensemble (Germany)",
        serverName = "dwd_icon_d2_eps",
        global = false,
    ),

    NOAA_GFS_025(
        title = "NOAA GFS (0.25°)",
        serverName = "ncep_gfs_seamless",
        metadata = "ncep_gfs025",
        global = true,
    ),

    NOAA_GFS_013(
        title = "NOAA GFS (0.13°)",
        serverName = "ncep_gfs013",
        global = true,
    ),

    NOAA_GFS_025_ENSEMBLE(
        title = "NOAA GFS (0.25°)",
        serverName = "ncep_gefs025",
        global = true,
    ),

    ECMWF_IFS_025(
        title = "ECMWF IFS (0.25°)",
        serverName = "ecmwf_ifs025",
        global = true,
    ),

    ECMWF_IFS_025_ENSEMBLE(
        title = "ECMWF IFS (0.25°)",
        serverName = "ecmwf_ifs025_ensemble",
        global = true,
    ),

    ECMWF_IFS(
        title = "ECMWF IFS",
        serverName = "ecmwf_ifs",
        global = true,
    ),

    GEM_GLOBAL(
        title = "GEM",
        serverName = "cmc_gem_seamless",
        metadata = "cmc_gem_gdps_15km",
        global = true,
    ),

    GEM_GLOBAL_ENSEMBLE(
        title = "GEM",
        serverName = "cmc_gem_geps",
        global = true,
    ),

    GEOSPHERE_AUSTRIA(
        title = "AROME GeoSphere (Austria)",
        serverName = "geosphere_arome_austria",
        global = false,
    ),

    ITALIA_METEO_ARPAE(
        title = "ARPAE ItaliaMeteo (Italy)",
        serverName = "italia_meteo_arpae_icon_2i",
        global = false,
    ),

    JMA_MSM(
        title = "JMA",
        serverName = "jma_seamless",
        metadata = "jma_gsm",
        global = false,
    ),

    METEOFRANCE_ARPEGE_WORLD(
        title = "ARPEGE Météo-France",
        serverName = "meteofrance_seamless",
        metadata = "meteofrance_arpege_world025",
        global = true,
    ),

//    METEOFRANCE_ARPEGE_EUROPE(
//        title = "ARPEGE Météo-France (Europe)",
//        serverName = "meteofrance_arpege_europe",
//        global = false,
//    ),

//    METEOFRANCE_AROME_FRANCE(
//        title = "ARPEGE Météo-France (France)",
//        serverName = "meteofrance_arome_france_hd",
//        global = false,
//    ),

    METNO_NORDIC(
        title = "MET Nordic Norway",
        serverName = "metno_nordic_pp",
        global = false,
    ),

    KNMI_HARMONIE_NETHERLANDS(
        title = "KNMI HARMONIE-AROME (Europe)",
        serverName = "knmi_seamless",
        metadata = "knmi_harmonie_arome_europe",
        global = false,
    ),

    UKMO_GLOBAL(
        title = "UK Met Office",
        serverName = "ukmo_seamless",
        metadata = "ukmo_global_deterministic_10km",
        global = true,
    ),

}