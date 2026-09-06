package com.github.airstream.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.TransitionAdapter
import com.github.airstream.R

class SingleViewTouchableMotionLayout(context: Context, attributeSet: AttributeSet? = null) :
    MotionLayout(context, attributeSet) {

    private val viewToDetectTouch by lazy {
        findViewById<View>(R.id.main_container) ?: findViewById(R.id.audio_player_container)
    }
    private val viewsToTranslate by lazy {
        if (isAudioPlayer) {
            listOf(viewToDetectTouch)
        } else {
            listOfNotNull(
                viewToDetectTouch,
                findViewById(R.id.player),
                findViewById(R.id.close_imageView),
                findViewById(R.id.play_imageView),
                findViewById(R.id.title_textView)
            )
        }
    }
    private val isAudioPlayer by lazy {
        viewToDetectTouch.id == R.id.audio_player_container
    }
    private val scaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val viewRect = Rect()
    private val transitionListenerList = mutableListOf<TransitionListener?>()
    private val swipeDownListener = mutableListOf<() -> Unit>()
    private val gestureDetector = GestureDetector(context, Listener())

    private var startedMinimized = false
    private var isStrictlyDownSwipe = false
    
    private var touchInitialX = 0f
    private var touchInitialY = 0f
    private var initialTranslationX = 0f
    private var initialTranslationY = 0f
    private var isFreeDragging = false

    private var isTouchDownInsideHitArea = false
    private var shouldInterceptTouchEvent = false

    init {
        super.setTransitionListener(object : TransitionAdapter() {
            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {
                transitionListenerList.filterNotNull()
                    .forEach { it.onTransitionChange(p0, p1, p2, p3) }
            }

            override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
                transitionListenerList.filterNotNull()
                    .forEach { it.onTransitionCompleted(p0, p1) }
            }
        })
    }

    override fun setTransitionListener(listener: TransitionListener?) {
        addTransitionListener(listener)
    }

    override fun addTransitionListener(listener: TransitionListener?) {
        transitionListenerList += listener
    }

    private inner class Listener : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            // Reset position before expanding
            viewsToTranslate.forEach { view ->
                view.animate().translationX(0f).translationY(0f).setDuration(200).start()
            }
            setTransitionDuration(200)
            transitionToStart()
            return true
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            if (isStrictlyDownSwipe && distanceY > 0) {
                isStrictlyDownSwipe = false
            }

            // Only trigger close on fast scroll if NOT freely dragging
            if (!isFreeDragging && isStrictlyDownSwipe && distanceY < -15F) {
                swipeDownListener.forEach { it.invoke() }
                return true
            }

            return false
        }
    }

    fun addSwipeDownListener(listener: () -> Unit) = apply {
        swipeDownListener.add(listener)
    }

    private fun isTouchInside(view: View, rawX: Float, rawY: Float): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val x = location[0]
        val y = location[1]
        return rawX >= x && rawX <= x + view.width && rawY >= y && rawY <= y + view.height
    }

    override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        // Prevent children (like ExoPlayer) from holding onto the touch when minimized,
        // so we can always drag the PiP view freely!
        if (progress == 1F) return
        super.requestDisallowInterceptTouchEvent(disallowIntercept)
    }

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                shouldInterceptTouchEvent = false
                isTouchDownInsideHitArea = false
                isFreeDragging = false

                if (isAudioPlayer) return false

                isTouchDownInsideHitArea = isTouchInside(viewToDetectTouch, event.rawX, event.rawY)
                
                touchInitialX = event.rawX
                touchInitialY = event.rawY
                initialTranslationX = viewToDetectTouch.translationX
                initialTranslationY = viewToDetectTouch.translationY
                
                startedMinimized = progress == 1F
                isStrictlyDownSwipe = true
            }

            MotionEvent.ACTION_MOVE -> {
                if (!shouldInterceptTouchEvent && isTouchDownInsideHitArea) {
                    val deltaX = event.rawX - touchInitialX
                    val deltaY = event.rawY - touchInitialY

                    if (!startedMinimized && deltaY > scaledTouchSlop) {
                        shouldInterceptTouchEvent = true
                        injectDownEvent(event)
                    } else if (startedMinimized && (Math.abs(deltaY) > scaledTouchSlop || Math.abs(deltaX) > scaledTouchSlop)) {
                        shouldInterceptTouchEvent = true
                        isFreeDragging = true
                    }
                }
            }
        }
        return shouldInterceptTouchEvent
    }

    private fun injectDownEvent(event: MotionEvent) {
        MotionEvent.obtain(event).apply {
            action = MotionEvent.ACTION_DOWN
            setLocation(event.x, event.y)
        }.also { downEvent ->
            onTouchEvent(downEvent)
            downEvent.recycle()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isTouchDownInsideHitArea && startedMinimized) {
            gestureDetector.onTouchEvent(event)
            
            when (event.actionMasked) {
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - touchInitialX
                    val deltaY = event.rawY - touchInitialY
                    
                    if (!isFreeDragging && (Math.abs(deltaY) > scaledTouchSlop || Math.abs(deltaX) > scaledTouchSlop)) {
                        isFreeDragging = true
                    }

                    if (isFreeDragging) {
                        viewsToTranslate.forEach { view ->
                            view.translationX = initialTranslationX + deltaX
                            view.translationY = initialTranslationY + deltaY
                        }
                        return true
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (isFreeDragging) {
                        isFreeDragging = false
                        
                        // Close if dragged off screen (bottom edge)
                        val screenHeight = resources.displayMetrics.heightPixels
                        val currentBottom = viewToDetectTouch.y + viewToDetectTouch.translationY + viewToDetectTouch.height
                        if (currentBottom > screenHeight * 0.95f) {
                             swipeDownListener.forEach { it.invoke() }
                        }
                        return true
                    }
                }
            }
        }

        // If we were freely dragging, don't pass the touch to MotionLayout
        if (isFreeDragging && startedMinimized) return true
        
        return isTouchDownInsideHitArea && super.onTouchEvent(event)
    }
}
