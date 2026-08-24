package com.github.airstream.ui.preferences

import android.os.Bundle
import com.github.airstream.R
import com.github.airstream.ui.base.BasePreferenceFragment

class AudioVideoSettings : BasePreferenceFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.audio_video_settings, rootKey)
    }
}
