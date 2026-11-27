package com.example.suararasa

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.suararasa.data.MusicCategory
import com.example.suararasa.data.PlaylistDataStore
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.collections.filterNot
import kotlin.collections.map
import kotlin.collections.plus

class PlaylistViewModel(private val dataStore: PlaylistDataStore) : ViewModel() {
    var playlists by mutableStateOf<List<MusicCategory>>(emptyList())
        private set

    init {
        viewModelScope.launch {
            dataStore.playlistsFlow.collect { loadedPlaylists ->
                playlists = loadedPlaylists.map { MusicCategory.fromSerialized(it) }
            }
        }
    }

    fun addPlaylist(playlist: MusicCategory) {
        viewModelScope.launch {
            val newPlaylist = playlist.copy(id = UUID.randomUUID().toString())
            dataStore.savePlaylists(playlists + newPlaylist)
        }
    }

    fun deletePlaylist(id: String) {
        viewModelScope.launch {
            dataStore.savePlaylists(playlists.filterNot { it.id == id })
        }
    }

    fun updatePlaylist(updated: MusicCategory) {
        viewModelScope.launch {
            dataStore.savePlaylists(
                playlists.map { if (it.id == updated.id) updated else it }
            )
        }
    }
}