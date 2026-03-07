package org.delcom.pam_proyek1_ifs23010.network.events.data

import kotlinx.serialization.Serializable

@Serializable
data class RequestEvent (
    val title: String,
    val description: String,

    // Properti baru disesuaikan dengan Backend
    val status: String = "belum terlaksana",
    val tanggalPelaksanaan: String,
    val tempatPelaksanaan: String,
    val estimasiBiaya: String,
    val divisi: String
)