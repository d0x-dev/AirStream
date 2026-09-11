package com.github.airstream.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.airstream.R
import com.github.airstream.api.TrendingCategory
import com.github.airstream.api.obj.StreamItem
import com.github.airstream.databinding.FragmentHomeBinding
import com.github.airstream.ui.activities.SettingsActivity
import com.github.airstream.ui.adapters.VideoCardsAdapter
import com.github.airstream.ui.models.HomeViewModel
import com.github.airstream.ui.models.SubscriptionsViewModel
import com.github.airstream.ui.models.TrendsViewModel
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment(R.layout.fragment_home) {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by activityViewModels()
    private val subscriptionsViewModel: SubscriptionsViewModel by activityViewModels()

    private val feedAdapter = VideoCardsAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentHomeBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        binding.feedRV.adapter = feedAdapter

        with(homeViewModel) {
            trending.observe(viewLifecycleOwner, ::showTrending)
            isLoading.observe(viewLifecycleOwner, ::updateLoading)
            loadingMore.observe(viewLifecycleOwner) { isL -> binding.loadMoreProgress.isVisible = isL }
        }

        binding.refresh.setOnRefreshListener {
            binding.refresh.isRefreshing = true
            fetchHomeFeed()
        }

        binding.feedRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                // Load more if we are within 5 items of the end
                if (totalItemCount > 0 && lastVisibleItem >= totalItemCount - 5) {
                    homeViewModel.loadMoreRecommendations(requireContext())
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()

        if (homeViewModel.loadedSuccessfully.value == false) {
            fetchHomeFeed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchHomeFeed() {
        binding.nothingHere.isGone = true
        // Recommendations are the single, predictable home feed. Keeping this explicit
        // avoids launching unused data requests for sections that are not rendered here.
        val visibleItems = setOf("trending")

        homeViewModel.loadHomeFeed(
            context = requireContext(),
            subscriptionsViewModel = subscriptionsViewModel,
            visibleItems = visibleItems,
            onUnusualLoadTime = ::showChangeInstanceSnackBar
        )
    }

    private fun showTrending(trends: Pair<TrendingCategory, TrendsViewModel.TrendingStreams>?) {
        if (trends == null) return
        val (_, trendingStreams) = trends

        if (trendingStreams.streams.isNotEmpty()) {
            binding.feedRV.isVisible = true
            binding.nothingHere.isGone = true
            feedAdapter.submitList(trendingStreams.streams)
        } else {
            binding.feedRV.isGone = true
            binding.nothingHere.isVisible = true
            feedAdapter.submitList(emptyList())
        }
    }

    private fun updateLoading(isLoading: Boolean) {
        if (isLoading) {
            showLoading()
        } else {
            hideLoading()
        }
    }

    private fun showLoading() {
        binding.progress.isVisible = !binding.refresh.isRefreshing
        binding.nothingHere.isVisible = false
        binding.feedRV.alpha = 0.3f
    }

    private fun hideLoading() {
        binding.progress.isVisible = false
        binding.refresh.isRefreshing = false

        val hasContent = homeViewModel.loadedSuccessfully.value == true
        if (hasContent) {
            showContent()
        } else {
            showNothingHere()
        }
        binding.feedRV.alpha = 1.0f
    }

    private fun showNothingHere() {
        binding.nothingHere.isVisible = true
        binding.feedRV.isVisible = false
    }

    private fun showContent() {
        binding.nothingHere.isVisible = false
        binding.feedRV.isVisible = true
    }

    private fun showChangeInstanceSnackBar() {
        val root = _binding?.root ?: return
        Snackbar
            .make(root, R.string.suggest_change_instance, Snackbar.LENGTH_LONG)
            .apply {
                setAction(R.string.change) {
                    redirectToIntentSettings()
                }
                show()
            }
    }

    private fun redirectToIntentSettings() {
        val settingsIntent = Intent(context, SettingsActivity::class.java).apply {
            putExtra(SettingsActivity.REDIRECT_KEY, SettingsActivity.REDIRECT_TO_INTENT_SETTINGS)
        }
        startActivity(settingsIntent)
    }
}
