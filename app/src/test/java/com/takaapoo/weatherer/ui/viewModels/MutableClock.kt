package com.takaapoo.weatherer.ui.viewModels

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

class MutableClock(
    private var currentInstant: Instant,
    private val zone: ZoneId
) : Clock() {

    override fun getZone(): ZoneId = zone

    override fun withZone(zone: ZoneId): Clock =
        MutableClock(currentInstant, zone)

    override fun instant(): Instant = currentInstant

    fun advanceBy(duration: Duration) {
        currentInstant = currentInstant.plus(duration)
    }
}