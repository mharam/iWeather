package com.takaapoo.weatherer.data

import kotlinx.serialization.Serializable

/*enum class Screens {
    HOME, ADDLOCATION, DETAIL, RADAR, HISTORY, SETTING, AQDETAIL
}*/

open class Screen

@Serializable
object Home: Screen()

@Serializable
object HomePane: Screen()

@Serializable
object AddLocation: Screen()

@Serializable
data class Detail(
    val roomWidth: Float,
    val locationsCount: Int,
    val initialPageNumber: Int
): Screen()

@Serializable
data class AQDetail(
    val pageNumber: Int
): Screen()

@Serializable
object Radar: Screen()

@Serializable
object History: Screen()

@Serializable
object Settings: Screen()
