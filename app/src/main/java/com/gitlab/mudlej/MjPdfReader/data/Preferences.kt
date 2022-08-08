package com.gitlab.mudlej.MjPdfReader.data

import android.content.SharedPreferences
import com.gitlab.mudlej.MjPdfReader.Utils

class Preferences(private val prefMan: SharedPreferences) {

    companion object {
        // Preferences keys
        const val firstInstallKey = "firstInstall"
        const val showFeaturesDialogKey = "showFeaturesDialog"
        const val highQualityKey = "highQuality"
        const val antiAliasingKey = "antiAliasing"
        const val horizontalScrollKey = "horizontalScroll"
        const val pageSnapKey = "pageSnap"
        const val pageFlingKey = "pageFling"
        const val pdfDarkThemeKey = "pdfDarkTheme"
        const val appDarkThemeKey = "appDarkTheme"
        const val screenOnKey = "screenOn"
        const val hideDelayKey = "hideDelay"
        const val showInLauncherKey = "showInLauncher"

        // Default values
        const val firstInstallDefault = true
        const val showFeaturesDialogDefault = true
        const val highQualityDefault = false
        const val antiAliasingDefault = true
        const val horizontalScrollDefault = false
        const val pageSnapDefault = false
        const val pageFlingDefault = false
        const val pdfDarkThemeDefault = false
        const val appDarkThemeDefault = false
        const val annotationRenderingDefault = true
        const val screenOnDefault = false

        const val hideDelayDefault = 4000
        const val spacingDefault = 10          // in dp
        const val minZoomDefault = 0.5f;
        const val midZoomDefault = 2.0f;
        const val maxZoomDefault = 5.0f;

        // Colors
        const val pdfDarkBackgroundColor = -0x313132          // -0x313132 = 0xffcecece
        const val pdfLightBackgroundColor = -0xcdcdce         // 0xff323232 = -0xcdcdce

    }

    // get values saved in Shared Preferences or return the default values
    fun getFirstInstall() = prefMan.getBoolean(firstInstallKey, firstInstallDefault)
    fun getShowFeaturesDialog() = prefMan.getBoolean(showFeaturesDialogKey, showFeaturesDialogDefault)
    fun getHighQuality() = prefMan.getBoolean(highQualityKey, highQualityDefault)
    fun getAntiAliasing() = prefMan.getBoolean(antiAliasingKey, antiAliasingDefault)
    fun getHorizontalScroll() = prefMan.getBoolean(horizontalScrollKey, horizontalScrollDefault)
    fun getPageSnap() = prefMan.getBoolean(pageSnapKey, pageSnapDefault)
    fun getPageFling() = prefMan.getBoolean(pageFlingKey, pageFlingDefault)
    fun getPdfDarkTheme() = prefMan.getBoolean(pdfDarkThemeKey, pdfDarkThemeDefault)
    fun getAppDarkTheme() = prefMan.getBoolean(appDarkThemeKey, appDarkThemeDefault)
    fun getScreenOn() = prefMan.getBoolean(screenOnKey, screenOnDefault)
    fun getAppVersion() = prefMan.getBoolean(Utils.getAppVersion(), firstInstallDefault)
    fun getHideDelay() = prefMan.getInt(hideDelayKey, hideDelayDefault)

    // put values in Shared Preferences
    fun setFirstInstall(value: Boolean) = prefMan.edit().putBoolean(firstInstallKey, value).apply()
    fun setShowFeaturesDialog(value: Boolean) = prefMan.edit().putBoolean(showFeaturesDialogKey, value).apply()
    fun setHighQuality(value: Boolean) = prefMan.edit().putBoolean(highQualityKey, value).apply()
    fun setAntiAliasing(value: Boolean) = prefMan.edit().putBoolean(antiAliasingKey, value).apply()
    fun setHorizontalScroll(value: Boolean) = prefMan.edit().putBoolean(horizontalScrollKey, value).apply()
    fun setPageSnap(value: Boolean) = prefMan.edit().putBoolean(pageSnapKey, value).apply()
    fun setPageFling(value: Boolean) = prefMan.edit().putBoolean(pageFlingKey, value).apply()
    fun setPdfDarkTheme(value: Boolean) = prefMan.edit().putBoolean(pdfDarkThemeKey, value).apply()
    fun setAppDarkTheme(value: Boolean) = prefMan.edit().putBoolean(appDarkThemeKey, value).apply()
    fun setScreenOn(value: Boolean) = prefMan.edit().putBoolean(screenOnKey, value).apply()
    fun setAppVersion(value: Boolean) = prefMan.edit().putBoolean(Utils.getAppVersion(), value).apply()
    fun setHideDelay(value: Int) = prefMan.edit().putInt(hideDelayKey, value).apply()












}