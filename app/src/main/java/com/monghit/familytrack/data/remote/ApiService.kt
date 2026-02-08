package com.monghit.familytrack.data.remote

import com.monghit.familytrack.data.remote.dto.ConfigUpdateRequest
import com.monghit.familytrack.data.remote.dto.ConfigUpdateResponse
import com.monghit.familytrack.data.remote.dto.CreateFamilyRequest
import com.monghit.familytrack.data.remote.dto.CreateFamilyResponse
import com.monghit.familytrack.data.remote.dto.CreateSafeZoneRequest
import com.monghit.familytrack.data.remote.dto.CreateSafeZoneResponse
import com.monghit.familytrack.data.remote.dto.DeleteSafeZoneRequest
import com.monghit.familytrack.data.remote.dto.DeleteSafeZoneResponse
import com.monghit.familytrack.data.remote.dto.FamilyLocationsResponse
import com.monghit.familytrack.data.remote.dto.JoinFamilyRequest
import com.monghit.familytrack.data.remote.dto.JoinFamilyResponse
import com.monghit.familytrack.data.remote.dto.LocationUpdateRequest
import com.monghit.familytrack.data.remote.dto.ManualNotifyRequest
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

    @POST("api/notify")
    suspend fun sendManualNotification(
        @Body request: ManualNotifyRequest
    ): Response<Unit>

    @POST("api/safe-zones/create")
    suspend fun createSafeZone(
        @Body request: CreateSafeZoneRequest
    ): Response<CreateSafeZoneResponse>

    @POST("api/safe-zones/delete")
    suspend fun deleteSafeZone(
        @Body request: DeleteSafeZoneRequest
    ): Response<DeleteSafeZoneResponse>

    @POST("api/family/create")
    suspend fun createFamily(
        @Body request: CreateFamilyRequest
    ): Response<CreateFamilyResponse>

    @POST("api/family/join")
    suspend fun joinFamily(
        @Body request: JoinFamilyRequest
    ): Response<JoinFamilyResponse>
}
