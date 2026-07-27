package com.takaapoo.weatherer.ui.viewModels

import androidx.lifecycle.ViewModel
import com.takaapoo.weatherer.domain.repository.PreferenceRepository
import com.takaapoo.weatherer.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DonatePromotionViewModel @Inject constructor(
    val preferenceRepository: PreferenceRepository,
    val userRepository: UserRepository,
): ViewModel() {


    suspend fun verifyIdToken(idToken: String) =
        userRepository.verifyGoogleToken(idToken)


}