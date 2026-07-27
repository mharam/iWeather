package com.takaapoo.weatherer.ui.screens.detail.historical

import android.content.Context
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.PasswordCredential
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialCustomException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.data.remote.UserAuthResponse
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.ui.theme.gerdFontFamily
import com.takaapoo.weatherer.utility.toDp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.security.SecureRandom
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun DonatePromotion(
    modifier: Modifier = Modifier,
    columnHeight: Float,
    trialPeriodExpired: Boolean?,
//    maxScroll: Float,
//    scrollState: ScrollState,
    onNavigateToDonateScreen: () -> Unit,
//    onVerifyIdToken: suspend (idToken: String) -> MyResult<out UserAuthResponse>,
    onNavigateToLoginScreen: () -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val lottieComposition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.lock_unlock_key)
    )

    Column(
        modifier = modifier
            .widthIn(max = 500.dp)
            .heightIn(min = columnHeight.toDp(density))
            .padding(16.dp),
    ) {
        LottieAnimation(
            composition = lottieComposition,
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            iterations = LottieConstants.IterateForever
        )
        Text(
            text = stringResource(R.string.history_weather_donate_title),
            style = MaterialTheme.typography.titleMedium,
            fontFamily = gerdFontFamily
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.history_weather_donate_motivation),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Start
        )
        Spacer(Modifier.height(32.dp))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            onClick = onNavigateToDonateScreen
        ) {
            Text(
                text = "Donate",
                fontFamily = gerdFontFamily,
                fontSize = 16.sp
            )
        }
        if (trialPeriodExpired != true) {
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                onClick = {
                    onNavigateToLoginScreen()
//                getGoogleIdToken(
//                    context,
//                    coroutineScope,
//                    onVerifyIdToken
//                )
                }
            ) {
                Text(
                    text = stringResource(R.string.free_trial),
                    fontFamily = gerdFontFamily,
                    fontSize = 16.sp
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}


private const val TAG = "Auth"
fun getGoogleIdToken(
    context: Context,
    coroutineScope: CoroutineScope,
    onVerifyIdToken: suspend (idToken: String) -> MyResult<out UserAuthResponse>,
) {
    val webClientId = "198468825984-05h5v3hqulfj469p1q7mr2is7895a6sp.apps.googleusercontent.com"
    
    // Using GetSignInWithGoogleOption is recommended for explicit button clicks
    /*val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(webClientId)
        .setNonce(generateSecureRandomNonce())
        .build()*/

    coroutineScope.launch(Dispatchers.Main) {
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(webClientId)
            .setNonce(generateSecureRandomNonce())
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = signIn(request, context)
        if (result is MyResult.Success) {
            Log.d(TAG, "Filter Authorized accounts TRUE!")
            handleSignInResult(
                result = result.data as GetCredentialResponse,
                onVerifyIdToken = onVerifyIdToken,
            )
        } else {
            val googleIdOptionFalse: GetGoogleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setNonce(generateSecureRandomNonce())
                .setAutoSelectEnabled(false)
                .build()

            val requestFalse: GetCredentialRequest = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOptionFalse)
                .build()

            val resultFalse = signIn(requestFalse, context)
            if (resultFalse is MyResult.Success)
                handleSignInResult(
                    result = resultFalse.data as GetCredentialResponse,
                    onVerifyIdToken = onVerifyIdToken,
                )
        }
    }
}

fun generateSecureRandomNonce(byteLength: Int = 32): String {
    val randomBytes = ByteArray(byteLength)
    SecureRandom().nextBytes(randomBytes)
    return Base64.encodeToString(randomBytes, Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING)
}

private suspend fun handleSignInResult(
    result: GetCredentialResponse,
    onVerifyIdToken: suspend (idToken: String) -> MyResult<out UserAuthResponse>,
) {
    var idToken: String? = null
    when (val credential = result.credential) {
        is GoogleIdTokenCredential -> {
            idToken = credential.idToken
        }
        is CustomCredential -> {
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                try {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    idToken = googleIdTokenCredential.idToken
                } catch (e: GoogleIdTokenParsingException) {
                    Log.e(TAG, "Received invalid google id token response", e)
                }
            } else {
                Log.e(TAG, "Unexpected custom credential type: ${credential.type}")
            }
        }
        is PasswordCredential -> {
            Log.d(TAG, "Password credential returned: ${credential.id}")
        }
        else -> {
            Log.e(TAG, "Unexpected credential type: ${credential::class.java.name}")
        }
    }

    // 5. SEND THIS 'idToken' TO YOUR SERVER HERE VIA RETROFIT/KTOR
    // Do not use 'userEmail' or the raw token contents locally for security verification.
    idToken?.let {
        Log.d(TAG, "Successfully fetched ID Token: idToken = $it")
        val response = onVerifyIdToken(it)
        if (response is MyResult.Success)
            Log.d(TAG, "Successfully verified ID Token: username & Id = ${response.data.name} , ${response.data.userId}")
        else if (response is MyResult.Error)
            Log.d(TAG, "Error in verifying ID Token = ${response.message} , ${response.exception?.message}")
    }
}


//This code will not work on Android versions < UPSIDE_DOWN_CAKE when GetCredentialException is
//thrown.
suspend fun signIn(request: GetCredentialRequest, context: Context): MyResult<out GetCredentialResponse?> {
    val credentialManager = CredentialManager.create(context)
    val failureMessage = "Sign in failed!"
    var result: MyResult<out GetCredentialResponse?>

    // Optional delay to ensure the UI is ready
    delay(250.milliseconds)
    try {
        result = MyResult.Success( credentialManager.getCredential(
            request = request,
            context = context,
        ))
        Log.i(TAG, "Sign in successful: $result")
        Toast.makeText(context, "Sign in successful!", Toast.LENGTH_SHORT).show()
    } catch (e: NoCredentialException) {
        Toast.makeText(context, "No Google accounts found", Toast.LENGTH_SHORT).show()
        Log.e(TAG, "$failureMessage: No matching credentials found. Verify your Google accounts and Server Client ID.", e)
        result = MyResult.Error(e)
    } catch (e: GetCredentialCancellationException) {
        Log.i(TAG, "Sign-in was cancelled by user")
        result = MyResult.Error(e)
    } catch (e: GoogleIdTokenParsingException) {
        Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
        Log.e(TAG, "$failureMessage: Issue with parsing received GoogleIdToken", e)
        result = MyResult.Error(e)
    } catch (e: GetCredentialCustomException) {
        Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
        Log.e(TAG, "$failureMessage: Issue with custom credential request", e)
        result = MyResult.Error(e)
    } catch (e: GetCredentialException) {
        Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
        Log.e(TAG, "$failureMessage: Unexpected error during sign in", e)
        result = MyResult.Error(e)
    }
    return result
}