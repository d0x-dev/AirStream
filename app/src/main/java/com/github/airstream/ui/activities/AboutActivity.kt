package com.github.airstream.ui.activities

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.core.text.HtmlCompat
import androidx.core.text.parseAsHtml
import com.github.airstream.BuildConfig
import com.github.airstream.R
import com.github.airstream.databinding.ActivityAboutBinding
import com.github.airstream.helpers.IntentHelper
import com.github.airstream.ui.base.BaseActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AboutActivity : BaseActivity() {
    private lateinit var binding: ActivityAboutBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.tvVersion.text = "Version "$""

        binding.cardDarkboy.setOnClickListener {
            IntentHelper.openLinkFromHref(this, supportFragmentManager, "https://github.com/d0x-dev")
        }

        binding.cardVenom.setOnClickListener {
            IntentHelper.openLinkFromHref(this, supportFragmentManager, "https://github.com/drkvenom786")
        }

        binding.btnRepo.setOnClickListener {
            IntentHelper.openLinkFromHref(this, supportFragmentManager, GITHUB_URL)
        }

        binding.btnTelegram.setOnClickListener {
            IntentHelper.openLinkFromHref(this, supportFragmentManager, "https://t.me/songpy")
        }

        binding.btnLicense.setOnClickListener {
            showLicense()
        }
    }

    private fun showLicense() {
        val licenseHtml = assets.open("gpl3.html")
            .bufferedReader()
            .use { it.readText() }
            .parseAsHtml(HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH)

        MaterialAlertDialogBuilder(this)
            .setPositiveButton(getString(R.string.okay)) { _, _ -> }
            .setMessage(licenseHtml)
            .create()
            .show()
    }

    companion object {
        const val GITHUB_URL = "https://github.com/d0x-dev/AirStream"
    }
}