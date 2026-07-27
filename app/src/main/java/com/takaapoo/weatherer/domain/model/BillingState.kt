package com.takaapoo.weatherer.domain.model

import com.android.billingclient.api.ProductDetails

data class BillingState(
    val billingResultResponseCodes: List<Int> = emptyList(),
    val productDetailsList: List<ProductDetails>? = null,
    val purchasedProductsIds: List<String> = emptyList(),
    val purchaseSuccessDialogVisibility: Boolean = false,
)
