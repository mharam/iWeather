package com.takaapoo.weatherer.utility

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.PasswordCredential
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialCustomException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.takaapoo.weatherer.data.remote.UserAuthResponse
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.domain.repository.PreferenceRepository
import com.takaapoo.weatherer.domain.repository.UserRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.security.SecureRandom
import kotlin.time.Duration.Companion.milliseconds

private const val TAG = "Auth"

@Singleton
class User @Inject constructor(
    val userRepository: UserRepository,
    val preferenceRepository: PreferenceRepository
) {
    var trialPeriodExpired: Boolean? = null
    private val coroutineScope = CoroutineScope(SupervisorJob())

    fun initialize(){
        coroutineScope.launch(Dispatchers.IO) {
            val settings = preferenceRepository.getSettings()
            settings.userId?.let {
                val result = userRepository.getTrialExpired(it)
                if (result is MyResult.Success && result.data.exist){
                    trialPeriodExpired = result.data.trialExpired
                }
            }
        }
    }

    suspend fun signIn(context: Context): MyResult<out UserAuthResponse> {
        val idToken = getGoogleIdToken(context)
        // SEND THIS 'idToken' TO SERVER
        idToken?.let {
            Log.d(TAG, "Successfully fetched ID Token: idToken = $it")
            val response = userRepository.verifyGoogleToken(it)
            if (response is MyResult.Success) {
                preferenceRepository.apply {
                    setUserId(response.data.userId)
                    setUserName(response.data.name)
                    setUserPicture(response.data.picture)
                }
                Log.d(
                    TAG,
                    "Successfully verified ID Token: username & Id = ${response.data.name} , ${response.data.userId}"
                )
                return response
            } else if (response is MyResult.Error)
                Log.d(TAG, "Error in verifying ID Token = ${response.message} , ${response.exception?.message}")

            return MyResult.Error(message = "Error in verifying ID Token!")
        }
        return MyResult.Error(message = "Error in retrieving ID Token!")
    }

    suspend fun getGoogleIdToken(
        context: Context,
//        onVerifyIdToken: suspend (idToken: String) -> MyResult<out UserAuthResponse>,
    ): String? {
        val webClientId = "198468825984-05h5v3hqulfj469p1q7mr2is7895a6sp.apps.googleusercontent.com"

        // Using GetSignInWithGoogleOption is recommended for explicit button clicks
        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(webClientId)
            .setNonce(generateSecureRandomNonce())
            .build()

//        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
//            .setFilterByAuthorizedAccounts(true)
//            .setServerClientId(webClientId)
//            .setNonce(generateSecureRandomNonce())
//            .setAutoSelectEnabled(true)
//            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        val result = getCredential(request, context)
        if (result is MyResult.Success) {
            Log.d(TAG, "Filter Authorized accounts TRUE!")
            return handleCredentialResponse(
                response = result.data as GetCredentialResponse,
            )
        } else {
//            val googleIdOptionFalse: GetGoogleIdOption = GetGoogleIdOption.Builder()
//                .setFilterByAuthorizedAccounts(false)
//                .setServerClientId(webClientId)
//                .setNonce(generateSecureRandomNonce())
//                .setAutoSelectEnabled(false)
//                .build()
//
//            val requestFalse: GetCredentialRequest = GetCredentialRequest.Builder()
//                .addCredentialOption(googleIdOptionFalse)
//                .build()
//
//            val resultFalse = getCredential(requestFalse, context)
//            if (resultFalse is MyResult.Success)
//                return handleCredentialResponse(
//                    response = resultFalse.data as GetCredentialResponse,
//                )

            return null
        }
    }

    fun generateSecureRandomNonce(byteLength: Int = 32): String {
        val randomBytes = ByteArray(byteLength)
        SecureRandom().nextBytes(randomBytes)
        return Base64.encodeToString(randomBytes, Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING)
    }

    private fun handleCredentialResponse(
        response: GetCredentialResponse,
//        onVerifyIdToken: suspend (idToken: String) -> MyResult<out UserAuthResponse>,
    ): String? {
        var idToken: String? = null
        when (val credential = response.credential) {
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
        return idToken
    }


    //This code will not work on Android versions < UPSIDE_DOWN_CAKE when GetCredentialException is
//thrown.
    suspend fun getCredential(request: GetCredentialRequest, context: Context): MyResult<out GetCredentialResponse?> {
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
        } catch (e: NoCredentialException) {
            Log.e(TAG, "$failureMessage: No matching credentials found. Verify your Google accounts and Server Client ID.", e)
            result = MyResult.Error(e)
        } catch (e: GetCredentialCancellationException) {
            Log.i(TAG, "Sign-in was cancelled by user")
            result = MyResult.Error(e)
        } catch (e: GoogleIdTokenParsingException) {
            Log.e(TAG, "$failureMessage: Issue with parsing received GoogleIdToken", e)
            result = MyResult.Error(e)
        } catch (e: GetCredentialCustomException) {
            Log.e(TAG, "$failureMessage: Issue with custom credential request", e)
            result = MyResult.Error(e)
        } catch (e: GetCredentialException) {
            Log.e(TAG, "$failureMessage: Unexpected error during sign in", e)
            result = MyResult.Error(e)
        }
        return result
    }
}