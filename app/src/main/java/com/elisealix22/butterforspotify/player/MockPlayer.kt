package com.elisealix22.butterforspotify.player

import android.graphics.Bitmap
import com.spotify.android.appremote.api.ImagesApi
import com.spotify.android.appremote.api.PlayerApi
import com.spotify.protocol.client.CallResult
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.Album
import com.spotify.protocol.types.Artist
import com.spotify.protocol.types.CrossfadeState
import com.spotify.protocol.types.Empty
import com.spotify.protocol.types.Image
import com.spotify.protocol.types.ImageUri
import com.spotify.protocol.types.PlaybackSpeed
import com.spotify.protocol.types.PlayerContext
import com.spotify.protocol.types.PlayerOptions
import com.spotify.protocol.types.PlayerRestrictions
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Track
import com.spotify.protocol.types.Types

private val Artist1 = Artist("Beyonce", "uri://beyonce")
private val Artist2 = Artist("Shaboozey", "uri://shaboozey")
private val Album1 = Album("COWBOY CARTER", "uri://cowboycarter")
private val ImageUri1 = ImageUri("uri://image1")

private val Track1 = Track(
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

private val PlayerOptions1 = PlayerOptions(false, 1)

private val PlayerRestrictions1 = PlayerRestrictions(false, false, false, false, false, false)

private val PlayerState1 = PlayerState(
    Track1,
    false,
    1.0F,
    30L,
    PlayerOptions1,
    PlayerRestrictions1
)

private val BitmapCallResult: CallResult<Bitmap> = CallResult(Types.RequestId.NONE)
private val EmptyCallResult: CallResult<Empty> = CallResult(Types.RequestId.NONE)
private val PlayerStateResult: CallResult<PlayerState> = CallResult(Types.RequestId.NONE)
private val CrossfadeResult: CallResult<CrossfadeState> = CallResult(Types.RequestId.NONE)

private val imagesApi = object : ImagesApi {
    override fun getImage(p0: ImageUri?): CallResult<Bitmap> = BitmapCallResult

    override fun getImage(p0: ImageUri?, p1: Image.Dimension?) = BitmapCallResult
}

private val playerApi = object : PlayerApi {
    override fun play(p0: String?): CallResult<Empty> = EmptyCallResult

    override fun play(p0: String?, p1: PlayerApi.StreamType?) = EmptyCallResult

    override fun queue(p0: String?): CallResult<Empty> = EmptyCallResult

    override fun resume(): CallResult<Empty> = EmptyCallResult

    override fun pause(): CallResult<Empty> = EmptyCallResult

    override fun setPodcastPlaybackSpeed(p0: PlaybackSpeed.PodcastPlaybackSpeed?) = EmptyCallResult

    override fun skipNext(): CallResult<Empty> = EmptyCallResult

    override fun skipPrevious(): CallResult<Empty> = EmptyCallResult

    override fun skipToIndex(p0: String?, p1: Int): CallResult<Empty> = EmptyCallResult

    override fun setShuffle(p0: Boolean): CallResult<Empty> = EmptyCallResult

    override fun toggleShuffle(): CallResult<Empty> = EmptyCallResult

    override fun setRepeat(p0: Int): CallResult<Empty> = EmptyCallResult

    override fun toggleRepeat(): CallResult<Empty> = EmptyCallResult

    override fun seekTo(p0: Long): CallResult<Empty> = EmptyCallResult

    override fun seekToRelativePosition(p0: Long): CallResult<Empty> = EmptyCallResult

    override fun getPlayerState(): CallResult<PlayerState> = PlayerStateResult

    override fun subscribeToPlayerState(): Subscription<PlayerState>? = null

    override fun subscribeToPlayerContext(): Subscription<PlayerContext>? = null

    override fun getCrossfadeState(): CallResult<CrossfadeState> = CrossfadeResult
}

val MockPlayer = Player(
    playerState = PlayerState1,
    spotifyApis = SpotifyApis(imagesApi = imagesApi, playerApi = playerApi)
)

val MockPlayerWithCachedState = Player(
    playerState = PlayerState1,
    spotifyApis = null
)
