package com.takaapoo.weatherer.utility

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.net.ssl.SSLException

class HttpFallbackInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        try {
            // Attempt original HTTPS request
            return chain.proceed(request)
        } catch (e: Exception) {
            // Check if it's an SSL/Handshake error and the original was HTTPS
            if (request.isHttps && (e is SSLException || e is IOException)) {
                val httpUrl = request.url.newBuilder().scheme("http").build()
                val httpRequest = request.newBuilder().url(httpUrl).build()
                return chain.proceed(httpRequest)
            }
            throw e
        }
    }
}
