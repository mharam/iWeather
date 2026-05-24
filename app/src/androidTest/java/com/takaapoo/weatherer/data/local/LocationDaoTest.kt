package com.takaapoo.weatherer.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class LocationDaoTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var locationDao: LocationDao
    private lateinit var db: WeatherDatabase

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        Dispatchers.setMain(dispatcher)
        db = Room.inMemoryDatabaseBuilder(context, WeatherDatabase::class.java).build()
        locationDao = db.locationDao()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    @Throws(IOException::class)
    fun tearDown() {
        db.close()
        Dispatchers.resetMain()
    }

    @Test
    fun add_Location_and_get_its_id_by_name() = runTest {
        val myLocation = Location(
            id = 5,
            customId = 5,
            name = "Tehran"
        )
        locationDao.addLocation(myLocation)
        val id = locationDao.getLocationIdWithName("Tehran")
        assertThat(id).isEqualTo(5)
    }
    @Test
    fun add_Location_and_get_locations_flow() = runTest {
        val myLocation = Location(
            id = 5,
            customId = 5,
            name = "Tehran"
        )
        locationDao.addLocation(myLocation)
        assertThat(locationDao.getAllLocations().first()).isEqualTo(listOf(myLocation))
        assertThat(locationDao.getLocationFlow(5).first()).isEqualTo(myLocation)
        assertThat(locationDao.getLocationFlow(1).first()).isNull()
        locationDao.deleteLocation(5)
        assertThat(locationDao.getLocation(5)).isNull()
    }
    @Test
    fun add_Location_and_count_locations_with_name() = runTest {
        val myLocation = Location(
            id = 5,
            customId = 5,
            name = "Tehran"
        )
        locationDao.addLocation(myLocation)
        assertThat(locationDao.countLocationWithName("Tehran")).isEqualTo(1)
        locationDao.deleteLocation(5)
        assertThat(locationDao.countLocationWithName("Tehran")).isEqualTo(0)
    }

}