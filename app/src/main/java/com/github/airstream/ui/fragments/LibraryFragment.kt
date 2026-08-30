package com.github.airstream.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.airstream.R
import com.github.airstream.api.PlaylistsHelper
import com.github.airstream.api.obj.Playlists
import com.github.airstream.constants.IntentData
import com.github.airstream.constants.PreferenceKeys
import com.github.airstream.databinding.FragmentLibraryBinding
import com.github.airstream.db.DatabaseHolder
import com.github.airstream.extensions.TAG
import com.github.airstream.extensions.ceilHalf
import com.github.airstream.extensions.dpToPx
import com.github.airstream.helpers.NavBarHelper
import com.github.airstream.helpers.PreferenceHelper
import com.github.airstream.ui.adapters.PlaylistBookmarkAdapter
import com.github.airstream.ui.adapters.PlaylistsAdapter
import com.github.airstream.ui.adapters.HorizontalHistoryAdapter
import com.github.airstream.ui.models.WatchHistoryModel
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.airstream.helpers.ImageHelper
import com.github.airstream.ui.base.DynamicLayoutManagerFragment
import com.github.airstream.ui.dialogs.CreatePlaylistDialog
import com.github.airstream.ui.dialogs.CreatePlaylistDialog.Companion.CREATE_PLAYLIST_DIALOG_REQUEST_KEY
import com.github.airstream.ui.models.CommonPlayerViewModel
import com.github.airstream.ui.sheets.BaseBottomSheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LibraryFragment : DynamicLayoutManagerFragment(R.layout.fragment_library) {
    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private val commonPlayerViewModel: CommonPlayerViewModel by activityViewModels()

    private val playlistsAdapter = PlaylistsAdapter(PlaylistsHelper.getPrivatePlaylistType())
    private val historyAdapter = HorizontalHistoryAdapter()
    private val historyViewModel: WatchHistoryModel by viewModels()
    private val playlistBookmarkAdapter = PlaylistBookmarkAdapter()

        override fun setLayoutManagers(gridItems: Int) {
        _binding?.bookmarksRecView?.layoutManager = GridLayoutManager(context, gridItems.ceilHalf())
        _binding?.playlistRecView?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        _binding?.historyRecView?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentLibraryBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

                binding.bookmarksRecView.adapter = playlistBookmarkAdapter
        binding.historyRecView.adapter = historyAdapter



        binding.viewChannelButton.setOnClickListener {
            val settingsIntent = android.content.Intent(context, com.github.airstream.ui.activities.SettingsActivity::class.java).apply {
                putExtra(com.github.airstream.ui.activities.SettingsActivity.REDIRECT_KEY, com.github.airstream.ui.activities.SettingsActivity.REDIRECT_TO_ACCOUNT_SETTINGS)
            }
            startActivity(settingsIntent)
        }
        binding.historyHeader.setOnClickListener {
            findNavController().navigate(R.id.action_libraryFragment_to_watchHistoryFragment)
        }

        historyViewModel.filteredWatchHistory.observe(viewLifecycleOwner) { history ->
            historyAdapter.submitList(history.take(15))
        }
        historyViewModel.fetchNextPage()

        // listen for playlists to become deleted
        playlistsAdapter.registerAdapterDataObserver(object :
            RecyclerView.AdapterDataObserver() {
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                _binding?.nothingHere?.isVisible = playlistsAdapter.itemCount == 0
                _binding?.sortTV?.isVisible = playlistsAdapter.itemCount > 0
                super.onItemRangeRemoved(positionStart, itemCount)
            }
        })
        binding.playlistRecView.adapter = playlistsAdapter

        // listen for the mini player state changing
        commonPlayerViewModel.isMiniPlayerVisible.observe(viewLifecycleOwner) {
            updateFABMargin(it)
        }



        binding.downloads.setOnClickListener {
            findNavController().navigate(R.id.action_libraryFragment_to_downloadsFragment)
        }

        val navBarItems = NavBarHelper.getNavBarItemPreference(requireContext())
        if (navBarItems.any { (itemId, isVisible) -> isVisible && itemId == R.id.downloadsFragment }) {
            binding.downloads.isGone = true
        }

        fetchPlaylists()
        initBookmarks()

        binding.playlistRefresh.isEnabled = true
        binding.playlistRefresh.setOnRefreshListener {
            fetchPlaylists()
            initBookmarks()
        }

        childFragmentManager.setFragmentResultListener(
            CREATE_PLAYLIST_DIALOG_REQUEST_KEY,
            this
        ) { _, resultBundle ->
            val isPlaylistCreated = resultBundle.getBoolean(IntentData.playlistTask)
            if (isPlaylistCreated) {
                fetchPlaylists()
            }
        }
        binding.createPlaylist.setOnClickListener {
            CreatePlaylistDialog()
                .show(childFragmentManager, CreatePlaylistDialog::class.java.name)
        }

        val sortOptions = resources.getStringArray(R.array.playlistSortingOptions)
        val sortOptionValues = resources.getStringArray(R.array.playlistSortingOptionsValues)
        val order = PreferenceHelper.getString(
            PreferenceKeys.PLAYLISTS_ORDER,
            sortOptionValues.first()
        )
        val orderIndex = sortOptionValues.indexOf(order)
        binding.sortTV.text = sortOptions.getOrNull(orderIndex)

        binding.sortTV.setOnClickListener {
            BaseBottomSheet().apply {
                setSimpleItems(sortOptions.toList()) { index ->
                    binding.sortTV.text = sortOptions[index]
                    val value = sortOptionValues[index]
                    PreferenceHelper.putString(PreferenceKeys.PLAYLISTS_ORDER, value)
                    fetchPlaylists()
                }
            }.show(childFragmentManager)
        }
    }

    override fun onResume() {
        super.onResume()
        updateProfileUi()
    }

    private fun updateProfileUi() {
        // Load profile data
        val name = PreferenceHelper.getString("yt_name", "")
        val email = PreferenceHelper.getString("yt_email", "")
        val avatar = PreferenceHelper.getString("yt_avatar", "")
        
        _binding?.let { binding ->
            if (name.isNotBlank()) {
                binding.profileName.text = name
                binding.profileUsername.text = email.takeIf { it.isNotBlank() } ?: "Logged in"
                binding.profilePremium.isVisible = true
                if (avatar.isNotBlank()) {
                    ImageHelper.loadImage(avatar, binding.profileAvatar)
                } else {
                    binding.profileAvatar.setImageResource(R.drawable.ic_person)
                }
            } else if (com.darkxvenom.airbeats.innertube.YouTube.cookie != null) {
                // Logged in but name not saved yet. Fetch it!
                binding.profileName.text = "Loading..."
                binding.profileUsername.text = "Fetching account info..."
                binding.profilePremium.isVisible = false
                lifecycleScope.launch {
                    val accountInfo = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { com.darkxvenom.airbeats.innertube.YouTube.accountInfo().getOrNull() }
                    if (accountInfo != null) {
                        val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext())
                        prefs.edit()
                            .putString("yt_name", accountInfo.name)
                            .putString("yt_email", accountInfo.email ?: "")
                            .putString("yt_avatar", accountInfo.thumbnailUrl ?: "")
                            .apply()
                        updateProfileUi()
                    } else {
                        binding.profileName.text = "Logged in"
                        binding.profileUsername.text = "Failed to load info"
                    }
                }
            } else {
                binding.profileName.text = "Guest"
                binding.profileUsername.text = getString(R.string.login_to_youtube)
                binding.profilePremium.isVisible = false
                binding.profileAvatar.setImageResource(R.drawable.ic_person)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initBookmarks() {
        lifecycleScope.launch {
            val bookmarks = withContext(Dispatchers.IO) {
                DatabaseHolder.Database.playlistBookmarkDao().getAll()
            }

            val binding = _binding ?: return@launch

            binding.bookmarksContainer.isVisible = bookmarks.isNotEmpty()
            if (bookmarks.isNotEmpty()) {
                playlistBookmarkAdapter.submitList(bookmarks)
            }
        }
    }

    private fun updateFABMargin(isMiniPlayerVisible: Boolean) {
        val isPill = PreferenceHelper.getBoolean(PreferenceKeys.PILL_SHAPED_NAV_BAR, false)
        // If pill is enabled, we need to push the FAB up so it doesn't get hidden behind the floating nav bar.
        val pillOffset = if (isPill) 92f else 0f

        // optimize CreatePlaylistFab bottom margin if miniPlayer active
        binding.createPlaylist.updateLayoutParams<MarginLayoutParams> {
            bottomMargin = (if (isMiniPlayerVisible) 64f + pillOffset else 16f + pillOffset).dpToPx()
        }
    }

    private fun fetchPlaylists() {
        _binding?.playlistRefresh?.isRefreshing = true
        lifecycleScope.launch {
            val playlists = try {
                withContext(Dispatchers.IO) {
                    PlaylistsHelper.getPlaylists()
                }
            } catch (e: Exception) {
                Log.e(TAG(), e.toString())
                Toast.makeText(context, R.string.unknown_error, Toast.LENGTH_SHORT).show()
                return@launch
            }

            val binding = _binding ?: return@launch
            binding.playlistRefresh.isRefreshing = false

            // also update playlists recycler when the playlists are empty in order to remove
            // playlists that were removed by the user
            showPlaylists(playlists)
            if (playlists.isEmpty()) {
                binding.sortTV.isVisible = false
                binding.nothingHere.isVisible = true
            }
        }
    }

    private fun showPlaylists(playlists: List<Playlists>) {
        val binding = _binding ?: return

        binding.nothingHere.isGone = true
        binding.sortTV.isVisible = true
        playlistsAdapter.submitList(playlists)
    }
}





