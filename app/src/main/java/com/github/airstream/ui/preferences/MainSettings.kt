package com.github.airstream.ui.preferences

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import com.github.airstream.BuildConfig
import com.github.airstream.R
import com.github.airstream.helpers.PreferenceHelper
import com.github.airstream.ui.base.BasePreferenceFragment
import com.github.airstream.ui.dialogs.ErrorDialog
import com.github.airstream.util.UpdateChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainSettings : BasePreferenceFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        val update = findPreference<Preference>("update")
        update?.summary = "v${BuildConfig.VERSION_NAME}"

        // check app update manually
        update?.setOnPreferenceClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                UpdateChecker(requireContext()).checkUpdate(true)
            }

            true
        }

        val crashlog = findPreference<Preference>("crashlog")
        crashlog?.isVisible = PreferenceHelper.getErrorLog().isNotEmpty() && BuildConfig.DEBUG
        crashlog?.setOnPreferenceClickListener {
            ErrorDialog().show(childFragmentManager, null)
            crashlog.isVisible = false
            true
        }
        
        listOf(
            "general" to R.id.action_global_generalSettings,
            "instance" to R.id.action_global_instanceSettings,
            "appearance" to R.id.action_global_appearanceSettings,
            "sponsorblock" to R.id.action_global_sponsorBlockSettings,
            "player" to R.id.action_global_playerSettings,
            "audio_video" to R.id.action_global_audioVideoSettings,
            "history" to R.id.action_global_historySettings,
            "notifications" to R.id.action_global_notificationSettings,
            "backup_restore" to R.id.action_global_backupRestoreSettings
        ).forEach { (preferenceKey, actionId) ->
            findPreference<Preference>(preferenceKey)?.setOnPreferenceClickListener { _ ->
                findNavController().navigate(actionId)
                true
            }
        }
    }
}
