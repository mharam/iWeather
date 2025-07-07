package com.takaapoo.weatherer.domain.model

import com.android.billingclient.api.ProductDetails

enum class DonatePaneContent{
    MOTIVATION, PAY
}

data class DonateState(
    val paneContent: DonatePaneContent = DonatePaneContent.MOTIVATION,
//    val billingClientResponseCode: Int? = null,
    val billingResultResponseCodes: List<Int> = emptyList(),
    val productDetailsList: List<ProductDetails>? = null,
    val subscriptionDetailsList: List<ProductDetails>? = null,
    val purchasedProductsIds: List<String> = emptyList(),
    val purchaseSuccessDialogVisibility: Boolean = false,
)