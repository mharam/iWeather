package com.takaapoo.weatherer.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable


open class Screen

@Serializable
data object Start: NavKey

@Serializable
data object Home: NavKey

@Serializable
data object HomePane: NavKey

@Serializable
data object AddLocation: NavKey

@Serializable
data object Detail: NavKey

@Serializable
data class AQDetail(
    val pageNumber: Int
): NavKey

@Serializable
data object Radar: NavKey

@Serializable
data object ContactUs: NavKey

@Serializable
data object AboutUs: NavKey

@Serializable
data object Support: NavKey

@Serializable
data object Settings: NavKey

@Serializable
data object SettingsGeneral: NavKey

@Serializable
data object SettingsUnit: NavKey

@Serializable
data object SettingsHourlyDiagram: NavKey

@Serializable
data object SettingsNotification: NavKey

@Serializable
data object SettingsCard: NavKey

@Serializable
data object Donate: NavKey


@Serializable
data object Login: NavKey
