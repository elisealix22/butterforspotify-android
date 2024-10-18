package com.elisealix22.butterforspotify.data.model.playlist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class HistoryContextType(val value: String) {
    @SerialName("album")
    ALBUM("album"),

    @SerialName("playlist")
    PLAYLIST("playlist"),

    @SerialName("show")
    SHOW("show"),

    @SerialName("artist")
    ARTIST("artist"),

    @SerialName("unknown")
    UNKNOWN("unknown")
}
