package com.github.airstream.repo

import com.github.airstream.api.obj.StreamItem
import com.github.airstream.db.obj.SubscriptionsFeedItem

data class FeedProgress(
    val currentProgress: Int,
    val total: Int
)

interface FeedRepository {
    suspend fun getFeed(
        forceRefresh: Boolean,
        onProgressUpdate: (FeedProgress) -> Unit
    ): List<StreamItem>
    suspend fun removeChannel(channelId: String) {}
    suspend fun submitFeedItemChange(feedItem: SubscriptionsFeedItem) {}
}