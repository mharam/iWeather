package com.takaapoo.weatherer.domain.model

import com.android.billingclient.api.ProductDetails

data class BillingState(
    val hasDonatedINAPP: Boolean? = null,
    val hasDonatedSUBS: Boolean? = null,
    val billingResultResponseCodes: List<Int> = emptyList(),
    val productDetailsList: List<ProductDetails>? = null,
    val purchasedProductsIds: List<String> = emptyList(),
    val purchaseSuccessDialogVisibility: Boolean = false,
)
