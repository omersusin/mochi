package com.omersusin.mochi.data

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Single-voice preview player. Only one sound plays at a time. */
object SoundPlayer {
    private var player: MediaPlayer? = null
    private val _playing = MutableStateFlow<String?>(null)
    val playing: StateFlow<String?> = _playing

    private val durations = mutableMapOf<String, String>()

    fun toggle(context: Context, resName: String) {
        if (_playing.value == resName) {
            stop()
            return
        }
        stop()
        val id = context.resources.getIdentifier(resName, "raw", context.packageName)
        if (id == 0) return
        player = MediaPlayer.create(context, id)?.also {
            it.setOnCompletionListener { stop() }
            it.start()
            _playing.value = resName
        }
    }

    fun stop() {
        player?.release()
        player = null
        _playing.value = null
    }

    fun duration(context: Context, resName: String): String = durations.getOrPut(resName) {
        val id = context.resources.getIdentifier(resName, "raw", context.packageName)
        if (id == 0) return "–"
        try {
            val r = MediaMetadataRetriever()
            context.resources.openRawResourceFd(id).use { fd ->
                r.setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
            }
            val ms = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            r.release()
            "%d:%02d".format(ms / 60_000, (ms / 1_000) % 60)
        } catch (_: Exception) {
            "–"
        }
    }
}
