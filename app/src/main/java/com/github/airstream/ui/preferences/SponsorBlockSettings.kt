package com.github.airstream.ui.preferences

import android.os.Bundle
import com.github.airstream.R
import com.github.airstream.ui.base.BasePreferenceFragment

class SponsorBlockSettings : BasePreferenceFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.sponsorblock_settings, rootKey)
    }
}
