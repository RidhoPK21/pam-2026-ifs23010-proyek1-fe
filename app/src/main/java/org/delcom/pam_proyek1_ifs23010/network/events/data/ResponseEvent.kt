package org.delcom.pam_proyek1_ifs23010.network.events.data

import kotlinx.serialization.Serializable

@Serializable
data class ResponseEvents (
    val events: List<ResponseEventData> // Diubah dari todos menjadi events
)

@Serializable
data class ResponseEvent (
    val event: ResponseEventData // Diubah dari todo menjadi event
)

@Serializable
data class ResponseEventData(
    val id: String = "",
    val userId: String = "",
    val title: String,
    val description: String,

    // Field baru pengganti isDone dan urgency
    val status: String = "belum terlaksana",
    val tanggalPelaksanaan: String = "",
    val tempatPelaksanaan: String = "",
    val estimasiBiaya: String = "",
    val divisi: String = "",

    val cover: String? = null,
    val createdAt: String = "",
    var updatedAt: String = ""
)

@Serializable
data class ResponseEventAdd (
    val eventId: String // Diubah dari todoId menjadi eventId
)

@Serializable
data class ResponseEventStats (
    val stats: ResponseEventStatsData
)

@Serializable
data class ResponseEventStatsData(
    val total: Long = 0,
    val complete: Long = 0,
    val active: Long = 0,
    val canceled: Long = 0 // Tambahan untuk statistik kegiatan yang dibatalkan
)