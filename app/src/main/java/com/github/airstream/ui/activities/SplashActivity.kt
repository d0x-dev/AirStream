package com.github.airstream.ui.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

import androidx.lifecycle.lifecycleScope
import com.github.airstream.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        
        super.onCreate(savedInstanceState)
        
        // Disable native splash screen exit animation if possible so we seamlessly transition
        

        setContentView(R.layout.activity_splash)

        val tvSplashText = findViewById<TextView>(R.id.tvSplashText)
        val container = findViewById<View>(R.id.splashContainer)

        // Gradient for text
        tvSplashText.paint.shader = LinearGradient(
            0f, 0f, 0f, 100f, // will be adjusted in onSizeChanged, but this works for now
            intArrayOf(
                Color.parseColor("#E91E63"),
                Color.parseColor("#FFC107"),
                Color.parseColor("#2196F3")
            ),
            null,
            Shader.TileMode.CLAMP
        )

        // Adjust gradient size once measured
        tvSplashText.post {
            tvSplashText.paint.shader = LinearGradient(
                0f, 0f, tvSplashText.width.toFloat(), tvSplashText.height.toFloat(),
                intArrayOf(
                    Color.parseColor("#E91E63"),
                    Color.parseColor("#FFC107"),
                    Color.parseColor("#2196F3")
                ),
                null,
                Shader.TileMode.CLAMP
            )
            tvSplashText.invalidate()
        }

        // Animate
        container.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(900)
            .withEndAction {
                lifecycleScope.launch {
                    delay(600) // 900 + 600 = 1500 total
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
            }
            .start()
    }
}