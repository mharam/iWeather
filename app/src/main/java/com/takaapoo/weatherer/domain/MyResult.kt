package com.takaapoo.weatherer.domain


sealed class MyResult<T> {
    data class Success<T>(val data: T): MyResult<T>()
    data class Error(val exception: Exception? = null, val message: String? = null): MyResult<Nothing>()
    class Loading : MyResult<Nothing>()
}