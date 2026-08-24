package com.github.airstream.util

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.github.airstream.BuildConfig
import com.github.airstream.R
import com.github.airstream.api.RetrofitInstance
import com.github.airstream.constants.IntentData.appUpdateChangelog
import com.github.airstream.constants.IntentData.appUpdateURL
import com.github.airstream.extensions.TAG
import com.github.airstream.extensions.toastFromMainDispatcher
import com.github.airstream.ui.dialogs.UpdateAvailableDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class UpdateChecker(private val context: Context) {
    suspend fun checkUpdate(isManualCheck: Boolean = false) {
        val currentAppVersion = BuildConfig.VERSION_NAME.filter { it.isDigit() }.toInt()

        try {
            val response = RetrofitInstance.externalApi.getLatestRelease()
            // version would be in the format "0.21.1"
            val update = response.name.filter { it.isDigit() }.toInt()

            if (currentAppVersion != update) {
                // Find the specific APK asset URL, fallback to htmlUrl if not found
                val apkAsset = response.assets.find { it.name.endsWith("_signed.apk") && it.name.startsWith("AirStream_v") }
                val downloadUrl = apkAsset?.browserDownloadUrl ?: response.htmlUrl
                
                withContext(Dispatchers.Main) {
                    showUpdateAvailableDialog(response.body, downloadUrl)
                }
                Log.i(TAG(), response.toString())
            } else if (isManualCheck) {
                context.toastFromMainDispatcher(R.string.app_uptodate)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showUpdateAvailableDialog(
        changelog: String,
        url: String
    ) {
        val dialog = UpdateAvailableDialog()
        val args =
            Bundle().apply {
                putString(appUpdateChangelog, sanitizeChangelog(changelog))
                putString(appUpdateURL, url)
            }
        dialog.arguments = args
        val fragmentManager = (context as? FragmentActivity)?.supportFragmentManager
        fragmentManager?.let {
            dialog.show(it, UpdateAvailableDialog::class.java.simpleName)
        }
    }

    private fun sanitizeChangelog(changelog: String): String {
        return changelog.substringBeforeLast("**Full Changelog**")
            .replace(Regex("in https://github\\.com/\\S+"), "")
    }
}