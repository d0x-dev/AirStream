package com.github.airstream.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.airstream.databinding.ItemHistoryCardBinding
import com.github.airstream.db.obj.WatchHistoryItem
import com.github.airstream.helpers.ImageHelper
import com.github.airstream.helpers.NavigationHelper
import com.github.airstream.parcelable.PlayerData
import com.github.airstream.ui.adapters.callbacks.DiffUtilItemCallback
import com.github.airstream.ui.base.BaseActivity
import com.github.airstream.ui.extensions.setFormattedDuration
import com.github.airstream.ui.extensions.setWatchProgressLength
import com.github.airstream.ui.sheets.VideoOptionsBottomSheet
import com.github.airstream.util.TextUtils
import com.github.airstream.constants.IntentData

class HorizontalHistoryAdapter :
    ListAdapter<WatchHistoryItem, HorizontalHistoryAdapter.ViewHolder>(DiffUtilItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        val activity = holder.itemView.context as BaseActivity
        holder.itemView.setOnClickListener {
            NavigationHelper.navigateVideo(activity, PlayerData(item.videoId))
        }

        holder.binding.moreOptions.setOnClickListener {
            val sheet = VideoOptionsBottomSheet()
            sheet.arguments = bundleOf(IntentData.streamItem to item.toStreamItem())
            sheet.show(activity.supportFragmentManager, "HorizontalHistoryAdapter")
        }

        ImageHelper.loadImage(item.thumbnailUrl, holder.binding.thumbnail)

        holder.binding.videoTitle.text = item.title
        holder.binding.videoUploader.text = item.uploader

        if (item.duration != null) {
            holder.binding.videoDuration.setFormattedDuration(item.duration, null, 0)
            holder.binding.thumbnailDurationCard.isVisible = true
        } else {
            holder.binding.thumbnailDurationCard.isGone = true
        }

        if (item.duration != null) {
            holder.binding.watchProgress.setWatchProgressLength(item.videoId, item.duration)
            holder.binding.watchProgress.isVisible = true
        } else {
            holder.binding.watchProgress.isGone = true
        }
    }

    inner class ViewHolder(val binding: ItemHistoryCardBinding) :
        RecyclerView.ViewHolder(binding.root)
}
