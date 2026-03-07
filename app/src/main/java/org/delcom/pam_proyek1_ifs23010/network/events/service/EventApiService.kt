package org.delcom.pam_proyek1_ifs23010.network.events.service

import okhttp3.MultipartBody
import org.delcom.pam_proyek1_ifs23010.network.data.ResponseMessage
// Pastikan semua import data mengarah ke folder 'events/data' yang baru
import org.delcom.pam_proyek1_ifs23010.network.events.data.RequestAuthLogin
import org.delcom.pam_proyek1_ifs23010.network.events.data.RequestAuthLogout
import org.delcom.pam_proyek1_ifs23010.network.events.data.RequestAuthRefreshToken
import org.delcom.pam_proyek1_ifs23010.network.events.data.RequestAuthRegister
import org.delcom.pam_proyek1_ifs23010.network.events.data.RequestEvent
import org.delcom.pam_proyek1_ifs23010.network.events.data.RequestUserChange
import org.delcom.pam_proyek1_ifs23010.network.events.data.RequestUserChangePassword
import org.delcom.pam_proyek1_ifs23010.network.events.data.ResponseAuthLogin
import org.delcom.pam_proyek1_ifs23010.network.events.data.ResponseAuthRegister
import org.delcom.pam_proyek1_ifs23010.network.events.data.ResponseEvent
import org.delcom.pam_proyek1_ifs23010.network.events.data.ResponseEventAdd
import org.delcom.pam_proyek1_ifs23010.network.events.data.ResponseEventStats
import org.delcom.pam_proyek1_ifs23010.network.events.data.ResponseEvents
import org.delcom.pam_proyek1_ifs23010.network.events.data.ResponseUser
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface EventApiService {
    // ----------------------------------
    // Auth
    // ----------------------------------

    // Register
    @POST("auth/register")
    suspend fun postRegister(
        @Body request: RequestAuthRegister
    ): ResponseMessage<ResponseAuthRegister?>

    // Login
    @POST("auth/login")
    suspend fun postLogin(
        @Body request: RequestAuthLogin
    ): ResponseMessage<ResponseAuthLogin?>

    // Logout
    @POST("auth/logout")
    suspend fun postLogout(
        @Body request: RequestAuthLogout
    ): ResponseMessage<String?>

    // RefreshToken
    @POST("auth/refresh-token")
    suspend fun postRefreshToken(
        @Body request: RequestAuthRefreshToken
    ): ResponseMessage<ResponseAuthLogin?>

    // ----------------------------------
    // Users
    // ----------------------------------

    // Ambil informasi profile
    @GET("users/me")
    suspend fun getUserMe(
        @Header("Authorization") authToken: String
    ): ResponseMessage<ResponseUser?>

    // Ubah data profile
    @PUT("users/me")
    suspend fun putUserMe(
        @Header("Authorization") authToken: String,
        @Body request: RequestUserChange,
    ): ResponseMessage<String?>

    // Ubah data kata sandi
    @PUT("users/me/password")
    suspend fun putUserMePassword(
        @Header("Authorization") authToken: String,
        @Body request: RequestUserChangePassword,
    ): ResponseMessage<String?>

    // Ubah photo profile
    @Multipart
    @PUT("users/me/photo")
    suspend fun putUserMePhoto(
        @Header("Authorization") authToken: String,
        @Part file: MultipartBody.Part
    ): ResponseMessage<String?>

    // ----------------------------------
    // Events (Kegiatan Himpunan)
    // ----------------------------------

    // Ambil semua data kegiatan
    @GET("events")
    suspend fun getEvents(
        @Header("Authorization") authToken: String,
        @Query("search") search: String? = null,
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 10,
        @Query("status") status: String? = null, // Filter baru
        @Query("divisi") divisi: String? = null  // Filter baru
    ): ResponseMessage<ResponseEvents?>

    // Ambil data statistik kegiatan
    @GET("events/stats")
    suspend fun getEventStats(
        @Header("Authorization") authToken: String
    ): ResponseMessage<ResponseEventStats?>

    // Menambahkan data kegiatan
    @POST("events")
    suspend fun postEvent(
        @Header("Authorization") authToken: String,
        @Body request: RequestEvent
    ): ResponseMessage<ResponseEventAdd?>

    // Ambil data kegiatan berdasarkan id
    @GET("events/{id}")
    suspend fun getEventById(
        @Header("Authorization") authToken: String,
        @Path("id") eventId: String // Path parameter disesuaikan
    ): ResponseMessage<ResponseEvent?>

    // Mengubah data kegiatan
    @PUT("events/{id}")
    suspend fun putEvent(
        @Header("Authorization") authToken: String,
        @Path("id") eventId: String,
        @Body request: RequestEvent
    ): ResponseMessage<String?>

    // Ubah cover kegiatan
    @Multipart
    @PUT("events/{id}/cover")
    suspend fun putEventCover(
        @Header("Authorization") authToken: String,
        @Path("id") eventId: String,
        @Part file: MultipartBody.Part
    ): ResponseMessage<String?>

    // Hapus data kegiatan
    @DELETE("events/{id}")
    suspend fun deleteEvent(
        @Header("Authorization") authToken: String,
        @Path("id") eventId: String
    ): ResponseMessage<String?>
}