package com.github.airstream.ui.preferences

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.github.airstream.BuildConfig
import com.github.airstream.R
import com.github.airstream.ui.extensions.onSystemInsets
import androidx.core.view.updatePadding
import com.github.airstream.ui.activities.MainActivity
import com.github.airstream.util.UpdateChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainSettings : Fragment(R.layout.fragment_custom_settings) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.onSystemInsets { v, insets ->
            v.updatePadding(bottom = insets.bottom)
        }
        
        
        
        listOf(
            R.id.opt_general to R.id.action_global_generalSettings,
            R.id.opt_instance to R.id.action_global_instanceSettings,
            R.id.opt_appearance to R.id.action_global_appearanceSettings,
            R.id.opt_sponsorblock to R.id.action_global_sponsorBlockSettings,
            R.id.opt_player to R.id.action_global_playerSettings,
            R.id.opt_audio_video to R.id.action_global_audioVideoSettings,
            R.id.opt_history to R.id.action_global_historySettings,
            R.id.opt_notifications to R.id.action_global_notificationSettings,
            R.id.opt_backup_restore to R.id.action_global_backupRestoreSettings
        ).forEach { (viewId, actionId) ->
            view.findViewById<View>(viewId)?.setOnClickListener {
                findNavController().navigate(actionId)
            }
        }
        
        view.findViewById<TextView>(R.id.tv_update_summary)?.text = "v${BuildConfig.VERSION_NAME}"
        view.findViewById<View>(R.id.opt_update)?.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                UpdateChecker(requireContext()).checkUpdate(true)
            }
        }
    }
}