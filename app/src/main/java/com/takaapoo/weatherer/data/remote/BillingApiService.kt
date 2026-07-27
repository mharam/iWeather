package com.takaapoo.weatherer.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface BillingApiService {
    @POST("Weather/Users.php")
    suspend fun storePurchase(
        @Body purchasePost: PurchasePost
    ): Response<PurchaseStoreResponse>
}