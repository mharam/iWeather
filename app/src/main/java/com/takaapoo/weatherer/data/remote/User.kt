package com.takaapoo.weatherer.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Request model
@Serializable
data class UserAuthRequest(
    @SerialName("id_token") val idToken: String
)

// Response model
@Serializable
data class UserAuthResponse(
    @SerialName("status") val status: String,
    @SerialName("user_id") val userId: String,
    @SerialName("email") val email: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("picture") val picture: String? = null,
    @SerialName("message") val message: String? = null
)

@Serializable
data class UserTrialCheckResponse(
    @SerialName("status") val status: String? = null,
    @SerialName("message") val message: String? = null,
    val exist: Boolean = false,
    @SerialName("trial_expired") val trialExpired: Boolean = true
)
