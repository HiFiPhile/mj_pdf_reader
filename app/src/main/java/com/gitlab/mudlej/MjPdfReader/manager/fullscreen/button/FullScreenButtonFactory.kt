package com.gitlab.mudlej.MjPdfReader.manager.fullscreen.button

import FullScreenButton
import android.app.Activity
import android.view.View
import com.gitlab.mudlej.MjPdfReader.manager.fullscreen.button.impl.ExitFullScreenButton
import com.gitlab.mudlej.MjPdfReader.manager.fullscreen.button.impl.RotateScreenButton
import com.google.android.material.button.MaterialButton

class FullScreenButtonFactory {
    companion object {
        fun exitButton(activity: Activity, button: MaterialButton): FullScreenButton {
            return ExitFullScreenButton(activity, button)
        }
        fun rotateScreenButton(activity: Activity, button: MaterialButton): FullScreenButton {
            return RotateScreenButton(activity, button)
        }
        fun decBrightnessButton(activity: Activity, view: View): FullScreenButton {
            return DecBrightnessButton(activity, button)
        }
        fun incBrightnessButton(activity: Activity, view: View): FullScreenButton {
            return IncBrightnessButton(activity, button)
        }
        fun horizontalSwipeButton(activity: Activity, button: MaterialButton): FullScreenButton {
            return HorizontalSwipeButton(activity, button)
        }
        fun zoomLockButton(activity: Activity, button: MaterialButton): FullScreenButton {
            return ZoomLockButton(activity, button)
        }
        fun screenshotButton(activity: Activity, button: MaterialButton): FullScreenButton {
            return ScreenshotButton(activity, button)
        }
        fun toggleLabelButton(activity: Activity, button: MaterialButton): FullScreenButton {
            return ToggleLabelButton(activity, button)
        }
    }
}