package com.github.airstream.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.text.util.Linkify
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.text.method.LinkMovementMethodCompat
import androidx.core.text.parseAsHtml
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.github.airstream.R
import com.github.airstream.api.SponsorBlockLabelHelper
import com.github.airstream.api.obj.Segment
import com.github.airstream.api.obj.Streams
import com.github.airstream.databinding.DescriptionLayoutBinding
import com.github.airstream.enums.SbSkipOptions
import com.github.airstream.extensions.formatShort
import com.github.airstream.helpers.ClipboardHelper
import com.github.airstream.helpers.PlayerHelper
import com.github.airstream.ui.activities.VideoTagsAdapter
import com.github.airstream.util.HtmlParser
import com.github.airstream.util.LinkHandler
import com.github.airstream.util.TextUtils
import java.util.Locale

class DescriptionLayout(
    context: Context,
    attributeSet: AttributeSet?
) : LinearLayout(context, attributeSet) {
    val binding = DescriptionLayoutBinding.inflate(LayoutInflater.from(context), this, true)
    private var streams: Streams? = null
    var handleLink: (link: String) -> Unit = {}

    private val videoTagsAdapter = VideoTagsAdapter()

    init {
        binding.root.setOnClickListener {
            toggleDescription()
        }
        binding.root.setOnLongClickListener {
            streams?.title?.let { ClipboardHelper.save(context, text = it) }
            true
        }
        
        binding.tagsRecycler.adapter = videoTagsAdapter
    }

    fun setSegments(segments: List<Segment>) {
        if (PlayerHelper.getSponsorBlockCategories()[SB_SPONSOR_CATEGORY] == SbSkipOptions.OFF) {
           return
        }

        val category = segments.filter { it.actionType == Segment.TYPE_FULL }.firstNotNullOfOrNull { it.category }
        binding.playerSponsorBadge.isVisible = category != null
        binding.playerSponsorBadge.chipIcon = SponsorBlockLabelHelper.categoryIcon(category)?.let { context.getDrawable(it) }
        binding.playerSponsorBadge.text = SponsorBlockLabelHelper.categoryLabel(category)?.let { context.getString(it) }
    }

    @SuppressLint("SetTextI18n")
    fun setStreams(streams: Streams) {
        this.streams = streams

        val views = streams.views.formatShort()
        val date = TextUtils.formatRelativeDate(streams.uploaded ?: -1L)
        binding.run {
            val tagsString = streams.tags.take(3).joinToString(" ") { if (it.startsWith("#")) it else "#$it" }
            val uploader = streams.uploader ?: "Unknown"
            playerViewsInfo.text = "@$uploader • $views views • $date $tagsString ...more"

            textLike.isVisible = false
            textDislike.isVisible = false

            playerTitle.text = streams.title
            playerDescription.isVisible = false
            metaInfo.isVisible = false
            additionalVideoInfo.isVisible = false
            binding.tagsRecycler.isVisible = false
        }
    }

    var onDescriptionClicked: (() -> Unit)? = null

    private fun setupDescription(description: String) {
        // Ignored since description is hidden here
    }

    private fun toggleDescription() {
        onDescriptionClicked?.invoke()
    }

    companion object {
        private const val ANIMATION_DURATION = 250L
        private const val SB_SPONSOR_CATEGORY = "sponsor_category"
    }
}