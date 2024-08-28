package com.takaapoo.weatherer.domain.repository

interface DataRefreshRepository {
    fun refreshData()
}