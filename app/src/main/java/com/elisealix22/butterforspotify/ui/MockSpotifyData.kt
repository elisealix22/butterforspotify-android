package com.elisealix22.butterforspotify.ui

import com.spotify.protocol.types.Album
import com.spotify.protocol.types.Artist
import com.spotify.protocol.types.ImageUri
import com.spotify.protocol.types.PlayerOptions
import com.spotify.protocol.types.PlayerRestrictions
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Track

val Artist1 = Artist("Beyonce", "uri://beyonce")
val Artist2 = Artist("Shaboozey", "uri://shaboozey")
val Album1 = Album("COWBOY CARTER", "uri://cowboycarter")
val ImageUri1 = ImageUri("uri://image1")

val Track1 = Track(
    Artist1,
    listOf(Artist1, Artist2),
    Album1,
    1000L,
    "SWEET HONEY BUCKIN'",
    "uri://sweet-honey-buckin",
    ImageUri1,
    false,
    false
)

val PlayerOptions1 = PlayerOptions(false, 1)

val PlayerRestrictions1 = PlayerRestrictions(false, false, false, false, false, false)

val PlayerState1 = PlayerState(
    Track1,
    false,
    1.0F,
    30L,
    PlayerOptions1,
    PlayerRestrictions1
)
