package com.github.airstream.ui.activities

import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.github.airstream.R
import com.github.airstream.databinding.ActivitySettingsBinding
import com.github.airstream.ui.base.BaseActivity

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivitySettingsBinding.inflate(layoutInflater)
        val navController = binding.settings.getFragment<NavHostFragment>().navController
        setSupportActionBar(binding.toolbar)
        setContentView(binding.root)

        // ensure that the toolbar's back button is always visible
        val appBarConfiguration = AppBarConfiguration.Builder()
            .setFallbackOnNavigateUpListener {
                finish()
                true
            }
            .build()
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        if (intent.extras?.getString(REDIRECT_KEY) == REDIRECT_TO_INTENT_SETTINGS) {
            navController.navigate(R.id.action_global_instanceSettings)
        } else if (intent.extras?.getString(REDIRECT_KEY) == REDIRECT_TO_ACCOUNT_SETTINGS) {
            navController.navigate(R.id.action_global_accountSettings)
        }
    }

    companion object {
        const val REDIRECT_KEY = "redirect"
        const val REDIRECT_TO_INTENT_SETTINGS = "intent_settings"
        const val REDIRECT_TO_ACCOUNT_SETTINGS = "account_settings"
    }
}



