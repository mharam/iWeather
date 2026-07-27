package com.takaapoo.weatherer.domain.model

enum class DonatePaneContent{
    MOTIVATION, PAY
}

data class DonateState(
    val paneContent: DonatePaneContent = DonatePaneContent.MOTIVATION,
)