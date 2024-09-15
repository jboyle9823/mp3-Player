package com.example.jonathanboyle_assignment3_mediaplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Slider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jonathanboyle_assignment3_mediaplayer.ui.theme.Purple40
import com.example.jonathanboyle_assignment3_mediaplayer.ui.theme.Purple80
import kotlinx.coroutines.delay


class DetailsScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val songIndex = intent.getIntExtra("songIndex", -1)
            DetailsScreenContent(songIndex, self = MyMediaService.self)
        }
    }
}
@Composable
fun DetailsScreenContent(songIndex: Int, self: MyMediaService) {
    var songCount by remember { mutableStateOf(songIndex) }
    self.playSongAtIndex(songCount)
    self.updateMediaMetadata()
    self.mediaPlayer.setVolume(0.5f, 0.5f)

    val coverBitmap = remember(songCount) {
        convertBytetoBitmap(MainActivity.musicList[songCount].cover ?: ByteArray(0))
    }

    LaunchedEffect(songCount) {
        self.updateMediaMetadata()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Purple80),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text(
            text = "Track #" + (songCount+1).toString(),
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier.padding(top = 10.dp)
        )
        if (coverBitmap != null) {
            BitmapImageBig(
                bitmap = coverBitmap
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = MainActivity.musicList[songCount].title ?: "Unknown Title",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Text(
            text = MainActivity.musicList[songCount].artist ?: "Unknown Artist",
            fontSize = 18.sp
        )
        Row {
            Text(
                text = "0:00",
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 10.dp, end = 4.dp)
            )
            songSlider(self = self, durationMS = MainActivity.musicList[songCount].durationMS, onSongEnd = {
                songCount += 1
            })
            Text(
                text = MainActivity.musicList[songCount].duration.toString(),
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 10.dp, start = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(101.dp))
        Row {
            Icon(Icons.Default.VolumeDown, contentDescription = null, modifier = Modifier
                .padding(top = 12.dp))
            volumeSlider(self = self)
            Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier
                .padding(top = 12.dp))
        }
        BottomNavigation(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            backgroundColor = Purple40
        ) {
            BottomNavigationItem(
                icon = { Icon(Icons.Default.SkipPrevious, contentDescription = null) },
                label = { Text(text = "Previous") },
                selected = false,
                onClick = {
                    self.prevNexSong(false)
                    songCount -= 1
                }
            )
            BottomNavigationItem(
                icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                label = { Text(text = "Play/Pause") },
                selected = false,
                onClick = {
                    self.playPause()
                }
            )
            BottomNavigationItem(
                icon = { Icon(Icons.Default.SkipNext, contentDescription = null) },
                label = { Text(text = "Next") },
                selected = false,
                onClick = {
                    self.prevNexSong(true)
                    songCount += 1
                }
            )
        }
    }
}

@Composable
fun songSlider(self: MyMediaService, durationMS: Long?, onSongEnd: () -> Unit) {
    var sliderPosition by remember { mutableStateOf(0f) }

    LaunchedEffect(self, durationMS) {
        while (true) {
            var currentPosition = self.mediaPlayer.currentPosition.toFloat()
            sliderPosition = currentPosition / durationMS!!.toFloat()
            delay(100)

            if (currentPosition >= durationMS) {
                onSongEnd()
                currentPosition = 0f
            }
        }
    }

    Column(modifier = Modifier.width(250.dp)) {
        if (durationMS != null) {
            Slider(
                value = sliderPosition,
                onValueChange = {
                    sliderPosition = it
                    self.mediaPlayer.seekTo((it * durationMS).toInt())
                },
            )
        }
    }
}


@Composable
fun volumeSlider(self: MyMediaService) {
    var sliderPosition by remember { mutableStateOf(0.5f) }
    Column(modifier = Modifier.width(275.dp)){
        Slider(
            value = sliderPosition,
            onValueChange = {
                sliderPosition = it
                self.mediaPlayer.setVolume(it,it)
            }
        )
    }
}