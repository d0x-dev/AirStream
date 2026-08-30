package com.github.airstream.ui.dialogs

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.darkxvenom.airbeats.innertube.YouTube
import com.github.airstream.R
import com.github.airstream.databinding.DialogYoutubeLoginBinding
import com.github.airstream.util.YouTubeSyncManager
import kotlinx.coroutines.launch

class YouTubeLoginDialog : DialogFragment() {

    private var _binding: DialogYoutubeLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_DeviceDefault_NoActionBar)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogYoutubeLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.webView.settings.apply {
            javaScriptEnabled = true
            setSupportZoom(true)
            builtInZoomControls = true
        }

        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(binding.webView, true)

        binding.webView.addJavascriptInterface(
            object {
                @JavascriptInterface
                fun onRetrieveVisitorData(newVisitorData: String?) {
                    if (newVisitorData != null) {
                        PreferenceManager.getDefaultSharedPreferences(requireContext())
                            .edit()
                            .putString("yt_visitor_data", newVisitorData)
                            .apply()
                        YouTube.visitorData = newVisitorData
                        
                        val appContext = requireContext().applicationContext
                        val cookieStr = YouTube.cookie ?: return
                        lifecycleScope.launch {
                            YouTubeSyncManager.syncData(appContext, cookieStr)
                        }
                    }
                }
            },
            "Android",
        )

        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String?) {
                if (url != null && url.startsWith("https://music.youtube.com/")) {
                    val cookieString = CookieManager.getInstance().getCookie(url)
                    if (cookieString != null && cookieString.contains("SAPISID")) {
                        PreferenceManager.getDefaultSharedPreferences(requireContext())
                            .edit()
                            .putString("yt_cookie", cookieString)
                            .apply()
                        YouTube.cookie = cookieString
                        
                        binding.webView.loadUrl("javascript:Android.onRetrieveVisitorData(window.yt.config_.VISITOR_DATA)")
                        
                        Toast.makeText(context, "Successfully logged in! Syncing data...", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                }
            }
        }

        binding.webView.loadUrl(
            "https://accounts.google.com/ServiceLogin?ltmpl=music&service=youtube&passive=true&continue=https://music.youtube.com/"
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
