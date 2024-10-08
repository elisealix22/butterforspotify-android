package com.elisealix22.butterforspotify.data.model.album

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AlbumType(val value: String) {
    @SerialName("album")
    ALBUM("album"),

    @SerialName("single")
    SINGLE("single"),

    @SerialName("compilation")
    COMPILATION("compilation")
}
