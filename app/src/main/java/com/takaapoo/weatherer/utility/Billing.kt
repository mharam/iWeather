package com.takaapoo.weatherer.utility

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.Purchase.PurchaseState
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.takaapoo.weatherer.domain.model.BillingState
import com.takaapoo.weatherer.domain.repository.PreferenceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Singleton
class Billing @Inject constructor(
    @param:ApplicationContext private val context: Context,
    val preferenceRepository: PreferenceRepository,
) {
    var hasDonated: Boolean? = null
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val oneTimeProductsIds = listOf("donate_03", "donate_05", "donate_10")
    private val subscriptionProductsIds = listOf("donate_03_year", "donate_05_year")
    private var purchasedProductsIds = mutableSetOf<String>()
    private var productDetailsList: List<ProductDetails>? = null

    private val _billingState = MutableStateFlow(BillingState())
    val billingState = _billingState.asStateFlow()

    private var connectRetryCount = 0

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        Log.i("purchase1", "response code = ${billingResult.responseCode}")
        when (billingResult.responseCode) {
            BillingResponseCode.OK if purchases != null -> {
                for (purchase in purchases) {
                    handlePurchase(purchase)
                }
            }
            BillingResponseCode.USER_CANCELED -> {
                // Handle an error caused by a user canceling the purchase flow.
            }
            else -> {
                // Handle any other error codes.
            }
        }
    }

    val billingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()


    fun startBillingConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                connectRetryCount = 0
                if (billingResult.responseCode == BillingResponseCode.OK) {
                    processPurchases()
                    queryPurchases()
                }
            }
            override fun onBillingServiceDisconnected() {
                connectRetryCount++
                if (connectRetryCount < 4) {
                    startBillingConnection()
                } else {
                    connectRetryCount = 0
                }
            }
        })
    }

    fun queryPurchases() {
        purchasedProductsIds.clear()
        val queryInAppPurchaseParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        billingClient.queryPurchasesAsync(queryInAppPurchaseParams){ billingResult: BillingResult, purchases: List<Purchase?> ->
            if (billingResult.responseCode == BillingResponseCode.OK) {
                coroutineScope.launch {
                    if (purchases.isEmpty() && hasDonated != null) {
                        preferenceRepository.updateDonateState(state = false)
                        hasDonated = false
                    }
                    purchases.forEach { purchase ->
                        purchasedProductsIds.addAll(purchase?.products ?: emptyList())
                        purchase?.let {
                            handlePurchase(purchase)
                        }
                    }
                    updatePurchasedProductsIds(purchasedProductsIds.toList())
                }
            }
        }

        val querySubscriptionPurchaseParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        billingClient.queryPurchasesAsync(querySubscriptionPurchaseParams){ billingResult: BillingResult, purchases: List<Purchase?> ->
            if (billingResult.responseCode == BillingResponseCode.OK) {
                coroutineScope.launch {
                    if (purchases.isEmpty() && hasDonated != null) {
                        preferenceRepository.updateDonateState(state = false)
                        hasDonated = false
                    }
                    purchases.forEach { purchase ->
                        purchasedProductsIds.addAll(purchase?.products ?: emptyList())
                        purchase?.let {
                            handlePurchase(purchase)
                        }
                    }
                    updatePurchasedProductsIds(purchasedProductsIds.toList())
                }
            }
        }
    }

    fun processPurchases() {
        val productBuilder = QueryProductDetailsParams.Product.newBuilder()
        val productList = oneTimeProductsIds.map { ids ->
            productBuilder.setProductId(ids)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder()
        params.setProductList(productList)

        val subscriptionList = subscriptionProductsIds.map { ids ->
            productBuilder.setProductId(ids)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }
        val subsParams = QueryProductDetailsParams.newBuilder()
        subsParams.setProductList(subscriptionList)

        coroutineScope.launch {
            val productDetailsResultDeferred = async(Dispatchers.IO) {
                billingClient.queryProductDetails(params.build())
            }
            val supportedResult =
                billingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS)
            val subscriptionDetailsResultDeferred = async(Dispatchers.IO) {
                billingClient.queryProductDetails(subsParams.build())
            }
            updateBillingResultResponseCodes(emptyList())
            val detailsResult = if (supportedResult.responseCode == BillingResponseCode.OK)
                awaitAll(
                    productDetailsResultDeferred,
                    subscriptionDetailsResultDeferred
                )
            else
                awaitAll(productDetailsResultDeferred)

            productDetailsList = detailsResult.flatMap {
                it.productDetailsList ?: emptyList()
            }
            val billingResultCodes = detailsResult.map {
                it.billingResult.responseCode
            }
            updateProductDetailsList(productDetailsList)
            updateBillingResultResponseCodes(billingResultCodes)
        }
    }

    fun handlePurchase(purchase: Purchase) {
        // Purchase retrieved from BillingClient#queryPurchasesAsync or your
        // onPurchasesUpdated.


        // Step 1: Send the purchase to your secure backend to verify the purchase
        // following
        // https://developer.android.com/google/play/billing/security#verify

        // Step 2: Update your entitlement storage with the purchase. If purchase is
        // in PENDING state then ensure the entitlement is marked as pending and the
        // user does not receive benefits yet. It is recommended that this step is
        // done on your secure backend and can combine in the API call to your
        // backend in step 1.
        Log.i("purchase1", "handlePurchase: ${purchase.purchaseState}")
        if (purchase.purchaseState == PurchaseState.PURCHASED) {
            coroutineScope.launch {
                preferenceRepository.updateDonateState(state = true)
                hasDonated = true

                /*purchase.products.forEach { id ->
                    if (oneTimeProductsIds.contains(id)){
                        val consumeParams = ConsumeParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()
                        launch {
                            val consumeResult = billingClient.consumePurchase(consumeParams)
                            if (consumeResult.billingResult.responseCode == BillingResponseCode.OK){
                                updatePurchaseSuccessDialogVisibility(true)
                            }
                        }
                    }
                }*/
                Log.i("purchase1", "isAcknowledged = ${purchase.isAcknowledged}")
                if (!purchase.isAcknowledged){
                    if (oneTimeProductsIds.containsAny(purchase.products) &&
                        subscriptionProductsIds.containsAny(purchase.products)) {
                        val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()

                        Log.i("purchase1", "Acknowledging")
                        val acknowledgeResult = billingClient.acknowledgePurchase(acknowledgeParams)
                        if (acknowledgeResult.responseCode == BillingResponseCode.OK){
                            updatePurchaseSuccessDialogVisibility(true)
                        }
                    }

                    /*if (subscriptionProductsIds.containsAny(purchase.products)){
                        val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()

                        Log.i("purchase1", "Acknowledging")
                        val acknowledgeResult = billingClient.acknowledgePurchase(acknowledgeParams)
                        if (acknowledgeResult.responseCode == BillingResponseCode.OK){
                            updatePurchaseSuccessDialogVisibility(true)
                        }
                    }*/
                } else {

                }
            }
        }

        // Step 3: Notify the user using appropriate messaging (delaying
        // notification if needed as discussed above).

        // Step 4: Notify google the purchase was processed using the steps
        // discussed in the processing purchases section.
    }

    fun launchBillingFlow(activity: Activity, productDetails: ProductDetails){
        val subToken = productDetails.subscriptionOfferDetails?.get(0)?.offerToken
        val billingFlowParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
        val productDetailsParamsList = listOf(
            if (subToken != null) {
                billingFlowParamsBuilder
                    // For One-time products, "setOfferToken" method shouldn't be called.
                    // For subscriptions, to get an offer token, call ProductDetails.subscriptionOfferDetails()
                    // for a list of offers that are available to the user
                    .setOfferToken(subToken)
                    .build()
            } else {
                billingFlowParamsBuilder.build()
            }
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .setIsOfferPersonalized(true)
            .build()

        val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)
    }





    private fun updateBillingResultResponseCodes(codes: List<Int>){
        _billingState.update {
            it.copy(billingResultResponseCodes = codes)
        }
    }

    private fun updateProductDetailsList(productDetails: List<ProductDetails>?){
        _billingState.update {
            it.copy(productDetailsList = productDetails)
        }
    }

    private fun updatePurchasedProductsIds(ids: List<String>){
        _billingState.update {
            it.copy(purchasedProductsIds = ids)
        }
    }

    fun updatePurchaseSuccessDialogVisibility(visible: Boolean){
        _billingState.update {
            it.copy(
                purchaseSuccessDialogVisibility = visible
            )
        }
    }
}