package com.example.to_doappbyorlov.network

import retrofit2.Call
import retrofit2.http.GET

interface AdviceSlipApiService {
    @GET("advice")
    fun getRandomAdvice(): Call<AdviceSlipResponse>
} 