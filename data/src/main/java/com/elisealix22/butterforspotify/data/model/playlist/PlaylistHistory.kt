package com.elisealix22.butterforspotify.data.model.playlist

import com.elisealix22.butterforspotify.data.model.track.Track

data class PlaylistHistory(
    val track: Track,
    val context: HistoryContext?
)
