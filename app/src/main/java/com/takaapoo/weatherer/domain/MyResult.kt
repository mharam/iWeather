package com.takaapoo.weatherer.domain


sealed class MyResult<T> {
    data class Success<T>(val data: T): MyResult<T>()
    data class Error<T>(val exception: Exception? = null, val message: String? = null): MyResult<T>()
    class Loading<T> : MyResult<T>()
}