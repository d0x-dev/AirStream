package com.github.airstream.repo

import com.github.airstream.api.RetrofitInstance
import com.github.airstream.api.obj.StreamItem
import com.github.airstream.helpers.PreferenceHelper

class PipedAccountFeedRepository : FeedRepository {
    override suspend fun getFeed(
        forceRefresh: Boolean,
        onProgressUpdate: (FeedProgress) -> Unit
    ): List<StreamItem> {
        val token = PreferenceHelper.getToken()

        return RetrofitInstance.authApi.getFeed(token)
    }
}