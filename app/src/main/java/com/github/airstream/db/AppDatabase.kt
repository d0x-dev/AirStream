package com.github.airstream.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.airstream.db.dao.CustomInstanceDao
import com.github.airstream.db.dao.DownloadDao
import com.github.airstream.db.dao.LocalPlaylistsDao
import com.github.airstream.db.dao.LocalSubscriptionDao
import com.github.airstream.db.dao.PlaylistBookmarkDao
import com.github.airstream.db.dao.SearchHistoryDao
import com.github.airstream.db.dao.SubscriptionGroupsDao
import com.github.airstream.db.dao.SubscriptionsFeedDao
import com.github.airstream.db.dao.WatchHistoryDao
import com.github.airstream.db.dao.WatchPositionDao
import com.github.airstream.db.obj.CustomInstance
import com.github.airstream.db.obj.Download
import com.github.airstream.db.obj.DownloadChapter
import com.github.airstream.db.obj.DownloadItem
import com.github.airstream.db.obj.DownloadPlaylist
import com.github.airstream.db.obj.DownloadPlaylistVideosCrossRef
import com.github.airstream.db.obj.DownloadSponsorBlockSegment
import com.github.airstream.db.obj.LocalPlaylist
import com.github.airstream.db.obj.LocalPlaylistItem
import com.github.airstream.db.obj.LocalSubscription
import com.github.airstream.db.obj.PlaylistBookmark
import com.github.airstream.db.obj.SearchHistoryItem
import com.github.airstream.db.obj.SubscriptionGroup
import com.github.airstream.db.obj.SubscriptionsFeedItem
import com.github.airstream.db.obj.WatchHistoryItem
import com.github.airstream.db.obj.WatchPosition

@Database(
    entities = [
        WatchHistoryItem::class,
        WatchPosition::class,
        SearchHistoryItem::class,
        CustomInstance::class,
        LocalSubscription::class,
        PlaylistBookmark::class,
        LocalPlaylist::class,
        LocalPlaylistItem::class,
        Download::class,
        DownloadItem::class,
        DownloadChapter::class,
        DownloadSponsorBlockSegment::class,
        DownloadPlaylist::class,
        DownloadPlaylistVideosCrossRef::class,
        SubscriptionGroup::class,
        SubscriptionsFeedItem::class
    ],
    version = 25,
    autoMigrations = [
        AutoMigration(from = 7, to = 8),
        AutoMigration(from = 8, to = 9),
        AutoMigration(from = 9, to = 10),
        AutoMigration(from = 10, to = 11),
        AutoMigration(from = 16, to = 17),
        AutoMigration(from = 18, to = 19),
        AutoMigration(from = 19, to = 20),
        AutoMigration(from = 20, to = 21),
        AutoMigration(from = 23, to = 24, spec = DatabaseHolder.MIGRATION_23_24::class),
        AutoMigration(from = 24, to = 25),
    ]
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    /**
     * Watch History
     */
    abstract fun watchHistoryDao(): WatchHistoryDao

    /**
     * Watch Positions
     */
    abstract fun watchPositionDao(): WatchPositionDao

    /**
     * Search History
     */
    abstract fun searchHistoryDao(): SearchHistoryDao

    /**
     * Custom Instances
     */
    abstract fun customInstanceDao(): CustomInstanceDao

    /**
     * Local Subscriptions
     */
    abstract fun localSubscriptionDao(): LocalSubscriptionDao

    /**
     * Bookmarked Playlists
     */
    abstract fun playlistBookmarkDao(): PlaylistBookmarkDao

    /**
     * Local playlists
     */
    abstract fun localPlaylistsDao(): LocalPlaylistsDao

    /**
     * Downloads
     */
    abstract fun downloadDao(): DownloadDao

    /**
     * Subscription groups
     */
    abstract fun subscriptionGroupsDao(): SubscriptionGroupsDao

    /**
     * Locally cached subscription feed
     */
    abstract fun feedDao(): SubscriptionsFeedDao
}
