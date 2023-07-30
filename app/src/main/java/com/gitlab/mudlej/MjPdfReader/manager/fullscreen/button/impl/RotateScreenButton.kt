package com.gitlab.mudlej.MjPdfReader.manager.fullscreen.button.impl

import FullScreenButton
import android.app.Activity
import com.gitlab.mudlej.MjPdfReader.R
import com.google.android.material.button.MaterialButton

class RotateScreenButton(
    private val activity: Activity,
    private val button: MaterialButton
) : FullScreenButton(activity, button) {

    override fun getLabel() = activity.getString(R.string.rotate)

}