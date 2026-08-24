package com.github.airstream.ui.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.github.airstream.databinding.ChannelRowBinding
import com.github.airstream.databinding.PlaylistsRowBinding
import com.github.airstream.databinding.TrendingRowBinding
import com.github.airstream.databinding.VideoRowBinding

class SearchViewHolder : RecyclerView.ViewHolder {
    var videoRowBinding: VideoRowBinding? = null
    var trendingRowBinding: TrendingRowBinding? = null
    var channelRowBinding: ChannelRowBinding? = null
    var playlistRowBinding: PlaylistsRowBinding? = null

    constructor(binding: VideoRowBinding) : super(binding.root) {
        videoRowBinding = binding
    }

    constructor(binding: TrendingRowBinding) : super(binding.root) {
        trendingRowBinding = binding
    }

    constructor(binding: ChannelRowBinding) : super(binding.root) {
        channelRowBinding = binding
    }

    constructor(binding: PlaylistsRowBinding) : super(binding.root) {
        playlistRowBinding = binding
    }
}
