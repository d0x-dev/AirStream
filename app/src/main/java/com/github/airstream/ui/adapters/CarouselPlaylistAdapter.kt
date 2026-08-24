package com.github.airstream.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.ListAdapter
import com.github.airstream.api.PlaylistsHelper
import com.github.airstream.constants.IntentData
import com.github.airstream.databinding.CarouselPlaylistThumbnailBinding
import com.github.airstream.helpers.ImageHelper
import com.github.airstream.helpers.NavigationHelper
import com.github.airstream.ui.adapters.callbacks.DiffUtilItemCallback
import com.github.airstream.ui.base.BaseActivity
import com.github.airstream.ui.sheets.PlaylistOptionsBottomSheet
import com.github.airstream.ui.viewholders.CarouselPlaylistViewHolder

data class CarouselPlaylist(
    val id: String,
    val title: String?,
    val thumbnail: String?
)

class CarouselPlaylistAdapter : ListAdapter<CarouselPlaylist, CarouselPlaylistViewHolder>(
    DiffUtilItemCallback()
) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CarouselPlaylistViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return CarouselPlaylistViewHolder(CarouselPlaylistThumbnailBinding.inflate(layoutInflater))
    }

    override fun onBindViewHolder(
        holder: CarouselPlaylistViewHolder,
        position: Int
    ) {
        val item = getItem(position)!!

        with(holder.binding) {
            playlistName.text = item.title
            ImageHelper.loadImage(item.thumbnail, thumbnail)

            val type = PlaylistsHelper.getPlaylistType(item.id)
            root.setOnClickListener {
                NavigationHelper.navigatePlaylist(root.context, item.id, type)
            }

            root.setOnLongClickListener {
                val playlistOptionsDialog = PlaylistOptionsBottomSheet()
                playlistOptionsDialog.arguments = bundleOf(
                    IntentData.playlistId to item.id,
                    IntentData.playlistName to item.title,
                    IntentData.playlistType to type
                )
                playlistOptionsDialog.show((root.context as BaseActivity).supportFragmentManager)

                true
            }
        }
    }
}