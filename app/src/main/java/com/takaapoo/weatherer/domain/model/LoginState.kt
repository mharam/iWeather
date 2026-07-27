package com.takaapoo.weatherer.domain.model

import com.takaapoo.weatherer.data.remote.UserAuthResponse
import com.takaapoo.weatherer.domain.MyResult

data class LoginState(
    val state: MyResult<out UserAuthResponse> = MyResult.Loading()
)
