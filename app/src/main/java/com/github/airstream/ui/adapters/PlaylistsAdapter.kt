package com.github.airstream.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.ListAdapter
import com.github.airstream.R
import com.github.airstream.api.obj.Playlists
import com.github.airstream.constants.IntentData
import com.github.airstream.databinding.PlaylistsRowBinding
import com.github.airstream.enums.PlaylistType
import com.github.airstream.helpers.ImageHelper
import com.github.airstream.helpers.NavigationHelper
import com.github.airstream.ui.adapters.callbacks.DiffUtilItemCallback
import com.github.airstream.ui.base.BaseActivity
import com.github.airstream.ui.sheets.PlaylistOptionsBottomSheet
import com.github.airstream.ui.sheets.PlaylistOptionsBottomSheet.Companion.PLAYLIST_OPTIONS_REQUEST_KEY
import com.github.airstream.ui.viewholders.PlaylistsViewHolder

class PlaylistsAdapter(
    private val playlistType: PlaylistType
) : ListAdapter<Playlists, PlaylistsViewHolder>(
    DiffUtilItemCallback(areItemsTheSame = { oldItem, newItem -> oldItem.id == newItem.id })
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = PlaylistsRowBinding.inflate(layoutInflater, parent, false)
        return PlaylistsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistsViewHolder, position: Int) {
        val playlist = getItem(holder.bindingAdapterPosition)
        holder.binding.apply {
            // set imageview drawable as empty playlist if imageview empty
            if (playlist.thumbnail.isNullOrEmpty()) {
                playlistThumbnail.setImageResource(R.drawable.ic_empty_playlist)
                playlistThumbnail
                    .setBackgroundColor(com.google.android.material.R.attr.colorSurface)
            } else {
                ImageHelper.loadImage(playlist.thumbnail, playlistThumbnail)
            }
            playlistTitle.text = playlist.name
            playlistDescription.text = playlist.shortDescription

            videoCount.text = playlist.videos.toString()

            root.setOnClickListener {
                NavigationHelper.navigatePlaylist(root.context, playlist.id, playlistType)
            }

            val fragmentManager = (root.context as BaseActivity).supportFragmentManager
            optionsMenu.setOnClickListener {
                fragmentManager.setFragmentResultListener(
                    PLAYLIST_OPTIONS_REQUEST_KEY,
                    (root.context as BaseActivity)
                ) { _, resultBundle ->
                    val newPlaylistDescription =
                        resultBundle.getString(IntentData.playlistDescription)
                    val newPlaylistName =
                        resultBundle.getString(IntentData.playlistName)
                    val isPlaylistToBeDeleted =
                        resultBundle.getBoolean(IntentData.playlistTask)

                    newPlaylistDescription?.let {
                        playlistDescription.text = it
                        playlist.shortDescription = it
                    }

                    newPlaylistName?.let {
                        playlistTitle.text = it
                        playlist.name = it
                    }

                    if (isPlaylistToBeDeleted) {
                        // try to refresh the playlists in the library on deletion success
                        onDelete(position)
                    }
                }

                val playlistOptionsDialog = PlaylistOptionsBottomSheet()
                playlistOptionsDialog.arguments = bundleOf(
                    IntentData.playlistId to playlist.id!!,
                    IntentData.playlistName to playlist.name!!,
                    IntentData.playlistType to playlistType
                )
                playlistOptionsDialog.show(
                    fragmentManager,
                    PlaylistOptionsBottomSheet::class.java.name
                )
            }
        }
    }

    private fun onDelete(position: Int) {
        val newList = currentList.toMutableList().also {
            it.removeAt(position)
        }
        submitList(newList)
    }
}
