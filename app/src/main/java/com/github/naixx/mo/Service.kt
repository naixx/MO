package com.github.naixx.mo

import retrofit2.Call
import retrofit2.http.GET

data class Response(val pngImagesInBase64: List<String>)

interface Service {
    @GET("images")
    fun images(): Call<Response>
}
