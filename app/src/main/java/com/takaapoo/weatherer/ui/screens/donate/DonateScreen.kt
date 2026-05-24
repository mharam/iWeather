package com.takaapoo.weatherer.ui.screens.donate

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ConnectionState
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.ProductDetails
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.model.DonatePaneContent
import com.takaapoo.weatherer.domain.model.DonateState
import com.takaapoo.weatherer.ui.theme.LoadingData
import com.takaapoo.weatherer.ui.theme.Transparent
import com.takaapoo.weatherer.ui.theme.customColorScheme
import com.takaapoo.weatherer.ui.theme.gerdFontFamily


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DonateScreen(
    modifier: Modifier = Modifier,
    singlePane: Boolean,
    donateState: DonateState,
    billingConnectionState: Int,
    drawerOpen: Boolean,

    onNavigateUp: () -> Unit,
    onNavigateToPay: () -> Unit,
    onConnectRetry: () -> Unit,
    onPurchaseProduct: (ProductDetails) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Donate",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateUp
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (singlePane) {
            AnimatedContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding()),
                targetState = donateState.paneContent,
                label = "donateContent",
                transitionSpec = {
                    ContentTransform(
                        targetContentEnter = slideInHorizontally(
                            initialOffsetX = { (0.6f * it).toInt() }
                        ) + fadeIn(),
                        initialContentExit = slideOutHorizontally(
                            targetOffsetX = { (-0.6f * it).toInt() }
                        ) + fadeOut()
                    )
                }
            ) { donateContent ->
                when (donateContent){
                    DonatePaneContent.MOTIVATION -> {
                        MotivationScreen(
                            modifier = Modifier,
                            bottomContentPadding = paddingValues.calculateBottomPadding(),
                            onNavigateUp = onNavigateUp,
                            onNavigateToPay = onNavigateToPay
                        )
                    }
                    DonatePaneContent.PAY -> {
                        PayScreen(
                            modifier = Modifier,
                            billingConnectionState = billingConnectionState,
                            billingResultResponseCodes = donateState.billingResultResponseCodes,
                            productDetailsList = donateState.productDetailsList,
                            purchasedProductIds = donateState.purchasedProductsIds,
                            drawerOpen = drawerOpen,
                            onNavigateUp = onNavigateUp,
                            onConnectRetry = onConnectRetry,
                            onPurchaseProduct = onPurchaseProduct
                        )
                    }
                }
            }
        }
        else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
            ){
                MotivationScreen(
                    modifier = Modifier.fillMaxHeight().weight(1f),
                    bottomContentPadding = paddingValues.calculateBottomPadding(),
                    onNavigateUp = onNavigateUp,
                    onNavigateToPay = onNavigateToPay
                )
                Spacer(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(color = MaterialTheme.customColorScheme.paneSpacerColor)
                )
                AnimatedContent(
                    modifier = Modifier.fillMaxHeight().weight(1f),
                    targetState = donateState.paneContent,
                    label = "donateContent",
                    transitionSpec = {
                        ContentTransform(
                            targetContentEnter = slideInHorizontally(
                                initialOffsetX = { (0.6f * it).toInt() }
                            ) + fadeIn(),
                            initialContentExit = slideOutHorizontally(
                                targetOffsetX = { (-0.6f * it).toInt() }
                            ) + fadeOut()
                        )
                    }
                ) {donateContent ->
                    when (donateContent){
                        DonatePaneContent.MOTIVATION -> {
                            Box(modifier = Modifier.fillMaxSize())
                        }
                        DonatePaneContent.PAY -> {
                            PayScreen(
                                modifier = Modifier.fillMaxSize(),
                                billingConnectionState = billingConnectionState,
                                billingResultResponseCodes = donateState.billingResultResponseCodes,
                                productDetailsList = donateState.productDetailsList,
                                purchasedProductIds = donateState.purchasedProductsIds,
                                drawerOpen = drawerOpen,
                                onNavigateUp = onNavigateUp,
                                onConnectRetry = onConnectRetry,
                                onPurchaseProduct = onPurchaseProduct
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun MotivationScreen(
    modifier: Modifier = Modifier,
    bottomContentPadding: Dp,
    onNavigateUp: () -> Unit,
    onNavigateToPay: () -> Unit
) {
    val lottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.donation))

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ){
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 500.dp)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LottieAnimation(
                composition = lottieComposition,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(330.dp),
                iterations = LottieConstants.IterateForever
            )
            Text(
                text = stringResource(R.string.donation_title),
                style = MaterialTheme.typography.titleMedium,
                fontFamily = gerdFontFamily
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.donation_body),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start
            )
            Spacer(Modifier.height(32.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                onClick = onNavigateToPay
            ) {
                Text(
                    text = stringResource(R.string.donate_positive_button),
                    fontFamily = gerdFontFamily,
                    fontSize = 16.sp
                )
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                onClick = onNavigateUp
            ) {
                Text(
                    text = stringResource(R.string.donate_negative_button),
                    fontFamily = gerdFontFamily,
                    fontSize = 16.sp
                )
            }
            Spacer(Modifier.height(16.dp + bottomContentPadding))

        }
    }
}

@Composable
fun PayScreen(
    modifier: Modifier = Modifier,
    billingConnectionState: Int,
    billingResultResponseCodes: List<Int>,
    productDetailsList: List<ProductDetails>?,
    purchasedProductIds: List<String>,
    drawerOpen: Boolean,
    onNavigateUp: () -> Unit,
    onConnectRetry: () -> Unit,
    onPurchaseProduct: (ProductDetails) -> Unit
) {
    when (billingConnectionState){
        ConnectionState.CONNECTING -> {
            Connecting(
                modifier = modifier.fillMaxSize(),
                text = "Connecting to Play store "
            )
        }
        ConnectionState.CONNECTED -> {
            when {
                billingResultResponseCodes.isEmpty() -> {
                    Connecting(
                        modifier = modifier.fillMaxSize(),
                        text = "Loading donate data "
                    )
                }
                billingResultResponseCodes.all { it == BillingResponseCode.OK } -> {
                    PayOptions(
                        modifier = modifier.fillMaxSize(),
                        productDetailsList = productDetailsList,
                        purchasedProductIds = purchasedProductIds,
                        onPurchaseProduct = onPurchaseProduct
                    )
                }
                else -> {
                    Error(
                        modifier = modifier,
                        text = "Loading donate options failed!",
                        onConnectRetry = onConnectRetry
                    )
                }
            }
        }
        else -> {
            Error(
                modifier = modifier,
                text = "Connecting to Play store failed!",
                onConnectRetry = onConnectRetry
            )
        }
    }
//    BackHandler(enabled = !drawerOpen) {
//        onNavigateUp()
//    }
}

@Composable
fun PayOptions(
    modifier: Modifier = Modifier,
    productDetailsList: List<ProductDetails>?,
    purchasedProductIds: List<String>,
    onPurchaseProduct: (ProductDetails) -> Unit
) {
    val oneTimeProducts = productDetailsList?.filter { it.productType == ProductType.INAPP}
    val subscriptionProducts = productDetailsList?.filter {
        it.productType == ProductType.SUBS && !purchasedProductIds.contains(it.productId)
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        oneTimeProducts?.forEach {
            Spacer(Modifier.height(16.dp))
            Button(
                modifier = Modifier
                    .widthIn(min = 200.dp, max = 280.dp)
                    .padding(horizontal = 24.dp),
                onClick = {
                    onPurchaseProduct(it)
                }
            ) {
                Text(
                    text = it.oneTimePurchaseOfferDetails?.formattedPrice ?: "?",
                    fontFamily = gerdFontFamily,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(Modifier.height(40.dp))

        subscriptionProducts?.forEach { subProduct ->
            subProduct.subscriptionOfferDetails?.forEach { offerDetail ->
                offerDetail.pricingPhases.pricingPhaseList.forEach { pricingPhase ->
                    Spacer(Modifier.height(16.dp))
                    Button(
                        modifier = Modifier
                            .widthIn(min = 200.dp, max = 280.dp)
                            .padding(horizontal = 24.dp),
                        onClick = {
                            onPurchaseProduct(subProduct)
                        }
                    ) {
                        Text(
                            text = "${pricingPhase.formattedPrice} / Year",
                            fontFamily = gerdFontFamily,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Connecting(
    modifier: Modifier = Modifier,
    text: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading data")
    val dotCount by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3.99f,
        animationSpec =  infiniteRepeatable(tween(
            durationMillis = 1200,
            easing = LinearEasing
        )),
        label = "loading..."
    )
    var loadingText = text
    repeat(
        times = dotCount.toInt(),
        action = { loadingText += "." }
    )

    Box(
        modifier = modifier.fillMaxSize()
    ){
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            color = MaterialTheme.colorScheme.primary,
            strokeCap = StrokeCap.Round
        )

        Box(
            modifier = Modifier.align(Alignment.Center)
        ){
            Text(
                text = "$text...",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight(500),
                color = Transparent
            )
            Text(
                text = loadingText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight(500),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun Error(
    modifier: Modifier = Modifier,
    text: String,
    onConnectRetry: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Icon(
            modifier = Modifier.size(88.dp),
            painter = painterResource(R.drawable.error_24px),
            contentDescription = null
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight(500),
            color = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onConnectRetry
        ){
            Text(
                text = "Retry",
                fontFamily = gerdFontFamily,
                fontSize = 16.sp
            )
        }
    }
}