package com.example.ott

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.STATE_ENDED
import androidx.media3.common.Player.STATE_READY
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.ott.ui.theme.OTTTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OTTTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun PlayerScreen(url: String, adUrl: String) {
    val context = LocalContext.current
    var showAd by remember { mutableStateOf(false) }
    var isAdPlaying by remember { mutableStateOf(false) }
    var adPlaybackState by remember { mutableStateOf(Player.STATE_IDLE) }
    var mainPlaybackState by remember { mutableStateOf(Player.STATE_IDLE) }
    var isMainPlaying by remember { mutableStateOf(false) }
    var adIntervalCounter by remember { mutableStateOf(1) }

    val mainPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            playWhenReady = false
        }
    }

    val adPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(adUrl))
            prepare()
            playWhenReady = false
        }
    }

    DisposableEffect(mainPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                mainPlaybackState = playbackState
            }
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                isMainPlaying = isPlaying
            }
        }
        mainPlayer.addListener(listener)
        onDispose {
            mainPlayer.removeListener(listener)
        }
    }

    DisposableEffect(adPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                adPlaybackState = playbackState
            }
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                isAdPlaying = isPlaying
            }
        }
        adPlayer.addListener(listener)
        onDispose {
            adPlayer.removeListener(listener)
        }
    }

    LaunchedEffect(mainPlayer) {
        while(true) {
            delay(500)
            if (mainPlaybackState == STATE_READY && isMainPlaying) {
                if (mainPlayer.currentPosition > 60 * 1000 * 4 * adIntervalCounter) {
                    adIntervalCounter++
                    mainPlayer.pause()
                    showAd = true
                    adPlayer.play()
                }
            }
        }
    }

    LaunchedEffect(adPlayer) {
        while(true) {
            delay(500)
            if (adPlaybackState == STATE_ENDED && !isAdPlaying) {
                mainPlayer.play()
                showAd = false
                adPlayer.pause()
                adPlayer.seekTo(0L)
            }
        }
    }

    val playerView = remember {
        PlayerView(context)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        @Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
        AndroidView(
            factory = { playerView },
            update = {
                it.player = if (showAd) adPlayer else mainPlayer
            },
            modifier = Modifier.fillMaxWidth().height(300.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button({
                mainPlayer.seekTo(5 * 1000 * 60)
                adIntervalCounter = 2
            }) {
                Text("Skip >4m")
            }
            Button({
                mainPlayer.seekTo(9 * 1000 * 60)
                adIntervalCounter = 3
            }) {
                Text("Skip >8m")
            }
            Button({
                mainPlayer.seekTo(13 * 1000 * 60)
                adIntervalCounter = 4
            }) {
                Text("Skip >12m")
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button({
                mainPlayer.seekTo(3 * 1000 * 60)
                adIntervalCounter = 1
            }) {
                Text("Skip <4m")
            }
            Button({
                mainPlayer.seekTo(7 * 1000 * 60)
                adIntervalCounter = 2
            }) {
                Text("Skip <8m")
            }
            Button({
                mainPlayer.seekTo(11 * 1000 * 60)
                adIntervalCounter = 3
            }) {
                Text("Skip <12m")
            }
        }
    }


    DisposableEffect(Unit) {
        onDispose {
            mainPlayer.release()
            adPlayer.release()
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Column(

    ) {
        Text(
            text = "Hello $name!",
            modifier = modifier
        )
        PlayerScreen(
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
            adUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4")
        Text(
            text = "Hello $name!",
            modifier = modifier
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    OTTTheme {
        Greeting("Android")
    }
}