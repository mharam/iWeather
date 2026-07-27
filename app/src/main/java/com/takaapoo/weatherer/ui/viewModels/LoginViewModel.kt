package com.takaapoo.weatherer.ui.viewModels

import androidx.lifecycle.ViewModel
import com.takaapoo.weatherer.data.remote.UserAuthResponse
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.domain.model.LoginState
import com.takaapoo.weatherer.utility.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    val user: User
): ViewModel() {
    private val _loginState = MutableStateFlow(LoginState())
    val loginState = _loginState.asStateFlow()



    fun updateLoginState(state: MyResult<out UserAuthResponse>) {
        _loginState.update {
            it.copy(
                state = state
            )
        }
    }

}