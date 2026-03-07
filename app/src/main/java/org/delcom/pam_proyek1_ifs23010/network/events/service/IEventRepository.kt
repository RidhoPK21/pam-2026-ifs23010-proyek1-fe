package org.delcom.pam_proyek1_ifs23010.network.events.service

import okhttp3.MultipartBody
import org.delcom.pam_proyek1_ifs23010.network.data.ResponseMessage
// Pastikan semua import mengarah ke package events.data
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

interface IEventRepository {

    // ----------------------------------
    // Auth
    // ----------------------------------

    suspend fun postRegister(
        request: RequestAuthRegister
    ): ResponseMessage<ResponseAuthRegister?>

    suspend fun postLogin(
        request: RequestAuthLogin
    ): ResponseMessage<ResponseAuthLogin?>

    suspend fun postLogout(
        request: RequestAuthLogout
    ): ResponseMessage<String?>

    suspend fun postRefreshToken(
        request: RequestAuthRefreshToken
    ): ResponseMessage<ResponseAuthLogin?>

    // ----------------------------------
    // Users
    // ----------------------------------

    suspend fun getUserMe(
        authToken: String
    ): ResponseMessage<ResponseUser?>

    suspend fun putUserMe(
        authToken: String,
        request: RequestUserChange
    ): ResponseMessage<String?>

    suspend fun putUserMePassword(
        authToken: String,
        request: RequestUserChangePassword
    ): ResponseMessage<String?>

    suspend fun putUserMePhoto(
        authToken: String,
        file: MultipartBody.Part
    ): ResponseMessage<String?>

    // ----------------------------------
    // Events (Kegiatan Himpunan)
    // ----------------------------------

    // Ubah nama fungsi dan parameter
    suspend fun getEvents(
        authToken: String,
        search: String? = null,
        page: Int = 1,
        perPage: Int = 10,
        status: String? = null, // Filter status kegiatan
        divisi: String? = null  // Filter divisi kegiatan
    ): ResponseMessage<ResponseEvents?>

    suspend fun getEventStats(
        authToken: String
    ): ResponseMessage<ResponseEventStats?>

    suspend fun postEvent(
        authToken: String,
        request: RequestEvent
    ): ResponseMessage<ResponseEventAdd?>

    suspend fun getEventById(
        authToken: String,
        eventId: String
    ): ResponseMessage<ResponseEvent?>

    suspend fun putEvent(
        authToken: String,
        eventId: String,
        request: RequestEvent
    ): ResponseMessage<String?>

    suspend fun putEventCover(
        authToken: String,
        eventId: String,
        file: MultipartBody.Part
    ): ResponseMessage<String?>

    suspend fun deleteEvent(
        authToken: String,
        eventId: String
    ): ResponseMessage<String?>
}