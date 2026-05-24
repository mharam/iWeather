package com.takaapoo.weatherer.data.remote

import kotlinx.serialization.Serializable


@Serializable
data class LandScape(
    val id: Int = 1,
    val url: String = ""
)
