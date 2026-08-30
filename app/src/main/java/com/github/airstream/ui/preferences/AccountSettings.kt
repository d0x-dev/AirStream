package com.github.airstream.ui.preferences

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.darkxvenom.airbeats.innertube.YouTube
import com.github.airstream.R
import com.github.airstream.ui.base.BasePreferenceFragment
import com.github.airstream.ui.dialogs.YouTubeLoginDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AccountSettings : BasePreferenceFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.account_settings, rootKey)

        val youtubeLogin = findPreference<Preference>("youtube_login")
        val useLoginForBrowsing = findPreference<SwitchPreferenceCompat>("yt_use_login_for_browsing")
        val autoSync = findPreference<SwitchPreferenceCompat>("yt_auto_sync")
        val ytLogout = findPreference<Preference>("yt_logout")

        useLoginForBrowsing?.setOnPreferenceChangeListener { _, newValue ->
            YouTube.useLoginForBrowse = newValue as Boolean
            true
        }

        youtubeLogin?.setOnPreferenceClickListener {
            YouTubeLoginDialog().show(childFragmentManager, YouTubeLoginDialog::class.java.name)
            true
        }

        ytLogout?.setOnPreferenceClickListener {
            val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext())
            prefs.edit().remove("yt_cookie").remove("yt_visitor_data")
                .remove("yt_name").remove("yt_email").remove("yt_avatar").apply()
            YouTube.cookie = null
            YouTube.visitorData = null
            updateUi()
            true
        }
        
        updateUi()
    }

    private fun updateUi() {
        val youtubeLogin = findPreference<Preference>("youtube_login")
        val useLoginForBrowsing = findPreference<SwitchPreferenceCompat>("yt_use_login_for_browsing")
        val autoSync = findPreference<SwitchPreferenceCompat>("yt_auto_sync")
        val ytLogout = findPreference<Preference>("yt_logout")

        val isLoggedIn = YouTube.cookie != null
        if (isLoggedIn) {
            youtubeLogin?.title = "Loading account info..."
            youtubeLogin?.summary = "Logged in"
            youtubeLogin?.setOnPreferenceClickListener(null)
            
            useLoginForBrowsing?.isVisible = true
            autoSync?.isVisible = true
            ytLogout?.isVisible = true

            lifecycleScope.launch {
                val accountInfoResult = withContext(Dispatchers.IO) {
                    YouTube.accountInfo()
                }
                val accountInfo = accountInfoResult.getOrNull()
                if (accountInfo != null) {
                    youtubeLogin?.title = accountInfo.name
                    youtubeLogin?.summary = accountInfo.email
                    val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext())
                    prefs.edit()
                        .putString("yt_name", accountInfo.name)
                        .putString("yt_email", accountInfo.email ?: "")
                        .putString("yt_avatar", accountInfo.thumbnailUrl ?: "")
                        .apply()
                } else {
                    youtubeLogin?.title = "YouTube Account"
                    youtubeLogin?.summary = "Logged in (Failed to fetch info)"
                }
            }
        } else {
            youtubeLogin?.title = getString(R.string.login_to_youtube)
            youtubeLogin?.summary = getString(R.string.login_to_youtube_summary)
            youtubeLogin?.setOnPreferenceClickListener {
                YouTubeLoginDialog().show(childFragmentManager, YouTubeLoginDialog::class.java.name)
                true
            }
            
            useLoginForBrowsing?.isVisible = false
            autoSync?.isVisible = false
            ytLogout?.isVisible = false
        }
    }
}

