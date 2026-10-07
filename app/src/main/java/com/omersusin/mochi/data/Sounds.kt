package com.omersusin.mochi.data

enum class SoundKind(val label: String) {
    ALARM("Alarms"),
    RINGTONE("Ringtones"),
    NOTIFICATION("Notifications"),
    UI("Interface"),
}

data class Sound(val resName: String, val kind: SoundKind) {
    val title: String = resName
        .removePrefix(when {
            resName.startsWith("al_cheer_") -> "al_cheer_"
            resName.startsWith("al_gentle_") -> "al_gentle_"
            else -> resName.substringBefore("_", "") + "_"
        })
        .removeSuffix(".ogg")
        .split("_").drop(1)
        .joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
        .ifBlank { resName }
}

private fun kindOf(res: String) = when {
    res.startsWith("al_") -> SoundKind.ALARM
    res.startsWith("rg_") -> SoundKind.RINGTONE
    res.startsWith("nt_") -> SoundKind.NOTIFICATION
    else -> SoundKind.UI
}

val CATALOG: List<Sound> = listOf(
    "al_cheer_03_open_window", "al_cheer_04_garden_path",
    "al_gentle_01_first_light", "al_gentle_02_slow_kettle",
    "rg_01_morning_glass", "rg_02_wooden_steps", "rg_03_paper_crane",
    "rg_04_window_light", "rg_05_small_parade", "rg_06_rainy_kitchen",
    "nt_01_cork", "nt_02_pebble", "nt_03_bubble", "nt_04_doorbell",
    "nt_05_spoon", "nt_06_firefly", "nt_07_seed", "nt_08_dew",
    "ui_01_tick", "ui_02_tock", "ui_03_tap", "ui_04_confirm",
    "ui_05_back", "ui_06_toggle",
).map { Sound(it, kindOf(it)) }
