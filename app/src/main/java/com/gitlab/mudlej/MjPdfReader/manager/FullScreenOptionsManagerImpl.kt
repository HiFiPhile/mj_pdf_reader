package com.gitlab.mudlej.MjPdfReader.manager

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import androidx.core.view.children
import com.gitlab.mudlej.MjPdfReader.data.PDF
import com.gitlab.mudlej.MjPdfReader.databinding.ActivityMainBinding
import com.gitlab.mudlej.MjPdfReader.manager.FullScreenOptionsManager.VisibilityState


class FullScreenOptionsManagerImpl (
    private val binding: ActivityMainBinding,
    private val pdf: PDF,
    private val delay: Long
) : FullScreenOptionsManager {

    private val delayHandler = Handler(Looper.getMainLooper())
    private val buttonsList: List<View> = listOf(
        binding.exitFullScreenButton,
        binding.rotateScreenButton,
        binding.brightnessButtonLayout,
        binding.autoScrollLayout,
        binding.screenshotButton,
        binding.toggleHorizontalSwipeButton
    )
    private var currentState: VisibilityState = VisibilityState.INVISIBLE

    init {
        setOnTouchListenerForAll()
    }

    override fun isVisible() = currentState == VisibilityState.VISIBLE

    override fun showAll() {
        if (pdf.isFullScreenToggled) {
            showFullScreenButtons()
        }
        showPageHandle()
        currentState = VisibilityState.VISIBLE
    }

    override fun hideAll() {
        hideFullScreenButtons()
        hidePageHandle()
        currentState = VisibilityState.INVISIBLE
    }

    override fun toggleAll() {
        if (isVisible()) hideAll() else showAll()
    }

    override fun showAllDelayed() {
        delayAction(::showAll)
    }

    override fun hideAllDelayed() {
        delayAction(::hideAll)
    }

    override fun toggleAllDelayed() {
        delayAction(::toggleAll)
    }

    override fun showAllTemporarily() {
        doTemporarily(::showAll, ::hideAll)
    }

    override fun hideAllTemporarily() {
        doTemporarily(::hideAll, ::showAll)
    }

    override fun toggleAllTemporarily() {
        doTemporarily(::toggleAll, ::toggleAll)
    }

    override fun showAllTemporarilyOrHide() {
        if (!isVisible()) {
            showAllTemporarily()
        }
        else {
            hideAll()
        }
    }

    override fun permanentlyHidePageHandle() {
        binding.pdfView.scrollHandle?.permanentHide()
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun getOnTouchListener(): View.OnTouchListener {
        val isEventFullyConsumed = false    // false so clickOnListener will be triggered
        return View.OnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> delayHandler.reset()
                MotionEvent.ACTION_UP -> hideAllDelayed()
            }
            isEventFullyConsumed
        }
    }

    // -------------
    private fun delayAction(action: Runnable) {
        delayHandler.reset()
        delayHandler.postDelayed(action, delay)
    }

    private fun doTemporarily(action: Runnable, undoAction: Runnable) {
        delayHandler.reset()
        action.run()
        delayHandler.postDelayed(undoAction, delay)
    }

    private fun showFullScreenButtons() = changeFullScreenButtonsVisibility(true)

    private fun hideFullScreenButtons() = changeFullScreenButtonsVisibility(false)

    private fun changeFullScreenButtonsVisibility(isVisible: Boolean) {
        val visibility = if (isVisible) View.VISIBLE else View.GONE
        buttonsList.forEach { it.visibility = visibility }
    }

    private fun showPageHandle() {
        binding.pdfView.scrollHandle?.customShow()
    }

    private fun hidePageHandle() {
        binding.pdfView.scrollHandle?.customHide()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setOnTouchListenerForAll() {
        buttonsList.forEach { it.setOnTouchListener(getOnTouchListener()) }
        binding.brightnessButtonLayout.children.forEach { it.setOnTouchListener(getOnTouchListener()) }
        binding.autoScrollLayout.children.forEach { it.setOnTouchListener(getOnTouchListener()) }
    }

    private fun Handler.reset() {
        this.removeCallbacksAndMessages(null)
    }

}