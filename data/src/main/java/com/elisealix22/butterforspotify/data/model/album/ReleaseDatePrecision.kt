package com.elisealix22.butterforspotify.data.model.album

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ReleaseDatePrecision(val value: String) {
    @SerialName("year")
    YEAR("year"),

    @SerialName("month")
    MONTH("month"),

    @SerialName("day")
    DAY("day")
}
