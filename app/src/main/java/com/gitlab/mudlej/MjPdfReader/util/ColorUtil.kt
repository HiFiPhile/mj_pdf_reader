package com.gitlab.mudlej.MjPdfReader.util

import android.content.Context
import android.content.res.ColorStateList
import android.view.Window
import com.gitlab.mudlej.MjPdfReader.enums.AppTheme
import com.google.android.material.elevation.SurfaceColors


object ColorUtil {

    var appTheme = AppTheme.DARK

//    fun colorize(context: Context, inputLayout: TextInputLayout) {
//        inputLayout.setStartIconTintList(colorOf(onBackground(context)))
//        inputLayout.hintTextColor = colorOf(onBackground(context))
//        inputLayout.placeholderTextColor = colorOf(onBackground(context))
//        inputLayout.editText?.setTextColor(onBackground(context))
//    }

    fun colorOf(color: Int): ColorStateList {
        return ColorStateList.valueOf(color)
    }

//    fun background(context: Context): Int {
//        return if (appTheme == AppTheme.DARK) {
//            ContextCompat.getColor(context, R.color.darkBackground)
//        } else {
//            ContextCompat.getColor(context, R.color.onDarkBackground)
//        }
//    }

//    private fun onBackground(context: Context): Int {
//        return if (appTheme == AppTheme.DARK) {
//            ContextCompat.getColor(context, R.color.onDarkBackground)
//        } else {
//            ContextCompat.getColor(context, R.color.darkBackground)
//        }
//    }

    fun colorize(context: Context, window: Window) {
        val color = SurfaceColors.SURFACE_2.getColor(context)
        window.statusBarColor = color
        window.navigationBarColor = color
    }
}