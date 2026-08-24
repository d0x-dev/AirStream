package com.github.airstream.ui.activities

import android.os.Bundle
import com.github.airstream.databinding.ActivityHelpBinding
import com.github.airstream.helpers.IntentHelper
import com.github.airstream.ui.base.BaseActivity
import com.google.android.material.card.MaterialCardView

class HelpActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        setupCard(binding.faq, FAQ_URL)
        setupCard(binding.matrix, MATRIX_URL)
        setupCard(binding.mastodon, MASTODON_URL)
        setupCard(binding.lemmy, LEMMY_URL)
    }

    private fun setupCard(card: MaterialCardView, link: String) {
        card.setOnClickListener {
            IntentHelper.openLinkFromHref(this, supportFragmentManager, link)
        }
    }

    companion object {
        private const val FAQ_URL = "https://airstream.dev/#faq"
        private const val MATRIX_URL = "https://matrix.to/#/#Airstream:matrix.org"
        private const val MASTODON_URL = "https://fosstodon.org/@airstream"
        private const val LEMMY_URL = "https://feddit.rocks/c/airstream"
    }
}
