package com.omersusin.mochi.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.omersusin.mochi.data.CATALOG
import com.omersusin.mochi.data.FavoritesStore
import com.omersusin.mochi.data.RingtoneHelper
import com.omersusin.mochi.data.RingtoneSlot
import com.omersusin.mochi.data.Sound
import com.omersusin.mochi.data.SoundKind
import com.omersusin.mochi.data.SoundPlayer
import kotlinx.coroutines.launch

private enum class Tab(val label: String) { SOUNDS("Sounds"), FAVORITES("Favorites"), ABOUT("About") }

@Composable
fun MochiApp() {
    var tab by remember { mutableStateOf(Tab.SOUNDS) }
    val snacks = remember { SnackbarHostState() }
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(title = { Text("Mochi", style = MaterialTheme.typography.headlineMedium) })
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == Tab.SOUNDS, onClick = { tab = Tab.SOUNDS },
                    icon = { Icon(Icons.Filled.MusicNote, null) }, label = { Text("Sounds") },
                )
                NavigationBarItem(
                    selected = tab == Tab.FAVORITES, onClick = { tab = Tab.FAVORITES },
                    icon = { Icon(Icons.Filled.Favorite, null) }, label = { Text("Favorites") },
                )
                NavigationBarItem(
                    selected = tab == Tab.ABOUT, onClick = { tab = Tab.ABOUT },
                    icon = { Icon(Icons.Filled.Info, null) }, label = { Text("About") },
                )
            }
        },
        snackbarHost = { SnackbarHost(snacks) },
    ) { pads ->
        when (tab) {
            Tab.SOUNDS -> LibraryScreen(
                sounds = CATALOG, favoritesOnly = false,
                modifier = Modifier.padding(pads), snacks = snacks,
            )
            Tab.FAVORITES -> LibraryScreen(
                sounds = CATALOG, favoritesOnly = true,
                modifier = Modifier.padding(pads), snacks = snacks,
            )
            Tab.ABOUT -> AboutScreen(Modifier.padding(pads))
        }
    }
}

@Composable
private fun LibraryScreen(
    sounds: List<Sound>,
    favoritesOnly: Boolean,
    modifier: Modifier = Modifier,
    snacks: SnackbarHostState,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val store = remember { FavoritesStore(context) }
    val favs by store.favorites.collectAsState(initial = emptySet())
    val playing by SoundPlayer.playing.collectAsState()
    var kind by remember { mutableStateOf<SoundKind?>(null) }
    var setTarget by remember { mutableStateOf<Sound?>(null) }

    val shown = sounds.filter {
        (!favoritesOnly || it.resName in favs) && (kind == null || it.kind == kind)
    }

    Column(modifier.fillMaxSize()) {
        if (!favoritesOnly) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    FilterChip(
                        selected = kind == null, onClick = { kind = null },
                        label = { Text("All") },
                    )
                }
                items(SoundKind.entries) { k ->
                    FilterChip(
                        selected = kind == k, onClick = { kind = k },
                        label = { Text(k.label) },
                    )
                }
            }
        }
        if (shown.isEmpty()) {
            Text(
                if (favoritesOnly) "Tap a heart to keep a sound here."
                else "Nothing here yet.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(24.dp),
            )
        }
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(shown, key = { it.resName }) { sound ->
                SoundRow(
                    sound = sound,
                    isPlaying = playing == sound.resName,
                    isFav = sound.resName in favs,
                    duration = SoundPlayer.duration(context, sound.resName),
                    onPlay = { SoundPlayer.toggle(context, sound.resName) },
                    onFav = { scope.launch { store.toggle(sound.resName) } },
                    onSet = { setTarget = sound },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }

    setTarget?.let { sound ->
        SetAsDialog(
            sound = sound,
            onPick = { slot ->
                setTarget = null
                if (RingtoneHelper.needsPermissionScreen(context)) {
                    RingtoneHelper.openPermissionScreen(context)
                    scope.launch { snacks.showSnackbar("Allow modifying system settings, then try again.") }
                } else {
                    val ok = RingtoneHelper.setAs(context, sound.resName, slot)
                    scope.launch {
                        snacks.showSnackbar(
                            if (ok) "“${sound.title}” is now your ${slot.label.lowercase()} sound."
                            else "Could not set it — try again.",
                        )
                    }
                }
            },
            onDismiss = { setTarget = null },
        )
    }
}

@Composable
private fun SoundRow(
    sound: Sound,
    isPlaying: Boolean,
    isFav: Boolean,
    duration: String,
    onPlay: () -> Unit,
    onFav: () -> Unit,
    onSet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // The one authored motion: the play button breathes on a spring.
    val punch by animateFloatAsState(
        targetValue = if (isPlaying) 1.18f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "play-punch",
    )
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp),
        ) {
            FloatingActionButton(
                onClick = onPlay,
                modifier = Modifier
                    .size(56.dp)
                    .scale(punch),
            ) {
                Icon(
                    if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Stop" else "Play",
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(sound.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${sound.kind.label} · $duration",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onFav) {
                Icon(
                    if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFav) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = onSet) { Text("Set") }
        }
    }
}

@Composable
private fun SetAsDialog(sound: Sound, onPick: (RingtoneSlot) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("“${sound.title}” as…") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                RingtoneSlot.entries.forEach { slot ->
                    TextButton(onClick = { onPick(slot) }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Check, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Default ${slot.label.lowercase()}")
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun AboutScreen(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Warm sounds for cold mornings.", style = MaterialTheme.typography.headlineSmall)
        Text(
            "24 hand-tuned tones: gentle alarms, dreamy ringtones, soft notifications. " +
                "Preview anything, keep favorites, and set any sound as your system " +
                "default — no account, no ads, fully offline.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Setting a default needs the “modify system settings” permission once — " +
                "Android asks for it the first time you tap Set.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
