package com.takaapoo.weatherer.domain.repository

import com.takaapoo.weatherer.data.remote.PurchaseStoreResponse
import com.takaapoo.weatherer.domain.MyResult

interface BillingRepository {
    suspend fun storePurchase(
        userId: String? = null,
        productIds: List<String>,
        purchaseToken: String
    ): MyResult<out PurchaseStoreResponse>
}