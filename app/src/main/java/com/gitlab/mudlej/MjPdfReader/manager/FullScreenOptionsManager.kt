package com.gitlab.mudlej.MjPdfReader.manager

import android.view.View

interface FullScreenOptionsManager {

    enum class VisibilityState { VISIBLE, INVISIBLE, NONE }

    fun isVisible(): Boolean

    fun showAll()

    fun hideAll()

    fun toggleAll()

    fun showAllDelayed()

    fun hideAllDelayed()

    fun toggleAllDelayed()

    fun showAllTemporarily()

    fun hideAllTemporarily()

    fun toggleAllTemporarily()

    fun showAllTemporarilyOrHide()

    fun permanentlyHidePageHandle()

    fun getOnTouchListener(): View.OnTouchListener
}

