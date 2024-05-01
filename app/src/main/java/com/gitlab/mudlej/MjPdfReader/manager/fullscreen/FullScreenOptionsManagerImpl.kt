package com.gitlab.mudlej.MjPdfReader.manager.fullscreen

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import androidx.core.view.isVisible
import com.gitlab.mudlej.MjPdfReader.R
import com.gitlab.mudlej.MjPdfReader.data.PDF
import com.gitlab.mudlej.MjPdfReader.databinding.ActivityMainBinding
import com.gitlab.mudlej.MjPdfReader.manager.fullscreen.FullScreenOptionsManager.VisibilityState
import kotlin.reflect.KFunction1


class FullScreenOptionsManagerImpl(
    private val binding: ActivityMainBinding,
    private val pdf: PDF,
//    private val buttons: List<FullScreenButton>,
    private val delay: Long
) : FullScreenOptionsManager {

    private val delayHandler = Handler(Looper.getMainLooper())
    
    private var visibility: VisibilityState = VisibilityState.INVISIBLE
    private var labelVisibility: VisibilityState = VisibilityState.VISIBLE

    private val buttonsList: List<View> = listOf(
        binding.fullScreenButtonLayout,
        binding.exitFullScreenButton,
        binding.rotateScreenButton,
        binding.decBrightnessButton,
        binding.brightnessLabel,
        binding.incBrightnessButton,
        binding.autoScrollButton,
        binding.toggleHorizontalSwipeButton,
        binding.toggleZoomLockButton,
        binding.screenshotButton,
        binding.toggleLabelButton,
        binding.autoScrollLayout,
        binding.incScrollSpeedButton,
        binding.decScrollSpeedButton,
        binding.toggleAutoScrollButton,
        binding.reverseScrollDirectionButton,
    )

    init {
        setOnTouchListenerForAll()
    }

    override fun isVisible() = visibility == VisibilityState.VISIBLE

    override fun showAll() {
        if (pdf.isFullScreenToggled) {
            showFullScreenButtons()
        }
        showPageHandle()
        showAutoScrollLayout()
        visibility = VisibilityState.VISIBLE
    }

    override fun hideAll() {
        hideFullScreenButtons()
        hidePageHandle()
        hideAutoScrollLayout()
        visibility = VisibilityState.INVISIBLE
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
        return View.OnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> delayHandler.reset()
                MotionEvent.ACTION_UP -> hideAllDelayed()
            }
            isEventFullyConsumed
        }
    }

    override fun toggleLabelVisibility(drawableOf: KFunction1<Int, Drawable?>, getLabel: KFunction1<Int, String?>) {
        binding.apply {
            if (labelVisibility == VisibilityState.VISIBLE) {
                exitFullScreenButton.text = ""
                rotateScreenButton.text = ""
                autoScrollButton.text = ""
                toggleHorizontalSwipeButton.text = ""
                toggleZoomLockButton.text = ""
                screenshotButton.text = ""
                toggleLabelButton.text = ""
                
                brightnessLabel.visibility = View.GONE
                toggleLabelButton.icon = drawableOf(R.drawable.ic_double_arrow_right)
            }
            else {
                exitFullScreenButton.text = getLabel(R.string.exit)
                rotateScreenButton.text = getLabel(R.string.rotate)
                autoScrollButton.text = getLabel(R.string.auto_scroll)
                toggleHorizontalSwipeButton.text = getLabel(R.string.horizontal_lock)
                toggleZoomLockButton.text = getLabel(R.string.zoom_lock)
                screenshotButton.text = getLabel(R.string.screenshot)
                toggleLabelButton.text = getLabel(R.string.hide_labels)

                brightnessLabel.visibility = View.VISIBLE
                toggleLabelButton.icon = drawableOf(R.drawable.ic_double_arrow_left)

            }
        }
        labelVisibility = inverseVisibility(labelVisibility)
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
        binding.fullScreenButtonLayout.visibility = visibility
    }

    private fun showPageHandle() {
        binding.pdfView.scrollHandle?.customShow()
    }

    private fun hidePageHandle() {
        binding.pdfView.scrollHandle?.customHide()
    }

    private fun showAutoScrollLayout() {
        if (pdf.isFullScreenToggled && pdf.isAutoScrollVisible) {
            binding.autoScrollLayout.visibility = View.VISIBLE
            binding.autoScrollSpeedText.visibility = View.VISIBLE
        }
    }

    private fun hideAutoScrollLayout() {
        if (pdf.isFullScreenToggled && pdf.isAutoScrollVisible) {
            binding.autoScrollLayout.visibility = View.GONE
            binding.autoScrollSpeedText.visibility = View.GONE
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setOnTouchListenerForAll() {
        buttonsList.forEach { it.setOnTouchListener(getOnTouchListener()) }
    }

    private fun Handler.reset() {
        this.removeCallbacksAndMessages(null)
    }

    private fun inverseVisibility(visibility: VisibilityState): VisibilityState {
        return if (visibility == VisibilityState.VISIBLE) VisibilityState.INVISIBLE
        else VisibilityState.VISIBLE
    }

}