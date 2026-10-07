package com.omersusin.mochi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.omersusin.mochi.data.SoundPlayer
import com.omersusin.mochi.ui.MochiApp
import com.omersusin.mochi.ui.theme.MochiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MochiTheme { MochiApp() }
        }
    }

    override fun onPause() {
        super.onPause()
        SoundPlayer.stop()
    }
}
