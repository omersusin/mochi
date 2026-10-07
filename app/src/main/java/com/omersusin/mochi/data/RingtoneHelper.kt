package com.omersusin.mochi.data

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import java.io.File
import java.io.FileOutputStream

enum class RingtoneSlot(val type: Int, val label: String) {
    ALARM(RingtoneManager.TYPE_ALARM, "Alarm"),
    RINGTONE(RingtoneManager.TYPE_RINGTONE, "Ringtone"),
    NOTIFICATION(RingtoneManager.TYPE_NOTIFICATION, "Notification"),
}

object RingtoneHelper {
    fun needsPermissionScreen(context: Context): Boolean =
        !Settings.System.canWrite(context)

    fun openPermissionScreen(context: Context) {
        val intent = Intent(
            Settings.ACTION_MANAGE_WRITE_SETTINGS,
            Uri.parse("package:${context.packageName}"),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /** Copies the bundled ogg into MediaStore and sets it as the system default. */
    fun setAs(context: Context, resName: String, slot: RingtoneSlot): Boolean {
        if (needsPermissionScreen(context)) return false
        val resId = context.resources.getIdentifier(resName, "raw", context.packageName)
        if (resId == 0) return false
        val fileName = "$resName.ogg"
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/ogg")
            put(MediaStore.Audio.Media.IS_ALARM, slot == RingtoneSlot.ALARM)
            put(MediaStore.Audio.Media.IS_RINGTONE, slot == RingtoneSlot.RINGTONE)
            put(MediaStore.Audio.Media.IS_NOTIFICATION, slot == RingtoneSlot.NOTIFICATION)
            if (Build.VERSION.SDK_INT >= 29) {
                put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_NOTIFICATIONS + "/Mochi")
                put(MediaStore.Audio.Media.IS_PENDING, 1)
            }
        }
        val collection =
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uri = context.contentResolver.insert(collection, values) ?: return false
        try {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                context.resources.openRawResource(resId).use { `in` -> `in`.copyTo(out) }
            } ?: return false
            if (Build.VERSION.SDK_INT >= 29) {
                values.clear()
                values.put(MediaStore.Audio.Media.IS_PENDING, 0)
                context.contentResolver.update(uri, values, null, null)
            }
            RingtoneManager.setActualDefaultRingtoneUri(context, slot.type, uri)
            return true
        } catch (_: Exception) {
            try {
                context.contentResolver.delete(uri, null, null)
            } catch (_: Exception) {
            }
            return false
        }
    }

    @Suppress("unused")
    fun exportToDownloads(context: Context, resName: String): File? {
        val resId = context.resources.getIdentifier(resName, "raw", context.packageName)
        if (resId == 0) return null
        return try {
            val out = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "$resName.ogg")
            FileOutputStream(out).use { o ->
                context.resources.openRawResource(resId).use { it.copyTo(o) }
            }
            out
        } catch (_: Exception) {
            null
        }
    }
}
