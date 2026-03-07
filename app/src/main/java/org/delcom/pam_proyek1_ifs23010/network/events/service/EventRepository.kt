package org.delcom.pam_proyek1_ifs23010.network.events.service

import okhttp3.MultipartBody
import org.delcom.pam_proyek1_ifs23010.helper.SuspendHelper
import org.delcom.pam_proyek1_ifs23010.network.data.ResponseMessage
// Import disesuaikan ke package events/data
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
import org.delcom.pam_proyek1_ifs23010.network.events.data.ResponseEventStats
import org.delcom.pam_proyek1_ifs23010.network.events.data.ResponseEventAdd
import org.delcom.pam_proyek1_ifs23010.network.events.data.ResponseEvents
import org.delcom.pam_proyek1_ifs23010.network.events.data.ResponseUser

class EventRepository(
    private val apiService: EventApiService
) : IEventRepository {

    // ----------------------------------
    // Auth
    // ----------------------------------

    override suspend fun postRegister(
        request: RequestAuthRegister
    ): ResponseMessage<ResponseAuthRegister?> {
        return SuspendHelper.safeApiCall {
            apiService.postRegister(request)
        }
    }

    override suspend fun postLogin(
        request: RequestAuthLogin
    ): ResponseMessage<ResponseAuthLogin?> {
        return SuspendHelper.safeApiCall {
            apiService.postLogin(request)
        }
    }

    override suspend fun postLogout(
        request: RequestAuthLogout
    ): ResponseMessage<String?> {
        return SuspendHelper.safeApiCall {
            apiService.postLogout(request)
        }
    }

    override suspend fun postRefreshToken(
        request: RequestAuthRefreshToken
    ): ResponseMessage<ResponseAuthLogin?> {
        return SuspendHelper.safeApiCall {
            apiService.postRefreshToken(request)
        }
    }

    // ----------------------------------
    // Users
    // ----------------------------------

    override suspend fun getUserMe(
        authToken: String
    ): ResponseMessage<ResponseUser?> {
        return SuspendHelper.safeApiCall {
            apiService.getUserMe("Bearer $authToken")
        }
    }

    override suspend fun putUserMe(
        authToken: String,
        request: RequestUserChange
    ): ResponseMessage<String?> {
        return SuspendHelper.safeApiCall {
            apiService.putUserMe("Bearer $authToken", request)
        }
    }

    override suspend fun putUserMePassword(
        authToken: String,
        request: RequestUserChangePassword
    ): ResponseMessage<String?> {
        return SuspendHelper.safeApiCall {
            apiService.putUserMePassword("Bearer $authToken", request)
        }
    }

    override suspend fun putUserMePhoto(
        authToken: String,
        file: MultipartBody.Part
    ): ResponseMessage<String?> {
        return SuspendHelper.safeApiCall {
            apiService.putUserMePhoto("Bearer $authToken", file)
        }
    }

    // ----------------------------------
    // Events (Kegiatan Himpunan)
    // ----------------------------------

    override suspend fun getEvents(
        authToken: String,
        search: String?,
        page: Int,
        perPage: Int,
        status: String?,
        divisi: String?
    ): ResponseMessage<ResponseEvents?> {
        return SuspendHelper.safeApiCall {
            // Memanggil getEvents dari apiService dengan parameter baru
            apiService.getEvents("Bearer $authToken", search, page, perPage, status, divisi)
        }
    }

    override suspend fun getEventStats(
        authToken: String
    ): ResponseMessage<ResponseEventStats?> {
        return SuspendHelper.safeApiCall {
            apiService.getEventStats("Bearer $authToken")
        }
    }

    override suspend fun postEvent(
        authToken: String,
        request: RequestEvent
    ): ResponseMessage<ResponseEventAdd?> {
        return SuspendHelper.safeApiCall {
            apiService.postEvent("Bearer $authToken", request)
        }
    }

    override suspend fun getEventById(
        authToken: String,
        eventId: String
    ): ResponseMessage<ResponseEvent?> {
        return SuspendHelper.safeApiCall {
            apiService.getEventById("Bearer $authToken", eventId)
        }
    }

    override suspend fun putEvent(
        authToken: String,
        eventId: String,
        request: RequestEvent
    ): ResponseMessage<String?> {
        return SuspendHelper.safeApiCall {
            apiService.putEvent("Bearer $authToken", eventId, request)
        }
    }

    override suspend fun putEventCover(
        authToken: String,
        eventId: String,
        file: MultipartBody.Part
    ): ResponseMessage<String?> {
        return SuspendHelper.safeApiCall {
            apiService.putEventCover("Bearer $authToken", eventId, file)
        }
    }

    override suspend fun deleteEvent(
        authToken: String,
        eventId: String
    ): ResponseMessage<String?> {
        return SuspendHelper.safeApiCall {
            apiService.deleteEvent("Bearer $authToken", eventId)
        }
    }
}