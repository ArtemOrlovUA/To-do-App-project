package com.example.to_doappbyorlov.network

import com.google.gson.annotations.SerializedName

data class AdviceSlipResponse(
    @SerializedName("slip")
    val slip: Slip
)

data class Slip(
    @SerializedName("slip_id")
    val slipId: Int,
    @SerializedName("advice")
    val advice: String
) 