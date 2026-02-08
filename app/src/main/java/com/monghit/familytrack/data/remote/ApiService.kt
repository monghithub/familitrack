package com.monghit.familytrack.data.remote

import com.monghit.familytrack.data.remote.dto.*
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

    @POST("api/user/update-profile")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<UpdateProfileResponse>

    @POST("api/user/profile")
    suspend fun getProfile(
        @Body request: GetProfileRequest
    ): Response<GetProfileResponse>

    @POST("api/quick-message")
    suspend fun sendQuickMessage(
        @Body request: QuickMessageRequest
    ): Response<Unit>

    @POST("api/emergency")
    suspend fun sendEmergency(
        @Body request: EmergencyRequest
    ): Response<EmergencyResponse>

    @GET("api/locations/history")
    suspend fun getLocationHistory(
        @Query("userId") userId: Int,
        @Query("date") date: String
    ): Response<LocationHistoryResponse>

    @POST("api/chat/send")
    suspend fun sendChatMessage(
        @Body request: ChatSendRequest
    ): Response<ChatSendResponse>

    @POST("api/chat/messages")
    suspend fun getChatMessages(
        @Body request: ChatMessagesRequest
    ): Response<ChatMessagesResponse>

    @POST("api/photos/send")
    suspend fun sendPhoto(
        @Body request: PhotoSendRequest
    ): Response<PhotoSendResponse>

    @POST("api/photos/list")
    suspend fun getPhotoList(
        @Body request: PhotoListRequest
    ): Response<PhotoListResponse>
}
