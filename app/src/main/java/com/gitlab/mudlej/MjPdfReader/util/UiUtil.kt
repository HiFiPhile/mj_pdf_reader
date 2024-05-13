package com.gitlab.mudlej.MjPdfReader.util

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.view.Menu
import androidx.appcompat.view.menu.MenuBuilder
import com.gitlab.mudlej.MjPdfReader.R
import top.defaults.colorpicker.ColorPickerPopup


@SuppressLint("RestrictedApi")
fun Menu.showOptionalIcons() {
    if (this is MenuBuilder) {
        setOptionalIconsVisible(true)
    }
}

fun newColorPicker(activity: Activity): ColorPickerPopup {
    return ColorPickerPopup.Builder(activity)
        .initialColor(Color.BLUE)
        .enableBrightness(true)
        .enableAlpha(true)
        .okTitle(activity.getString(R.string.ok))
        .cancelTitle(activity.getString(R.string.cancel))
        .build()
}


fun configureSearchIcon(menu: Menu, show: Boolean) {
    val searchItem = menu.findItem(R.id.search_in_search_activity)
    searchItem?.isVisible = show
}