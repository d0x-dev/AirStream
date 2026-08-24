package com.github.airstream.ui.preferences

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.github.airstream.R
import com.github.airstream.constants.PreferenceKeys
import com.github.airstream.helpers.PreferenceHelper
import com.github.airstream.ui.base.BasePreferenceFragment
import com.github.airstream.ui.dialogs.NavBarOptionsDialog
import com.github.airstream.ui.dialogs.RequireRestartDialog
import com.google.android.material.color.DynamicColors

class AppearanceSettings : BasePreferenceFragment() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.appearance_settings, rootKey)

        val themeToggle = findPreference<ListPreference>(PreferenceKeys.THEME_MODE)
        themeToggle?.setOnPreferenceChangeListener { _, _ ->
            RequireRestartDialog().show(childFragmentManager, RequireRestartDialog::class.java.name)
            true
        }

        val pureTheme = findPreference<SwitchPreferenceCompat>(PreferenceKeys.PURE_THEME)
        pureTheme?.setOnPreferenceChangeListener { _, _ ->
            RequireRestartDialog().show(childFragmentManager, RequireRestartDialog::class.java.name)
            true
        }

        val accentColor = findPreference<ListPreference>(PreferenceKeys.ACCENT_COLOR)
        updateAccentColorValues(accentColor!!)
        accentColor.setOnPreferenceChangeListener { _, _ ->
            RequireRestartDialog().show(childFragmentManager, RequireRestartDialog::class.java.name)
            true
        }

        val navBarOptions = findPreference<Preference>(PreferenceKeys.NAVBAR_ITEMS)
        navBarOptions?.setOnPreferenceClickListener {
            NavBarOptionsDialog().show(childFragmentManager, null)
            true
        }
    }

    /**
     * Remove material you from accent color option if not available
     */
    private fun updateAccentColorValues(pref: ListPreference) {
        if (!DynamicColors.isDynamicColorAvailable()) {
            pref.entries = pref.entries.toList().subList(1, pref.entries.size).toTypedArray()
            pref.entryValues = pref.entryValues.toList().subList(1, pref.entryValues.size).toTypedArray()
        }
    }
}
