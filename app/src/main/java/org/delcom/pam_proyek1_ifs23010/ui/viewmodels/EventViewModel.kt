package org.delcom.pam_proyek1_ifs23010.ui.viewmodels

import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

import org.delcom.pam_proyek1_ifs23010.network.events.data.RequestEvent
import org.delcom.pam_proyek1_ifs23010.network.events.data.RequestUserChange
import org.delcom.pam_proyek1_ifs23010.network.events.data.RequestUserChangePassword
import org.delcom.pam_proyek1_ifs23010.network.events.data.ResponseEventData
import org.delcom.pam_proyek1_ifs23010.network.events.data.ResponseEventStatsData
import org.delcom.pam_proyek1_ifs23010.network.events.data.ResponseUserData
import org.delcom.pam_proyek1_ifs23010.network.events.service.IEventRepository
import javax.inject.Inject

sealed interface ProfileUIState {
    data class Success(val data: ResponseUserData) : ProfileUIState
    data class Error(val message: String) : ProfileUIState
    object Loading : ProfileUIState
}

sealed interface EventsUIState {
    data class Success(val data: List<ResponseEventData>) : EventsUIState
    data class Error(val message: String) : EventsUIState
    object Loading : EventsUIState
}

sealed interface EventUIState {
    data class Success(val data: ResponseEventData) : EventUIState
    data class Error(val message: String) : EventUIState
    object Loading : EventUIState
}

sealed interface EventActionUIState {
    data class Success(val message: String) : EventActionUIState
    data class Error(val message: String) : EventActionUIState
    object Loading : EventActionUIState
}

sealed interface StatsUIState {
    data class Success(val data: ResponseEventStatsData) : StatsUIState
    data class Error(val message: String) : StatsUIState
    object Loading : StatsUIState
}

data class UIStateEvent(
    val profile: ProfileUIState = ProfileUIState.Loading,
    val stats: StatsUIState = StatsUIState.Loading,
    val events: EventsUIState = EventsUIState.Loading,
    var event: EventUIState = EventUIState.Loading,
    var eventAdd: EventActionUIState = EventActionUIState.Loading,
    var eventChange: EventActionUIState = EventActionUIState.Loading,
    var eventDelete: EventActionUIState = EventActionUIState.Loading,
    var eventChangeCover: EventActionUIState = EventActionUIState.Loading,
    var profileChange: EventActionUIState = EventActionUIState.Loading,
    var profileChangePassword: EventActionUIState = EventActionUIState.Loading,
    var profileChangePhoto: EventActionUIState = EventActionUIState.Loading
)

@HiltViewModel
@Keep
class EventViewModel @Inject constructor(
    private val repository: IEventRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(UIStateEvent())
    val uiState = _uiState.asStateFlow()

    private var currentPage = 1
    private var isLastPage = false
    private var currentStatus: String? = null
    private var currentDivisi: String? = null
    private val currentEventsList = mutableListOf<ResponseEventData>()
    private var isFetching = false

    fun getProfile(authToken: String) {
        // PERBAIKAN: Gunakan Dispatchers.IO untuk pemanggilan asinkron yang aman
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(profile = ProfileUIState.Loading) }
            val tmpState = runCatching {
                repository.getUserMe(authToken)
            }.fold(
                onSuccess = { response ->
                    if (response.status == "success") {
                        ProfileUIState.Success(response.data!!.user)
                    } else {
                        ProfileUIState.Error(response.message)
                    }
                },
                onFailure = { error ->
                    ProfileUIState.Error(error.message ?: "Unknown error")
                }
            )
            _uiState.update { it.copy(profile = tmpState) }
        }
    }

    fun resetAndGetAllEvents(
        authToken: String,
        search: String? = null,
        status: String? = null,
        divisi: String? = null
    ) {
        isFetching = false
        currentPage = 1
        isLastPage = false
        currentStatus = status
        currentDivisi = divisi
        currentEventsList.clear()
        getAllEvents(authToken, search, currentStatus, currentDivisi)
    }

    fun getAllEvents(
        authToken: String,
        search: String? = null,
        status: String? = currentStatus,
        divisi: String? = currentDivisi
    ) {
        if (isLastPage || isFetching) return

        isFetching = true

        if (currentPage == 1) {
            _uiState.update { it.copy(events = EventsUIState.Loading) }
        }

        // PERBAIKAN: Gunakan Dispatchers.IO untuk mencegah ANR saat Ktor lag
        viewModelScope.launch(Dispatchers.IO) {
            val tmpState = runCatching {
                repository.getEvents(authToken, search, currentPage, 10, status, divisi)
            }.fold(
                onSuccess = { response ->
                    isFetching = false

                    if (response.status == "success") {
                        val newEvents = response.data?.events ?: emptyList()
                        if (newEvents.size < 10) isLastPage = true

                        // Proses anti-duplikat ini juga bisa berat jika datanya banyak,
                        // karenanya sangat tepat dilakukan di Dispatchers.IO
                        val uniqueEvents = newEvents.filter { newEvent ->
                            currentEventsList.none { existingEvent -> existingEvent.id == newEvent.id }
                        }

                        currentEventsList.addAll(uniqueEvents)
                        currentPage++
                        EventsUIState.Success(currentEventsList.toList())
                    } else {
                        EventsUIState.Error(response.message)
                    }
                },
                onFailure = { error ->
                    isFetching = false
                    EventsUIState.Error(error.message ?: "Unknown error")
                }
            )

            _uiState.update { state -> state.copy(events = tmpState) }
        }
    }

    fun getEventStats(authToken: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(stats = StatsUIState.Loading) }
            val tmpState = runCatching {
                repository.getEventStats(authToken)
            }.fold(
                onSuccess = { response ->
                    if (response.status == "success") {
                        StatsUIState.Success(response.data!!.stats)
                    } else {
                        StatsUIState.Error(response.message)
                    }
                },
                onFailure = { error ->
                    StatsUIState.Error(error.message ?: "Unknown error")
                }
            )
            _uiState.update { it.copy(stats = tmpState) }
        }
    }

    fun postEvent(
        authToken: String,
        title: String,
        description: String,
        status: String,
        tanggalPelaksanaan: String,
        tempatPelaksanaan: String,
        estimasiBiaya: String,
        divisi: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(eventAdd = EventActionUIState.Loading) }
            val tmpState = runCatching {
                repository.postEvent(
                    authToken = authToken,
                    RequestEvent(
                        title = title,
                        description = description,
                        status = status,
                        tanggalPelaksanaan = tanggalPelaksanaan,
                        tempatPelaksanaan = tempatPelaksanaan,
                        estimasiBiaya = estimasiBiaya,
                        divisi = divisi
                    )
                )
            }.fold(
                onSuccess = { response ->
                    if (response.status == "success") EventActionUIState.Success(response.message)
                    else EventActionUIState.Error(response.message)
                },
                onFailure = { error -> EventActionUIState.Error(error.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(eventAdd = tmpState) }
        }
    }

    fun getEventById(authToken: String, eventId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(event = EventUIState.Loading) }
            val tmpState = runCatching {
                repository.getEventById(authToken, eventId)
            }.fold(
                onSuccess = { response ->
                    if (response.status == "success") EventUIState.Success(response.data!!.event)
                    else EventUIState.Error(response.message)
                },
                onFailure = { error -> EventUIState.Error(error.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(event = tmpState) }
        }
    }

    fun putEvent(
        authToken: String,
        eventId: String,
        title: String,
        description: String,
        status: String,
        tanggalPelaksanaan: String,
        tempatPelaksanaan: String,
        estimasiBiaya: String,
        divisi: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(eventChange = EventActionUIState.Loading) }
            val tmpState = runCatching {
                repository.putEvent(
                    authToken = authToken,
                    eventId = eventId,
                    RequestEvent(
                        title = title,
                        description = description,
                        status = status,
                        tanggalPelaksanaan = tanggalPelaksanaan,
                        tempatPelaksanaan = tempatPelaksanaan,
                        estimasiBiaya = estimasiBiaya,
                        divisi = divisi
                    )
                )
            }.fold(
                onSuccess = { response ->
                    if (response.status == "success") EventActionUIState.Success(response.message)
                    else EventActionUIState.Error(response.message)
                },
                onFailure = { error -> EventActionUIState.Error(error.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(eventChange = tmpState) }
        }
    }

    fun putEventCover(authToken: String, eventId: String, file: MultipartBody.Part) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(eventChangeCover = EventActionUIState.Loading) }
            val tmpState = runCatching {
                repository.putEventCover(authToken = authToken, eventId = eventId, file = file)
            }.fold(
                onSuccess = { response ->
                    if (response.status == "success") EventActionUIState.Success(response.message)
                    else EventActionUIState.Error(response.message)
                },
                onFailure = { error -> EventActionUIState.Error(error.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(eventChangeCover = tmpState) }
        }
    }

    fun deleteEvent(authToken: String, eventId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(eventDelete = EventActionUIState.Loading) }
            val tmpState = runCatching {
                repository.deleteEvent(authToken = authToken, eventId = eventId)
            }.fold(
                onSuccess = { response ->
                    if (response.status == "success") EventActionUIState.Success(response.message)
                    else EventActionUIState.Error(response.message)
                },
                onFailure = { error -> EventActionUIState.Error(error.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(eventDelete = tmpState) }
        }
    }

    fun putUserMe(authToken: String, name: String, username: String, about: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(profileChange = EventActionUIState.Loading) }
            val tmpState = runCatching {
                repository.putUserMe(authToken, RequestUserChange(name, username, about))
            }.fold(
                onSuccess = { response ->
                    if (response.status == "success") EventActionUIState.Success(response.message)
                    else EventActionUIState.Error(response.message)
                },
                onFailure = { error -> EventActionUIState.Error(error.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(profileChange = tmpState) }
        }
    }

    fun putUserMePassword(authToken: String, oldPassword: String, newPassword: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(profileChangePassword = EventActionUIState.Loading) }
            val tmpState = runCatching {
                repository.putUserMePassword(authToken, RequestUserChangePassword(oldPassword, newPassword))
            }.fold(
                onSuccess = { response ->
                    if (response.status == "success") EventActionUIState.Success(response.message)
                    else EventActionUIState.Error(response.message)
                },
                onFailure = { error -> EventActionUIState.Error(error.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(profileChangePassword = tmpState) }
        }
    }

    fun putUserMePhoto(authToken: String, file: MultipartBody.Part) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(profileChangePhoto = EventActionUIState.Loading) }
            val tmpState = runCatching {
                repository.putUserMePhoto(authToken, file)
            }.fold(
                onSuccess = { response ->
                    if (response.status == "success") EventActionUIState.Success(response.message)
                    else EventActionUIState.Error(response.message)
                },
                onFailure = { error -> EventActionUIState.Error(error.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(profileChangePhoto = tmpState) }
        }
    }
}