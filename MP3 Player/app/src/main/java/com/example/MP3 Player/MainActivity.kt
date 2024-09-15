package com.example.jonathanboyle_assignment3_mediaplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jonathanboyle_assignment3_mediaplayer.ui.theme.JonathanBoyleAssignment3MediaPlayerTheme
import com.example.jonathanboyle_assignment3_mediaplayer.ui.theme.Purple80

class MainActivity : ComponentActivity() {

    private lateinit var songIds: ArrayList<Int>

    companion object{
        lateinit var musicList: ArrayList<Song>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JonathanBoyleAssignment3MediaPlayerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    musicUI(musicList)
                }
            }
        }
        songIds = arrayListOf(
            R.raw.steelhaze_rustedpride,
            R.raw.themanwhopassedthetorch,
            R.raw.roughanddecent,
            R.raw.shulkandfiora,
            R.raw.engagetheenemy,
            R.raw.youwillknowournames,
            R.raw.maintheme,
            R.raw.tokyo,
            R.raw.battlebtwo,
            R.raw.tosailforbiddenseas,
            R.raw.titanlost,
            R.raw.theriddle
        )

        musicList = arrayListOf()

        var mmr: MediaMetadataRetriever = MediaMetadataRetriever()
        var afd: AssetFileDescriptor
        var count = 0

        for(music in songIds){
            afd = this.getResources().openRawResourceFd(music)
            mmr.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength())
            musicList.add(
                Song(
                    count,
                    music,
                    mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
                    mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                    mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM),
                    mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION),
                    mmr.embeddedPicture
                )
            )
            count++
            afd.close()
        }
    }
    override fun onStart() {
        super.onStart()
        val songIndex = intent.getIntExtra("songIndex", -1)
        if(MyMediaService.started){
            Intent(this, MyMediaService::class.java).also { intent->
                bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }

        }
        else{
            val intent = Intent(this, MyMediaService::class.java).apply {
                putExtra("position", songIndex)
            }
            startForegroundService(intent)
        }
    }

    private lateinit var mService: MyMediaService
    private var mBound: Boolean = false

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as MyMediaService.LocalBinder
            mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }
}



@Composable
fun musicUI(musicList: ArrayList<Song>) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).background(Purple80)
    ) {
        Text(
            modifier = Modifier.padding(5.dp),
            text = " Music Player",
            fontSize = 45.sp,
            fontWeight = FontWeight.Bold,
        )
        musicList.forEach { song ->
            val index = musicList.indexOf(song)
            MusicSong(song = song, index = index, musicList = musicList)
        }
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MusicSong(song: Song, index: Int, musicList: ArrayList<Song>) {
    val shouldShowDialog = remember{mutableStateOf(false)}
    val context = LocalContext.current
    val isDeleted = remember{mutableStateOf(false)}
    if(shouldShowDialog.value){
        AlertDialog(
            title = {Text(text = "Confirm Deletion")},
            text = {Text(text = "Are you sure you want to delete this song?")},
            onDismissRequest = {shouldShowDialog.value = false},
            dismissButton = { Button(onClick = {shouldShowDialog.value = false}) {
                Text(text = "Close")
            }},
            confirmButton = { Button(onClick = {
                isDeleted.value = true
                musicList.removeAt(index)
                shouldShowDialog.value = false}) {
                Text(text = "Confirm")
            }}
        )
    }
    if(!isDeleted.value){
        Row(
            modifier = Modifier
                .combinedClickable(
                    enabled = true,
                    onClick = {
                        val intent = Intent(context, DetailsScreen::class.java).apply {
                            putExtra("songIndex", musicList.indexOf(song))
                        }
                        context.startActivity(intent)
                    },
                    onLongClick = {
                        shouldShowDialog.value = true
                    }
                )
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            val coverBitmap = remember { convertBytetoBitmap(song.cover ?: ByteArray(0)) }
            if (coverBitmap != null) {
                BitmapImage(bitmap = coverBitmap)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = song.title ?: "Unknown Title", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = song.artist ?: "Unknown Artist", fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun BitmapImage(bitmap: Bitmap) {
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "",
        modifier = Modifier
            .size(70.dp)
            .clip(RoundedCornerShape(4.dp))
    )
}

@Composable
fun BitmapImageBig(bitmap: Bitmap) {
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "",
        modifier = Modifier
            .size(380.dp)
            .padding(top = 16.dp)
    )
}

fun convertBytetoBitmap(imageData: ByteArray): Bitmap {
    return BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
}


