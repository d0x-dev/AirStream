package com.github.airstream.util

import android.content.Context
import android.widget.Toast
import com.darkxvenom.airbeats.innertube.YouTube
import com.darkxvenom.airbeats.innertube.models.ArtistItem
import com.darkxvenom.airbeats.innertube.models.PlaylistItem
import com.darkxvenom.airbeats.innertube.models.SongItem
import com.darkxvenom.airbeats.innertube.utils.completedLibraryPage
import com.github.airstream.api.obj.StreamItem
import com.github.airstream.db.DatabaseHolder
import com.github.airstream.db.obj.LocalSubscription
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object YouTubeSyncManager {

    suspend fun syncData(context: Context, cookie: String) = withContext(Dispatchers.IO) {
        try {
            YouTube.cookie = cookie

            // 1. Sync Subscriptions (Artists/Channels from Music Library)
            val artistsPage = YouTube.library("FEmusic_library_corpus_artists").completedLibraryPage().getOrNull()
            if (artistsPage != null) {
                val artists = artistsPage.items.filterIsInstance<ArtistItem>()
                val localSubscriptionsDao = DatabaseHolder.Database.localSubscriptionDao()
                
                artists.forEach { artist ->
                    val dbSubExists = localSubscriptionsDao.includes(artist.id)
                    if (!dbSubExists) {
                        localSubscriptionsDao.insert(
                            LocalSubscription(
                                channelId = artist.id,
                                name = artist.title,
                                avatar = artist.thumbnail,
                                verified = false
                            )
                        )
                    }
                }
            }

            // 2. Sync Playlists
            val playlistsPage = YouTube.library("FEmusic_liked_playlists").completedLibraryPage().getOrNull()
            if (playlistsPage != null) {
                val playlists = playlistsPage.items.filterIsInstance<PlaylistItem>()
                val localPlaylistsDao = DatabaseHolder.Database.localPlaylistsDao()
                val playlistRepo = com.github.airstream.repo.LocalPlaylistsRepository()
                
                for (playlist in playlists) {
                    if (playlist.id != "LM") {
                        val existing = localPlaylistsDao.getAll().find { it.playlist.name == playlist.title }
                        if (existing == null) {
                            val playlistId = playlistRepo.createPlaylist(playlist.title)
                            val pPage = YouTube.playlist(playlist.id).getOrNull()
                            if (pPage != null) {
                                val songs = pPage.songs
                                val streams = songs.map { song ->
                                    StreamItem(
                                        url = "/watch?v=${song.id}",
                                        title = song.title,
                                        thumbnail = song.thumbnail,
                                        uploaderName = song.artists.firstOrNull()?.name ?: "",
                                        uploaderUrl = "/channel/${song.artists.firstOrNull()?.id ?: ""}",
                                        uploaderAvatar = "",
                                        uploadedDate = "",
                                        shortDescription = "",
                                        duration = 0,
                                        views = 0,
                                        uploaded = 0,
                                        uploaderVerified = false,
                                        isShort = false
                                    )
                                }
                                val streamsArray = streams.toTypedArray()
                                if (streamsArray.isNotEmpty()) {
                                    playlistRepo.addToPlaylist(playlistId, *streamsArray)
                                }
                            }
                        }
                    }
                }
            }

            // 3. Sync Liked Videos (from LM playlist)
            val lmPage = YouTube.playlist("LM").getOrNull()
            if (lmPage != null) {
                val songs = lmPage.songs
                val existing = DatabaseHolder.Database.localPlaylistsDao().getAll().find { it.playlist.name == "Liked Videos" }
                if (existing == null) {
                    val playlistRepo = com.github.airstream.repo.LocalPlaylistsRepository()
                    val playlistId = playlistRepo.createPlaylist("Liked Videos")
                    val streams = songs.map { song ->
                        StreamItem(
                            url = "/watch?v=${song.id}",
                            title = song.title,
                            thumbnail = song.thumbnail,
                            uploaderName = song.artists.firstOrNull()?.name ?: "",
                            uploaderUrl = "/channel/${song.artists.firstOrNull()?.id ?: ""}",
                            uploaderAvatar = "",
                            uploadedDate = "",
                            shortDescription = "",
                            duration = 0,
                            views = 0,
                            uploaded = 0,
                            uploaderVerified = false,
                            isShort = false
                        )
                    }
                    val streamsArray = streams.toTypedArray()
                    if (streamsArray.isNotEmpty()) {
                        playlistRepo.addToPlaylist(playlistId, *streamsArray)
                    }
                }
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "YouTube sync complete!", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "YouTube sync failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
