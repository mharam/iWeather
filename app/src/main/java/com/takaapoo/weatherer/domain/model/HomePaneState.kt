package com.takaapoo.weatherer.domain.model

enum class HomePaneContent{
    LOCATIONLIST, WEATHERDETAIL, AIRQUALITY
}

data class HomePaneState(
    val listItemCount: Int = 0,
    val initialPage: Int = -1,
    val pageNumberReturningFromDetail: Int? = null,
    val paneContent: HomePaneContent = HomePaneContent.LOCATIONLIST
)
