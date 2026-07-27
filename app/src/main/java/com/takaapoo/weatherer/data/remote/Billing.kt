package com.takaapoo.weatherer.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PurchasePost(
    @SerialName("user_id") val userId: String? = null,
    @SerialName("product_ids") val productIds: List<String>,
    @SerialName("purchase_token") val purchaseToken: String
)

@Serializable
data class PurchaseStoreResponse(
    @SerialName("status") val status: String? = null,
    @SerialName("message") val message: String? = null,
)