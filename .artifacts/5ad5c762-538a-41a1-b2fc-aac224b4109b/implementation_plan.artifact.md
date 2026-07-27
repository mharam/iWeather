# Implementation Plan - Fix BAD_AUTHENTICATION in Google Sign-In

The user is experiencing a `BAD_AUTHENTICATION` error when attempting to sign in with Google using Credential Manager. This error, accompanied by "Long live credential not available," typically indicates a mismatch between the application's configuration (Package Name, SHA-1) and the Google Cloud Console settings, or a stale session being incorrectly handled by `autoSelect`.

## User Review Required

> [!IMPORTANT]
> **Google Cloud Console Configuration Check**:
> The `BAD_AUTHENTICATION` error for `oauth2:openid` is almost always caused by one of the following:
> 1. The **Server Client ID** (`webClientId`) used in the code must be of type **Web application** in the Google Cloud Console.
> 2. The **Android** client ID in the same Google Cloud project must have the correct **Package Name** (`com.takaapoo.weatherer.debug`) and **SHA-1 certificate fingerprint** for your debug signing key.
> 3. Ensure the Google Play Services on the test device are up to date.

## Proposed Changes

We will refactor the authentication logic to use the modern `GetSignInWithGoogleOption` (as recommended by Google for sign-in buttons) and improve the robustness of the fallback mechanism.

### [utility](file:///D:/My%20Apps/iWeather/app2/app/src/main/java/com/takaapoo/weatherer/utility)

#### [MODIFY] [User.kt](file:///D:/My%20Apps/iWeather/app2/app/src/main/java/com/takaapoo/weatherer/utility/User.kt)
- Switch from `GetGoogleIdOption` to `GetSignInWithGoogleOption` for a better user experience and more robust GMS interaction.
- Refactor `getGoogleIdToken` to handle `BAD_AUTHENTICATION` more gracefully.
- Consolidate common logic: Move `generateSecureRandomNonce` and `handleCredentialResponse` to a shared location or ensure they are reused.

### [ui/screens/detail/historical](file:///D:/My%20Apps/iWeather/app2/app/src/main/java/com/takaapoo/weatherer/ui/screens/detail/historical)

#### [MODIFY] [DonatePromotion.kt](file:///D:/My%20Apps/iWeather/app2/app/src/main/java/com/takaapoo/weatherer/ui/screens/detail/historical/DonatePromotion.kt)
- Remove redundant authentication functions (`getGoogleIdToken`, `signIn`, `generateSecureRandomNonce`) and use the `User` class instead.

## Verification Plan

### Manual Verification
- Attempt to sign in on a device with the debug build.
- Observe the Logcat for "Filter Authorized accounts TRUE!" or "Sign in successful".
- If `BAD_AUTHENTICATION` persists, the user should be prompted with a more descriptive error or directed to check their Google Cloud Console configuration.

### Automated Tests
- No specific automated tests can verify GMS authentication failures, but we can verify that the `GetCredentialRequest` is built correctly.
