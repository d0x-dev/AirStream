package com.github.airstream.ui.models

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.airstream.api.MediaServiceRepository
import com.github.airstream.api.obj.StreamItem
import com.github.airstream.api.obj.Streams
import com.github.airstream.extensions.toID
import com.github.airstream.helpers.PreferenceHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class ShortsViewModel : ViewModel() {

    private val _shortsList = MutableLiveData<List<StreamItem>>(emptyList())
    val shortsList: LiveData<List<StreamItem>> = _shortsList

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isLoadingMore = MutableLiveData<Boolean>(false)
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore

    private val _isError = MutableLiveData<Boolean>(false)
    val isError: LiveData<Boolean> = _isError

    private val seenVideoIds = mutableSetOf<String>()
    private val allShorts = mutableListOf<StreamItem>()
    private val streamsCache = ConcurrentHashMap<String, Streams>()

    // Local likes and dislikes state tracking
    val likedShorts = mutableMapOf<String, Boolean>()
    val dislikedShorts = mutableMapOf<String, Boolean>()

    private val searchQueries = listOf(
        "#shorts",
        "#viral",
        "#shorts trending",
        "#reels",
        "#funny",
        "#gaming",
        "#tech",
        "#music",
        "#entertainment"
    )
    private var currentQueryIndex = 0
    private var nextPageToken: String? = null
    private var isFetching = false

    fun loadInitialShorts(context: Context, initialVideoId: String? = null) {
        if (allShorts.isNotEmpty() && initialVideoId == null) {
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _isError.value = false
            seenVideoIds.clear()
            allShorts.clear()

            try {
                // If initial video is provided, fetch its stream info and make it first
                if (!initialVideoId.isNullOrBlank()) {
                    val initialId = initialVideoId.toID()
                    seenVideoIds.add(initialId)
                    try {
                        val streamInfo = withContext(Dispatchers.IO) {
                            MediaServiceRepository.instance.getStreams(initialId)
                        }
                        streamsCache[initialId] = streamInfo
                        val item = streamInfo.toStreamItem(initialId)
                        allShorts.add(item)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // Fetch initial feed from trending or search
                val fetchedShorts = withContext(Dispatchers.IO) {
                    fetchShortsBatch(context)
                }

                allShorts.addAll(fetchedShorts)
                _shortsList.value = allShorts.toList()
                _isError.value = allShorts.isEmpty()
            } catch (e: Exception) {
                e.printStackTrace()
                _isError.value = allShorts.isEmpty()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMoreShorts(context: Context) {
        if (isFetching || _isLoadingMore.value == true) return
        isFetching = true
        _isLoadingMore.value = true

        viewModelScope.launch {
            try {
                val newShorts = withContext(Dispatchers.IO) {
                    fetchShortsBatch(context)
                }

                if (newShorts.isNotEmpty()) {
                    allShorts.addAll(newShorts)
                    _shortsList.value = allShorts.toList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isFetching = false
                _isLoadingMore.value = false
            }
        }
    }

    private suspend fun fetchShortsBatch(context: Context): List<StreamItem> {
        val result = mutableListOf<StreamItem>()
        val mediaRepo = MediaServiceRepository.instance

        // Strategy 1: If nextPageToken is available, fetch next page of current search
        if (!nextPageToken.isNullOrBlank()) {
            val query = searchQueries[currentQueryIndex % searchQueries.size]
            try {
                val searchResult = mediaRepo.getSearchResultsNextPage(query, "all", nextPageToken!!)
                nextPageToken = searchResult.nextpage
                val items = filterAndCollectShorts(searchResult.items)
                result.addAll(items)
            } catch (e: Exception) {
                nextPageToken = null
            }
        }

        // Strategy 2: If we still need items or no token, advance query and search
        if (result.size < 6) {
            val query = searchQueries[currentQueryIndex % searchQueries.size]
            currentQueryIndex++
            try {
                val searchResult = mediaRepo.getSearchResults(query, "all")
                nextPageToken = searchResult.nextpage
                val items = filterAndCollectShorts(searchResult.items)
                result.addAll(items)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Strategy 3: Try trending streams if result is still low
        if (result.size < 6) {
            try {
                val region = PreferenceHelper.getTrendingRegion(context)
                val trending = mediaRepo.getTrending(region, com.github.airstream.api.TrendingCategory.LIVE)
                val shortsFromTrending = filterAndCollectShorts(trending)
                result.addAll(shortsFromTrending)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return result
    }

    private fun filterAndCollectShorts(items: List<Any>): List<StreamItem> {
        val filtered = mutableListOf<StreamItem>()
        for (item in items) {
            val streamItem = when (item) {
                is StreamItem -> item
                is com.github.airstream.api.obj.ContentItem -> item.toStreamItem()
                else -> null
            } ?: continue

            val videoId = streamItem.url?.toID() ?: continue
            if (seenVideoIds.contains(videoId)) continue

            // Identify Shorts: either isShort, or duration <= 90s, or title contains #shorts
            val isShortVideo = streamItem.isShort ||
                    (streamItem.duration != null && streamItem.duration > 0 && streamItem.duration <= 90) ||
                    (streamItem.title?.contains("#shorts", ignoreCase = true) == true) ||
                    (streamItem.shortDescription?.contains("#shorts", ignoreCase = true) == true)

            if (isShortVideo) {
                seenVideoIds.add(videoId)
                filtered.add(streamItem)
            }
        }
        return filtered
    }

    suspend fun getStreamInfo(videoId: String): Streams? {
        val id = videoId.toID()
        streamsCache[id]?.let { return it }

        return try {
            val streams = withContext(Dispatchers.IO) {
                MediaServiceRepository.instance.getStreams(id)
            }
            streamsCache[id] = streams
            streams
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun toggleLike(videoId: String): Boolean {
        val id = videoId.toID()
        val currentlyLiked = likedShorts[id] == true
        if (currentlyLiked) {
            likedShorts[id] = false
        } else {
            likedShorts[id] = true
            dislikedShorts[id] = false
        }
        return likedShorts[id] == true
    }

    fun toggleDislike(videoId: String): Boolean {
        val id = videoId.toID()
        val currentlyDisliked = dislikedShorts[id] == true
        if (currentlyDisliked) {
            dislikedShorts[id] = false
        } else {
            dislikedShorts[id] = true
            likedShorts[id] = false
        }
        return dislikedShorts[id] == true
    }
}

