package com.takaapoo.weatherer.ui.viewModels

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.android.billingclient.api.ProductDetails
import com.takaapoo.weatherer.domain.model.DonatePaneContent
import com.takaapoo.weatherer.domain.model.DonateState
import com.takaapoo.weatherer.domain.repository.PreferenceRepository
import com.takaapoo.weatherer.utility.Billing
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject


@HiltViewModel
class DonateViewModel @Inject constructor(
//    @param:ApplicationContext val applicationContext: Context,
    val preferenceRepository: PreferenceRepository,
    val billing: Billing,
): ViewModel() {

//    val oneTimeProductsIds = listOf("donate_03", "donate_05", "donate_10")
//    val subscriptionProductsIds = listOf("donate_03_year", "donate_05_year")
//    var purchasedProductsIds = mutableSetOf<String>()

    private val _donateState = MutableStateFlow(DonateState())
    val donateState = _donateState.asStateFlow()

//    private var connectRetryCount = 0

    /*private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
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
    }*/

//    val billingClient = BillingClient.newBuilder(applicationContext)
//        .setListener(purchasesUpdatedListener)
//        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
//        .build()

//    init {
//        startBillingConnection()
//    }

    fun startBillingConnection() {
        billing.startBillingConnection()
        /*billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                connectRetryCount = 0
                val ready = billingResult.responseCode == BillingResponseCode.OK
//                updateBillingClientResponseCode(billingResult.responseCode)
                if (ready) {
                    processPurchases()
                    queryPurchases()
                }
            }
            override fun onBillingServiceDisconnected() {
                connectRetryCount++
                if (connectRetryCount < 4) {
//                    updateBillingClientResponseCode(null)
                    startBillingConnection()
                } else {
//                    updateBillingClientResponseCode(BillingResponseCode.SERVICE_DISCONNECTED)
                    connectRetryCount = 0
                }
            }
        })*/
    }

    /*fun queryPurchases() {
        val queryInAppPurchaseParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        billingClient.queryPurchasesAsync(queryInAppPurchaseParams){
            billingResult: BillingResult, purchases: List<Purchase?> ->
            if (billingResult.responseCode == BillingResponseCode.OK) {
                purchases.forEach { purchase ->
                    purchase?.let {
                        handlePurchase(purchase)
                    }
                }
            }
        }

        val querySubscriptionPurchaseParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        billingClient.queryPurchasesAsync(querySubscriptionPurchaseParams){
                billingResult: BillingResult, purchases: List<Purchase?> ->
            if (billingResult.responseCode == BillingResponseCode.OK) {
                purchases.forEach { purchase ->
                    purchasedProductsIds.addAll(purchase?.products ?: emptyList())
                    purchase?.let {
                        handlePurchase(purchase)
                    }
                }
                updatePurchasedProductsIds(purchasedProductsIds.toList())
            }
        }
    }*/

    /*fun processPurchases() {
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


        val productDetailsResultDeferred = viewModelScope.async(Dispatchers.IO) {
            billingClient.queryProductDetails(params.build())
        }
        val supportedResult = billingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS)
        val subscriptionDetailsResultDeferred = viewModelScope.async(Dispatchers.IO) {
            billingClient.queryProductDetails(subsParams.build())
        }
        viewModelScope.launch {
            updateBillingResultResponseCodes(emptyList())

            val detailsResult = if (supportedResult.responseCode == BillingResponseCode.OK)
                awaitAll(
                    productDetailsResultDeferred,
                    subscriptionDetailsResultDeferred
                )
            else
                awaitAll(productDetailsResultDeferred)

            val productDetailsList = detailsResult.map {
                it.productDetailsList ?: emptyList()
            }.flatten()
            val billingResultCodes = detailsResult.map {
                it.billingResult.responseCode
            }

            updateProductDetailsList(productDetailsList)
            updateBillingResultResponseCodes(billingResultCodes)
        }

    }*/

    fun launchBillingFlow(activity: Activity, productDetails: ProductDetails){
        billing.launchBillingFlow(activity, productDetails)
    }

    /*fun handlePurchase(purchase: Purchase) {
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
            viewModelScope.launch(Dispatchers.IO) {
                preferenceRepository.updateDonateState(state = true)
            }
            purchase.products.forEach { id ->
                if (oneTimeProductsIds.contains(id)){
                    val consumeParams = ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    viewModelScope.launch(Dispatchers.IO) {
                        val consumeResult = billingClient.consumePurchase(consumeParams)
                        if (consumeResult.billingResult.responseCode == BillingResponseCode.OK){
                            updatePurchaseSuccessDialogVisibility(true)
                        }
                    }
                }
            }
            Log.i("purchase1", "isAcknowledged = ${purchase.isAcknowledged}")
            if (!purchase.isAcknowledged){
                purchase.products.forEach { id ->
                    if (subscriptionProductsIds.contains(id)){
                        val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()
                        viewModelScope.launch(Dispatchers.IO) {
                            Log.i("purchase1", "Acknowledging")
                            val acknowledgeResult = billingClient.acknowledgePurchase(acknowledgeParams)
                            if (acknowledgeResult.responseCode == BillingResponseCode.OK){
                                updatePurchaseSuccessDialogVisibility(true)
                            }
                        }
                    }
                }
            }
        }


        // Step 3: Notify the user using appropriate messaging (delaying
        // notification if needed as discussed above).

        // Step 4: Notify Google the purchase was processed using the steps
        // discussed in the processing purchases section.
    }*/





//    private fun updateBillingClientResponseCode(code: Int?){
//        _donateState.update {
//            it.copy(billingClientResponseCode = code)
//        }
//    }

//    private fun updateBillingResultResponseCodes(codes: List<Int>){
//        _donateState.update {
//            it.copy(billingResultResponseCodes = codes)
//        }
//    }

//    private fun updateProductDetailsList(productDetails: List<ProductDetails>?){
//        _donateState.update {
//            it.copy(productDetailsList = productDetails)
//        }
//    }
//
//    private fun updatePurchasedProductsIds(ids: List<String>){
//        _donateState.update {
//            it.copy(purchasedProductsIds = ids)
//        }
//    }



    fun updatePaneContent(content: DonatePaneContent){
        _donateState.update {
            it.copy(paneContent = content)
        }
    }

//    fun updatePurchaseSuccessDialogVisibility(visible: Boolean){
//        _donateState.update {
//            it.copy(purchaseSuccessDialogVisibility = visible)
//        }
//    }

}