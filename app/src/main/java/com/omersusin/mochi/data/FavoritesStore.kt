package com.omersusin.mochi.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.store by preferencesDataStore("mochi")
private val FAVS = stringSetPreferencesKey("favorites")

class FavoritesStore(private val context: Context) {
    val favorites: Flow<Set<String>> =
        context.store.data.map { it[FAVS] ?: emptySet() }

    suspend fun toggle(resName: String) {
        context.store.edit { prefs ->
            val cur = prefs[FAVS] ?: emptySet()
            prefs[FAVS] = if (resName in cur) cur - resName else cur + resName
        }
    }
}
