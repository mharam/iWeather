package com.takaapoo.weatherer.data.repository

import com.takaapoo.weatherer.data.remote.BillingApiService
import com.takaapoo.weatherer.data.remote.PurchasePost
import com.takaapoo.weatherer.data.remote.PurchaseStoreResponse
import com.takaapoo.weatherer.di_modules.Http
import com.takaapoo.weatherer.di_modules.Https
import com.takaapoo.weatherer.di_modules.callWithFallback
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.domain.repository.BillingRepository
import org.json.JSONObject
import javax.inject.Inject

class BillingRepositoryImpl @Inject constructor(
    @param:Http private val billingApiServiceHttp: BillingApiService,
    @param:Https private val billingApiServiceHttps: BillingApiService
): BillingRepository {
    override suspend fun storePurchase(
        userId: String?,
        productIds: List<String>,
        purchaseToken: String,
    ): MyResult<out PurchaseStoreResponse> {
        val post = PurchasePost(userId, productIds, purchaseToken)
        val response = try {
            callWithFallback(
                httpsCall = {
                    billingApiServiceHttps.storePurchase(post)
                },
                httpCall = {
                    billingApiServiceHttp.storePurchase(post)
                }
            )
        } catch (e: Exception){
            return MyResult.Error(exception = e)
        }
        val errorBody = response.errorBody()?.string()
        return if (response.isSuccessful && response.body() != null) {
            if (response.body()?.status == "success")
                MyResult.Success(response.body()!!)
            else
                MyResult.Error(message = response.body()!!.message)
        } else if (!errorBody.isNullOrEmpty()) {
            val jsonObject = JSONObject(errorBody)
            MyResult.Error(message = jsonObject.optString("message"))
        } else
            MyResult.Error(message = "Retrieving data from server was not successful: ${response.body()}")
    }

}