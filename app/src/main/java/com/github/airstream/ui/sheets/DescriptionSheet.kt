package com.github.airstream.ui.sheets

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.text.parseAsHtml
import androidx.core.view.isGone
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.airstream.R
import com.github.airstream.api.obj.Streams
import com.github.airstream.databinding.DescriptionSheetBinding
import com.github.airstream.ui.activities.VideoTagsAdapter
import com.github.airstream.ui.models.CommonPlayerViewModel
import com.github.airstream.util.TextUtils
import com.github.airstream.extensions.formatShort
import kotlinx.serialization.json.Json
import android.graphics.Color
import android.content.res.ColorStateList
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.graphics.Bitmap
import com.github.airstream.helpers.ImageHelper

class DescriptionSheet : ExpandablePlayerSheet(R.layout.description_sheet) {
    private var _binding: DescriptionSheetBinding? = null
    private val binding get() = _binding!!

    private val commonPlayerViewModel: CommonPlayerViewModel by activityViewModels()
    private val videoTagsAdapter = VideoTagsAdapter()

    override fun getSheetMaxHeightPx(): Int = commonPlayerViewModel.maxSheetHeightPx
    override fun getDragHandle(): View = binding.dragHandle
    override fun getBottomSheet(): FrameLayout = binding.standardBottomSheet

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = DescriptionSheetBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.apply {
            setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }

        binding.btnClose.setOnClickListener {
            dismiss()
        }

        binding.standardBottomSheet.backgroundTintList = ColorStateList.valueOf(Color.BLACK)

        binding.tagsRecycler.adapter = videoTagsAdapter
        binding.tagsRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val streamsJson = arguments?.getString(ARG_STREAMS) ?: return
        try {
            val streams = Json.decodeFromString(Streams.serializer(), streamsJson)
            populate(streams)
        } catch (e: Exception) {
            e.printStackTrace()
            dismiss()
        }
    }

    private fun populate(streams: Streams) {
        binding.videoTitle.text = streams.title
        
        // Likes
        val likes = streams.likes
        if (likes >= 0) {
            binding.txtLikesCount.text = likes.formatShort()
        } else {
            binding.txtLikesCount.text = "N/A"
        }

        // Views
        binding.txtViewsCount.text = "%,d".format(streams.views)

        // Date
        val uploadTimestamp = streams.uploadTimestamp
        if (uploadTimestamp != null) {
            val fullDate = TextUtils.localizeInstant(uploadTimestamp)
            val parts = fullDate.split(" ")
            if (parts.size >= 3) { // e.g. "Dec 11, 2024"
                binding.txtDateYear.text = parts.last()
                binding.txtDateMonth.text = parts.dropLast(1).joinToString(" ").replace(",", "")
            } else {
                binding.txtDateYear.text = fullDate
                binding.txtDateMonth.text = ""
            }
        } else {
            binding.txtDateYear.text = "N/A"
            binding.txtDateMonth.text = ""
        }

        // Tags
        if (streams.tags.isNotEmpty()) {
            videoTagsAdapter.submitList(streams.tags)
            binding.tagsRecycler.isGone = false
        } else {
            binding.tagsRecycler.isGone = true
        }

        // Description
        setupDescription(streams.description)

        // Meta Info
        if (streams.metaInfo.isNotEmpty()) {
            binding.additionalInfo.isGone = false
            val metaInfoText = streams.metaInfo.joinToString("\n\n") { info ->
                val text = info.description.takeIf { it.isNotBlank() } ?: info.title
                val links = info.urls.mapIndexed { index, url ->
                    "<a href=\"$url\">${info.urlTexts.getOrNull(index).orEmpty()}</a>"
                }
                text + "\n" + links.joinToString("\n")
            }
            binding.additionalInfo.text = metaInfoText.parseAsHtml()
        } else {
            binding.additionalInfo.isGone = true
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val bitmap = ImageHelper.getImage(requireContext(), streams.thumbnailUrl)
            if (bitmap != null) {
                val scaled = Bitmap.createScaledBitmap(bitmap, 1, 1, false)
                val avgColor = scaled.getPixel(0, 0)
                scaled.recycle()

                val darkTint = ColorUtils.blendARGB(Color.BLACK, avgColor, 0.20f)
                val tintList = ColorStateList.valueOf(darkTint)

                withContext(Dispatchers.Main) {
                    binding.cardLikes.setCardBackgroundColor(tintList)
                    binding.cardViews.setCardBackgroundColor(tintList)
                    binding.cardDate.setCardBackgroundColor(tintList)
                    binding.cardDescription.setCardBackgroundColor(tintList)
                }
            }
        }
    }
    private fun setupDescription(description: String) {
        val descTextView = binding.txtDescription
        // detect whether the description is html formatted
        if (description.contains("<") && description.contains(">")) {
            descTextView.movementMethod = androidx.core.text.method.LinkMovementMethodCompat.getInstance()
            descTextView.text = description.replace("</a>", "</a> ")
                .parseAsHtml(tagHandler = com.github.airstream.util.HtmlParser(com.github.airstream.util.LinkHandler(::handleLink)))
        } else {
            // Links can be present as plain text
            descTextView.autoLinkMask = android.text.util.Linkify.WEB_URLS
            descTextView.text = description
        }
    }

    private fun handleLink(link: String) {
        requireActivity().supportFragmentManager.setFragmentResult(
            com.github.airstream.ui.sheets.CommentsSheet.HANDLE_LINK_REQUEST_KEY,
            androidx.core.os.bundleOf(com.github.airstream.constants.IntentData.url to link)
        )
        dismiss() // close sheet when clicking a link
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_STREAMS = "streams_json"
    }
}
