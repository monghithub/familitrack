package com.monghit.familytrack.data.remote

import com.monghit.familytrack.data.remote.dto.ConfigUpdateRequest
import com.monghit.familytrack.data.remote.dto.ConfigUpdateResponse
import com.monghit.familytrack.data.remote.dto.FamilyLocationsResponse
import com.monghit.familytrack.data.remote.dto.LocationUpdateRequest
import com.monghit.familytrack.data.remote.dto.RegisterDeviceRequest
import com.monghit.familytrack.data.remote.dto.RegisterDeviceResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("api/register")
    suspend fun registerDevice(
        @Body request: RegisterDeviceRequest
    ): Response<RegisterDeviceResponse>

    @POST("api/location/update")
    suspend fun updateLocation(
        @Body request: LocationUpdateRequest
    ): Response<Unit>

    @POST("api/config/location-interval")
    suspend fun updateLocationInterval(
        @Query("deviceToken") deviceToken: String,
        @Query("intervalSeconds") intervalSeconds: Int
    ): Response<ConfigUpdateResponse>

    @GET("api/family/locations")
    suspend fun getFamilyLocations(): Response<FamilyLocationsResponse>
}
