package com.takaapoo.weatherer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addLocation(location: Location)

    @Query("SELECT id From locations WHERE name = :locationName")
    suspend fun getLocationIdWithName(locationName: String): Int

    @Query("SELECT * FROM locations ORDER BY custom_id")
    fun getAllLocations(): Flow<List<Location>>

    @Query("SELECT * FROM locations WHERE id = :locationId")
    fun getLocationFlow(locationId: Int): Flow<Location?>

    @Query("SELECT * FROM locations WHERE id = :locationId")
    suspend fun getLocation(locationId: Int): Location?

    @Query("SELECT * FROM locations ORDER BY custom_id")
    suspend fun getAllLocationsList(): List<Location>

    @Query("DELETE FROM locations WHERE name = :locationName")
    suspend fun deleteLocation(locationName: String)

    @Query("DELETE From locations WHERE id = :locationId")
    suspend fun deleteLocation(locationId: Int)

    @Query("SELECT COUNT(name) FROM locations WHERE name = :locationName")
    suspend fun countLocationWithName(locationName: String) : Int

    @Query("UPDATE locations SET modified_time = :lastModifiedTime WHERE name = :name")
    suspend fun updateLocationLastModifiedTime(name: String, lastModifiedTime: String?)

    @Query("UPDATE locations SET utc_offset = :utcOffset WHERE name = :name")
    suspend fun updateLocationUtcOffset(name: String, utcOffset:Long?)

    @Query("UPDATE locations SET location_type = :locationType WHERE name = :name")
    suspend fun updateLocationType(name: String, locationType: LocationType)

    @Query("UPDATE locations SET elevation = :elevation WHERE name = :name")
    suspend fun updateLocationElevation(name: String, elevation: Float)

    @Query("UPDATE locations SET yearlyPrecipitation = :precipitation WHERE name = :name")
    suspend fun updateLocationPrecipitation(name: String, precipitation: Float?)

    @Query("UPDATE locations SET currentPictureUrl = :currentPicUrl, picIdsList = :picIds WHERE name = :name")
    suspend fun updateLocationPicIds(name: String, currentPicUrl: String, picIds: List<Int>)

    @Update
    suspend fun updateLocation(vararg location: Location)

    @Query("UPDATE locations SET name = :newLocationName WHERE id = :locationId")
    suspend fun updateLocationName(locationId: Int, newLocationName: String)

    @Query("UPDATE locations SET custom_id = :customId WHERE id = :id")
    suspend fun updateLocationCustomId(id: Int, customId: Int)

    @Query("SELECT custom_id FROM locations WHERE id = :id")
    suspend fun getLocationCustomId(id: Int): Int

    @Query("SELECT COUNT(*) FROM locations")
    fun locationCount(): Flow<Int>

    @Query("DELETE From locations WHERE ABS(elevation - (-1000)) < 0.001" +
            " Or yearlyPrecipitation IS NULL Or currentPictureUrl = ''")
    suspend fun deleteFaultyLocations()

}