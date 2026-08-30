package com.github.airstream.ui.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.github.airstream.databinding.AllCaughtUpRowBinding
import com.github.airstream.databinding.TrendingRowBinding
import com.github.airstream.databinding.ShortsShelfRowBinding

class VideoCardsViewHolder : RecyclerView.ViewHolder {
    var trendingRowBinding: TrendingRowBinding? = null
    var allCaughtUpBinding: AllCaughtUpRowBinding? = null
    var shortsShelfBinding: ShortsShelfRowBinding? = null

    constructor(binding: TrendingRowBinding) : super(binding.root) {
        trendingRowBinding = binding
    }

    constructor(binding: AllCaughtUpRowBinding) : super(binding.root) {
        allCaughtUpBinding = binding
    }
    
    constructor(binding: ShortsShelfRowBinding) : super(binding.root) {
        shortsShelfBinding = binding
    }
}
