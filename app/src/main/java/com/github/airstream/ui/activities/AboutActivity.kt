package com.github.airstream.ui.activities

import android.annotation.SuppressLint
import android.os.Bundle
import coil3.load
import coil3.request.crossfade
import com.github.airstream.BuildConfig
import com.github.airstream.R
import com.github.airstream.databinding.ActivityAboutBinding
import com.github.airstream.helpers.IntentHelper
import com.github.airstream.ui.base.BaseActivity

class AboutActivity : BaseActivity() {
    private lateinit var binding: ActivityAboutBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.appVersion.text = BuildConfig.VERSION_NAME

        // Load avatars
        binding.imgDarkboy.load("https://avatars.githubusercontent.com/u/218248866") {
            crossfade(true)
        }
        binding.imgVenom.load("https://avatars.githubusercontent.com/u/241423835") {
            crossfade(true)
        }

        // Global Action Listeners
        binding.btnGlobalFb.setOnClickListener {
            // FB link not provided, use default or ignore
        }
        binding.btnGlobalInsta.setOnClickListener {
            IntentHelper.openLinkFromHref(this, supportFragmentManager, "https://instagram.com/dark__336")
        }
        binding.btnGlobalGithub.setOnClickListener {
            IntentHelper.openLinkFromHref(this, supportFragmentManager, "https://github.com/d0x-dev/AirStream")
        }
        binding.btnGlobalGoogle.setOnClickListener {
            IntentHelper.openLinkFromHref(this, supportFragmentManager, "https://darkboy.pro")
        }
        binding.btnGlobalLicense.setOnClickListener {
            showLicense()
        }

        // Developer Cards
        binding.devDarkboy.setOnClickListener {
            IntentHelper.openLinkFromHref(this, supportFragmentManager, "https://github.com/d0x-dev")
        }
        binding.devVenom.setOnClickListener {
            IntentHelper.openLinkFromHref(this, supportFragmentManager, "https://github.com/drkvenom786")
        }

        // Darkboy Socials
        binding.btnDarkboyGithub.setOnClickListener {
            IntentHelper.openLinkFromHref(this, supportFragmentManager, "https://github.com/d0x-dev")
        }
        binding.btnDarkboyTelegram.setOnClickListener {
            IntentHelper.openLinkFromHref(this, supportFragmentManager, "https://t.me/songpy")
        }
        binding.btnDarkboyInsta.setOnClickListener {
            IntentHelper.openLinkFromHref(this, supportFragmentManager, "https://instagram.com/dark__336")
        }
        binding.btnDarkboyWebsite.setOnClickListener {
            IntentHelper.openLinkFromHref(this, supportFragmentManager, "https://darkboy.pro")
        }

        // Venom Socials
        binding.btnVenomGithub.setOnClickListener {
            IntentHelper.openLinkFromHref(this, supportFragmentManager, "https://github.com/drkvenom786")
        }
        binding.btnVenomWebsite.setOnClickListener {
            IntentHelper.openLinkFromHref(this, supportFragmentManager, "https://venomx.pro")
        }
    }

    private fun showLicense() {
        // Implement license dialog
    }
}