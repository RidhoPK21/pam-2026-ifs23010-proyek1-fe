package org.delcom.pam_proyek1_ifs23010.network.events.data

import kotlinx.serialization.Serializable

@Serializable
data class RequestUserChange (
    val name: String,
    val username: String,
    val about: String? = null
)

@Serializable
data class RequestUserChangePassword (
    val password: String,
    val newPassword: String
)