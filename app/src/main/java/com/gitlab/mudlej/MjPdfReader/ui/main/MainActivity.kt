/*
 *   MJ PDF Reader
 *   Copyright (C) 2022 Mudlej
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *  --------------------------
 *  This code was previously licensed under
 *
 *  MIT License
 *
 *  Copyright (c) 2018 Gokul Swaminathan
 *  Copyright (c) 2022 Mudlej
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.gitlab.mudlej.MjPdfReader.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.*
import android.print.PrintManager
import android.provider.MediaStore
import android.provider.Settings
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.net.toUri
import androidx.core.text.isDigitsOnly
import androidx.core.view.*
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.github.barteksc.pdfviewer.PDFView.Configurator
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.github.barteksc.pdfviewer.scroll.ScrollHandle
import com.github.barteksc.pdfviewer.util.Constants
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.gitlab.mudlej.MjPdfReader.Launcher
import com.gitlab.mudlej.MjPdfReader.Launchers
import com.gitlab.mudlej.MjPdfReader.R
import com.gitlab.mudlej.MjPdfReader.data.*
import com.gitlab.mudlej.MjPdfReader.databinding.ActivityMainBinding
import com.gitlab.mudlej.MjPdfReader.databinding.PasswordDialogBinding
import com.gitlab.mudlej.MjPdfReader.manager.database.DatabaseManager
import com.gitlab.mudlej.MjPdfReader.manager.database.DatabaseManagerImpl
import com.gitlab.mudlej.MjPdfReader.manager.fullscreen.FullScreenOptionsManager
import com.gitlab.mudlej.MjPdfReader.manager.fullscreen.FullScreenOptionsManagerImpl
import com.gitlab.mudlej.MjPdfReader.repository.AppDatabase
import com.gitlab.mudlej.MjPdfReader.ui.*
import com.gitlab.mudlej.MjPdfReader.ui.about.AboutActivity
import com.gitlab.mudlej.MjPdfReader.ui.bookmark.BookmarksActivity
import com.gitlab.mudlej.MjPdfReader.ui.search.SearchActivity
import com.gitlab.mudlej.MjPdfReader.ui.settings.SettingsActivity
import com.gitlab.mudlej.MjPdfReader.ui.text_mode.TextModeActivity
import com.gitlab.mudlej.MjPdfReader.util.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shockwave.pdfium.PdfPasswordException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.sign
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {
    enum class AdditionalOptions { APP_SETTINGS, TEXT_MODE, METADATA, ADVANCED_CONFIG, ABOUT }

    private val shouldStopExtracting: MutableMap<Int, Boolean> = mutableMapOf()
    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding

    private val handler = Handler(Looper.getMainLooper())
    private val autoScrollHandler = Handler(Looper.getMainLooper())

    private lateinit var fullScreenOptionsManager: FullScreenOptionsManager
    private lateinit var databaseManager: DatabaseManager
    private lateinit var pref: Preferences
    private val pdf = PDF()
    private val extras = ExtendedDataHolder.instance

    private val launchers = Launchers(
        Launcher(this, pdf).pdfPicker(),
        Launcher(this, pdf).saveToDownloadPermission(::saveDownloadedFileAfterPermissionRequest),
        Launcher(this, pdf).readFileErrorPermission(::restartAppIfGranted),
        Launcher(this, pdf).settings(::displayFromUri)
    )

    private lateinit var appTitle: TextView
    private lateinit var appTitlePageNumber: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "-----------onCreate: ${pdf.name} ")
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCustomActionBar()

        // To avoid FileUriExposedException, (https://stackoverflow.com/questions/38200282/)
        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().build())

        // init
        pref = Preferences(PreferenceManager.getDefaultSharedPreferences(this))
        fullScreenOptionsManager = FullScreenOptionsManagerImpl(binding, pdf, pref.getHideDelay().toLong())
        databaseManager = DatabaseManagerImpl(AppDatabase.getInstance(applicationContext))

        Constants.THUMBNAIL_RATIO = pref.getThumbnailRation()
        Constants.PART_SIZE = pref.getPartSize()

        // Show Into Activity and Features Dialog on the first install
        onFirstInstall()

        // Create PDF by restoring it in case of an activity restart OR open filer picker
        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState)
        } else {
            pdf.uri = intent.data
            if (pdf.uri == null) pickFile()
        }

        displayFromUri(pdf.uri)
        setButtonsFunctionalities()
        //showAppFeaturesDialogOnFirstRun()
    }

    fun initPdf(pdf: PDF, uri: Uri) {
        pdf.uri = uri
        pdf.fileHash = computeHash(this@MainActivity, pdf)
        if (pdf.fileHash == null) {
            respondToNoFileHash()
        }
    }

    private fun respondToNoFileHash() {
        //throw IllegalStateException("Failed to compute file hash")
        Toast.makeText(
            this,
            "Can't hash the file! Last visited page won't be remembered in this session.",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun setCustomActionBar() {
        val actionBar = supportActionBar
        // Disable the default and enable the custom
        actionBar?.setDisplayShowTitleEnabled(false)
        actionBar?.setDisplayShowCustomEnabled(true)

        val customView: View = layoutInflater.inflate(R.layout.actionbar_title, null)
        appTitlePageNumber = customView.findViewById(R.id.actionbarPageNumber)
        appTitle = customView.findViewById(R.id.actionbarTitle)

        // Change the font family (optional)
        appTitle.setTypeface(Typeface.SERIF)
        appTitlePageNumber.setTypeface(Typeface.SERIF)

        fun titleClickListener() {
            val title = pdf.getTitle()
            if (title.isNotBlank()) {
                Toast.makeText(this, title, Toast.LENGTH_LONG).show()
            }
        }
        appTitle.setOnClickListener { titleClickListener() }
        appTitlePageNumber.setOnClickListener { titleClickListener() }

        // Apply the custom view
        actionBar?.customView = customView
    }

    private fun onFirstInstall() {
        val isFirstRun = pref.getFirstInstall()
        if (isFirstRun) {
            // To avoid com.github.paolorotolo.appintro.AppIntroBaseFragment.onCreateView
            // android.content.res.Resources$NotFoundException
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                startActivity(Intent(this, MainIntroActivity::class.java))
            }
            pref.setFirstInstall(false)
            pref.setShowFeaturesDialog(true)
        }
    }

    private fun pickFile() {
        try {
            launchers.pdfPicker.launch(arrayOf(PDF.FILE_TYPE))
        } catch (e: ActivityNotFoundException) {
            // alert user that file manager not working
            Toast.makeText(this, R.string.toast_pick_file_error, Toast.LENGTH_LONG).show()
        }
    }

    fun displayFromUri(uri: Uri?) {
        if (uri == null) return

        pdf.name = getFileName(this, uri)
        updateAppTitle()
        pdf.resetLength()

        setTaskDescription(ActivityManager.TaskDescription(pdf.name))
        val scheme = uri.scheme
        if (scheme != null && scheme.contains("http")) {
            downloadOrShowDownloadedFile(uri)
        } else {
            initPdfViewAndLoad(binding.pdfView.fromUri(pdf.uri))

            // start extracting text in the background
            //if (!pdf.isExtractingTextFinished) extractPdfText()
        }
    }

    private fun updateAppTitle() {
        appTitle.text = pdf.getTitleWithPageNumber()
    }

    private fun initPdfViewAndLoad(viewConfigurator: Configurator) {
        // attempt to find a saved location for the pdf else assign zero
        if (pdf.pageNumber == 0) {
            lifecycleScope.launchWhenCreated {
                val hash = computeHash(this@MainActivity, pdf) ?: return@launchWhenCreated
                val pageNumber = databaseManager.findLocation(hash)

                pdf.fileHash = hash
                pdf.pageNumber = pageNumber

                withContext(Dispatchers.Main) {
                    initPdfViewAndLoad(viewConfigurator, pageNumber)
                }
            }
        } else initPdfViewAndLoad(viewConfigurator, pdf.pageNumber)
    }

    private fun initPdfViewAndLoad(viewConfigurator: Configurator, pageNumber: Int) {
        configureTheme()

        val pdfView = binding.pdfView
        pdfView.useBestQuality(pref.getHighQuality())
        pdfView.minZoom = Preferences.minZoomDefault
        pdfView.midZoom = Preferences.midZoomDefault
        pdfView.maxZoom = pref.getMaxZoom()
        pdfView.zoomTo(pdf.zoom)

        viewConfigurator   // creates a Configurator
            .defaultPage(pageNumber)
            .onPageChange { page: Int, pageCount: Int -> setCurrentPage(page, pageCount) }
            .enableAnnotationRendering(Preferences.annotationRenderingDefault)
            .enableAntialiasing(pref.getAntiAliasing())
            .onTap { fullScreenOptionsManager.showAllTemporarilyOrHide(); true }
            .onLongPress { copyPageText(false) }
            .scrollHandle(createScrollHandle())
            .spacing(Preferences.spacingDefault)
            .onError { exception: Throwable -> handleFileOpeningError(exception) }
            .onPageError { page: Int, error: Throwable -> reportLoadPageError(page, error) }
            .pageFitPolicy(FitPolicy.WIDTH)
            .password(pdf.password)
            .swipeHorizontal(pref.getHorizontalScroll())
            .autoSpacing(pref.getHorizontalScroll())
            .pageSnap(pref.getPageSnap())
            .pageFling(pref.getPageFling())
            .nightMode(pref.getPdfDarkTheme())
            .load()


        // Show the page scroll handler for 3 seconds when the pdf is loaded then hide it.
        pdfView.performTap()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createScrollHandle(): ScrollHandle {
        // hiding the handle if the pdf.length is 1 will happen when pdf.length is set in setPdfLength()
        val handle = DefaultScrollHandle(this)
        handle.setOnTouchListener(fullScreenOptionsManager.getOnTouchListener())
        handle.setOnClickListener { goToPage() }
        return handle
    }

    private fun copyPageText(bypass: Boolean) {
        val pageNumber = pdf.pageNumber
        if (shouldStopExtracting.getOrElse(pageNumber) { false }) {
            return
        }

        var pageText = ""
        CoroutineScope(Dispatchers.IO).launch {
            try {
                pageText = binding.pdfView.getPageText(pageNumber)
            }
            catch (e: Throwable) {
                Log.e("PdfBox", "extractPageText($pageNumber): error while stripping text", e)
                showFailedExtractTextSnackbar(pageNumber)
            }

            withContext(Dispatchers.Main) {
                if (pageText.isEmpty() || pageText.isBlank())
                    showNoTextInPageToast()
                else
                    showCopyPageTextDialog(this@MainActivity, pageNumber, pageText, pref, bypass)
            }
        }
    }

    private fun showFailedExtractTextSnackbar(pageNumber: Int) {
        Snackbar.make(binding.root, "Failed to extract text of this file.", Snackbar.LENGTH_SHORT)
            .setAction("Stop this message") { shouldStopExtracting[pageNumber] = true }
            .show()
    }

    private fun showNoTextInPageToast() {
        Toast.makeText(this, "Couldn't find text in this page.", Toast.LENGTH_LONG).show()
    }


    @SuppressLint("SourceLockedOrientationActivity")
    private fun setButtonsFunctionalities() {
        exitFullScreenListener(binding)
        setBrightnessButtons(binding)
        setAutoScrollButtons(binding)
        binding.apply {
            rotateScreenButton.setOnClickListener { rotateScreenButtonListener() }
            brightnessButton.setOnClickListener { brightnessButtonListener(binding) }
            autoScrollButton.setOnClickListener { autoScrollButtonListener(binding) }
            screenshotButton.setOnClickListener { takeScreenshot() }
            toggleHorizontalSwipeButton.setOnClickListener { horizontalSwipeButtonListener(binding) }
            pickFile.setOnClickListener { pickFile() }
        }
    }

    private fun rotateScreenButtonListener() {
        requestedOrientation =
            if (pdf.isPortrait) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        pdf.togglePortrait()
    }

    private fun horizontalSwipeButtonListener(binding: ActivityMainBinding) {
        binding.apply {
            if (pdfView.isHorizontalSwipeDisabled) {
                enableHorizontalSwiping(binding)
            } else {
                disableHorizontalSwiping(binding)
            }
        }
        fixButtonsColor()
    }

    private fun enableHorizontalSwiping(binding: ActivityMainBinding) {
        binding.toggleHorizontalSwipeImage.setImageResource(R.drawable.ic_horizontal_swipe_locked)
        binding.pdfView.isHorizontalSwipeDisabled = false
    }

    private fun disableHorizontalSwiping(binding: ActivityMainBinding) {
        binding.toggleHorizontalSwipeImage.setImageResource(R.drawable.ic_allow_horizontal_swipe)
        binding.pdfView.isHorizontalSwipeDisabled = true
    }

    private fun setBrightnessButtons(binding: ActivityMainBinding) {
        // init the seekbar
        val brightness = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        binding.brightnessSeekBar.progress = brightness
        binding.brightnessPercentage.text = "$brightness%"
        binding.brightnessSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (seekBar == null) return
                // Don't override system's brightness if the user didn't manually asked for it
                if (fromUser) updateBrightness(progress)
            }
        })
    }

    private fun setAutoScrollButtons(binding: ActivityMainBinding) {
        var isAutoScrolling = false
        val delay = 1L
        val interval = 0.25
        var scrollBy = -interval * 3

        binding.autoScrollSpeedText.text = formatSpeed(scrollBy)

        binding.increaseScrollSpeedButton.setOnClickListener {
            scrollBy = changeScrollingSpeed(scrollBy, interval, isIncreasing = true)
        }
        binding.decreaseScrollSpeedButton.setOnClickListener {
            if (scrollBy.absoluteValue > interval)
                scrollBy = changeScrollingSpeed(scrollBy, interval, isIncreasing = false)
        }
        // check this out: https://stackoverflow.com/questions/7938516/continuously-increase-integer-value-as-the-button-is-pressed
        val handler = Handler(mainLooper)
        lateinit var runnable: Runnable
        val DELAY = 100L

        fun createUpdatingSpeedRunnable(isIncreasing: Boolean): Boolean {
            runnable = Runnable {
                if (!binding.increaseScrollSpeedButton.isPressed && !binding.decreaseScrollSpeedButton.isPressed) {
                    return@Runnable
                }

                scrollBy = changeScrollingSpeed(scrollBy, interval, isIncreasing)
                handler.postDelayed(runnable, DELAY)
            }
            handler.postDelayed(runnable, DELAY)
            return true
        }

        binding.increaseScrollSpeedButton.setOnLongClickListener { createUpdatingSpeedRunnable(isIncreasing = true) }
        binding.decreaseScrollSpeedButton.setOnLongClickListener { createUpdatingSpeedRunnable(isIncreasing = false) }

        binding.reverseAutoScrollButton.setOnClickListener { scrollBy = -scrollBy }

        binding.toggleAutoScrollButton.setOnClickListener {
            isAutoScrolling = !isAutoScrolling

            if (!isAutoScrolling) {
                stopAutoScrolling(binding)
                return@setOnClickListener
            } else {
                binding.toggleAutoScrollButton.setImageResource(R.drawable.ic_pause)
            }

            fun scroll() {
                autoScrollHandler.postDelayed({
                    binding.pdfView.moveRelativeTo(0F, scrollBy.toFloat())
                    binding.pdfView.loadPages()

                    if (isAutoScrolling || pdf.pageNumber < pdf.length) {
                        scroll()
                    }
                }, delay)
            }
            scroll()
        }
    }

    private fun stopAutoScrolling(binding: ActivityMainBinding) {
        binding.toggleAutoScrollButton.setImageResource(R.drawable.ic_start)
        autoScrollHandler.removeCallbacksAndMessages(null)
    }

    private fun autoScrollButtonListener(binding: ActivityMainBinding) {
        binding.apply {
            if (toggleAutoScrollButton.isVisible) {
                increaseScrollSpeedButton.visibility = View.GONE
                decreaseScrollSpeedButton.visibility = View.GONE
                reverseAutoScrollButton.visibility = View.GONE
                toggleAutoScrollButton.visibility = View.GONE
                autoScrollSpeedText.visibility = View.GONE
            } else {
                increaseScrollSpeedButton.visibility = View.VISIBLE
                decreaseScrollSpeedButton.visibility = View.VISIBLE
                reverseAutoScrollButton.visibility = View.VISIBLE
                toggleAutoScrollButton.visibility = View.VISIBLE
                autoScrollSpeedText.visibility = View.VISIBLE
            }
        }
    }

    private fun brightnessButtonListener(binding: ActivityMainBinding) {
        binding.apply {
            if (brightnessSeekBar.isVisible) {
                brightnessSeekBar.visibility = View.GONE
                brightnessPercentage.visibility = View.GONE
            } else {
                brightnessSeekBar.visibility = View.VISIBLE
                brightnessPercentage.visibility = View.VISIBLE
            }
        }
    }

    private fun exitFullScreenListener(binding: ActivityMainBinding) {
        binding.exitFullScreenButton.setOnClickListener {
            unlockScreenOrientation()
            toggleFullscreen()
            stopAutoScrolling(binding)
            enableHorizontalSwiping(binding)

            // A try to give the brightness control back to the system but this won't work
            // updateBrightness(brightness)
        }
    }

    private fun unlockScreenOrientation() {
        // set orientation to unspecified so that the screen rotation will be unlocked
        // this is because PORTRAIT / LANDSCAPE modes will lock the app in them
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    // TODO: improve this
    private fun formatSpeed(scrollBy: Double) = (scrollBy.absoluteValue * 4).toInt().toString()

    private fun changeScrollingSpeed(scrollBy: Double, interval: Double, isIncreasing: Boolean): Double {
        val newSpeed = if (isIncreasing) {
            (scrollBy.absoluteValue + interval) * scrollBy.sign
        } else {
            if (scrollBy.absoluteValue > interval) {
                (scrollBy.absoluteValue - interval) * scrollBy.sign
            } else {
                scrollBy
            }
        }

        binding.autoScrollSpeedText.text = formatSpeed(newSpeed)
        return newSpeed
    }

    private fun updateBrightness(brightness: Int) {
        binding.brightnessPercentage.text = "$brightness%"
        val layout = window.attributes
        window.attributes.screenBrightness = brightness.toFloat() / 100
        window.attributes = layout
    }

    public override fun onResume() {
        Log.i(TAG, "-----------onResume: ${pdf.name} ")
        super.onResume()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (pref.getScreenOn()) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // check if there is a pdf at first
        // if (pdf.uri == null) return

        if (pdf.uri != null) binding.pickFile.visibility = View.GONE

        // restore the full screen mode if was toggled On
        restoreFullScreenIfNeeded()

        // Prompt the user to restore the previous zoom if there is one saved other than the default
        // pdfZoom != binding.pdfView.getZoom())   // doesn't work for some peculiar reason
        if (pdf.zoom != 1f) {
            Snackbar.make(
                findViewById(R.id.mainLayout),
                getString(R.string.ask_restore_zoom), Snackbar.LENGTH_LONG
            )
                .setAction(getString(R.string.restore)) {
                    binding.pdfView.zoomWithAnimation(pdf.zoom)
                }
                .show()
        }
        fixButtonsColor()
    }

    private fun restoreFullScreenIfNeeded() {
        if (pdf.isFullScreenToggled) {
            pdf.isFullScreenToggled = false
            toggleFullscreen()
        }
    }

    private fun fixButtonsColor() {
        // changes buttons color
        val color = if (pref.getPdfDarkTheme()) R.color.bright else R.color.dark
        DrawableCompat.setTint(
            DrawableCompat.wrap(binding.exitFullScreenImage.drawable),
            ContextCompat.getColor(this, color)
        )
        DrawableCompat.setTint(
            DrawableCompat.wrap(binding.rotateScreenImage.drawable),
            ContextCompat.getColor(this, color)
        )
        DrawableCompat.setTint(
            DrawableCompat.wrap(binding.brightnessButton.drawable),
            ContextCompat.getColor(this, color)
        )
        DrawableCompat.setTint(
            DrawableCompat.wrap(binding.autoScrollButton.drawable),
            ContextCompat.getColor(this, color)
        )
        DrawableCompat.setTint(
            DrawableCompat.wrap(binding.screenshotImage.drawable),
            ContextCompat.getColor(this, color)
        )
        DrawableCompat.setTint(
            DrawableCompat.wrap(binding.toggleHorizontalSwipeImage.drawable),
            ContextCompat.getColor(this, color)
        )
    }

    private fun shareFile(uri: Uri?, type: FileType) {
        if (uri == null) {
            checkHasFile()  // only to show the message
            return
        }
        val sharingIntent: Intent =
            if (uri.scheme != null && uri.scheme!!.startsWith("http"))
                plainTextShareIntent(getString(R.string.share_file), pdf.uri.toString())
            else if (type == FileType.PDF)
                fileShareIntent(getString(R.string.share_file), pdf.name, uri)
            else if (type == FileType.IMAGE)
                imageShareIntent(getString(R.string.share_file), pdf.name, uri)
            else return

        try {
            startActivity(sharingIntent)
        } catch (e: Throwable) {
            Toast.makeText(this, "Error sharing the file. (${e.message})", Toast.LENGTH_LONG).show()
        }
    }

    private fun configureTheme() {
        // This should be moved to the onCreate or xml files
        window.statusBarColor = Color.parseColor("#1a1b1b")

        val pdfView = binding.pdfView

        // set background color behind pages
        if (!pref.getPdfDarkTheme()) pdfView.setBackgroundColor(Preferences.pdfDarkBackgroundColor) else pdfView.setBackgroundColor(
            Preferences.pdfLightBackgroundColor
        )
        if (pref.getAppFollowSystemTheme()) {
            if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            )
        } else {
            if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO) AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    private fun reportLoadPageError(page: Int, error: Throwable) {
        val message = resources.getString(R.string.cannot_load_page) + page + " " + error
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        Log.e(TAG, message)
    }

    private fun handleFileOpeningError(exception: Throwable) {
        if (exception is PdfPasswordException) {
            if (pdf.password != null) {
                Toast.makeText(this, R.string.wrong_password, Toast.LENGTH_SHORT).show()
                pdf.password = null // prevent the toast if the user rotates the screen
            }
            askForPdfPassword()
        } else if (couldNotOpenFileDueToMissingPermission(exception)) {
            launchers.readFileErrorPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            Toast.makeText(this, R.string.file_opening_error, Toast.LENGTH_LONG).show()
            Log.e(TAG, getString(R.string.file_opening_error), exception)
        }
    }

    private fun couldNotOpenFileDueToMissingPermission(e: Throwable): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED
        ) return false
        val exceptionMessage = e.message
        return e is FileNotFoundException && exceptionMessage != null
                && exceptionMessage.contains(getString(R.string.permission_denied))
    }

    private fun restartAppIfGranted(isPermissionGranted: Boolean) {
        if (isPermissionGranted) {
            // This is a quick and dirty way to make the system restart the current activity *and the current app process*.
            // This is needed because on Android 6 storage permission grants do not take effect until
            // the app process is restarted.
            exitProcess(0)
        } else {
            Toast.makeText(this, R.string.file_opening_error, Toast.LENGTH_LONG).show()
        }
    }

    private fun toggleFullscreen() {
        fun showUi() {
            supportActionBar?.show()
            binding.pdfView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }

        fun hideUi() {
            supportActionBar?.hide()
            binding.pdfView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    )
        }

        if (!pdf.isFullScreenToggled) {
            hideUi()
            pdf.isFullScreenToggled = true
            fullScreenOptionsManager.hideAll()

            // show how to exit Full Screen dialog
            if (pref.getShowFeaturesDialog()) {
                showHowToExitFullscreenDialog(this, pref)
            }
        } else {
            showUi()
            pdf.isFullScreenToggled = false
            fullScreenOptionsManager.showAllTemporarilyOrHide()
        }
    }

    private fun downloadOrShowDownloadedFile(uri: Uri) {
        if (pdf.downloadedPdf == null) {
            pdf.downloadedPdf = lastCustomNonConfigurationInstance as ByteArray?
        }
        if (pdf.downloadedPdf != null) {
            initPdfViewAndLoad(binding.pdfView.fromBytes(pdf.downloadedPdf))
        } else {
            // we will get the pdf asynchronously with the DownloadPDFFile object
            binding.progressBar.visibility = View.VISIBLE
            val downloadPDFFile =
                DownloadPDFFile(this)
            downloadPDFFile.execute(uri.toString())
        }
    }

    override fun onRetainCustomNonConfigurationInstance(): Any? {
        return pdf.downloadedPdf
    }

    fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

    fun saveToFileAndDisplay(pdfFileContent: ByteArray?) {
        pdf.downloadedPdf = pdfFileContent
        saveToDownloadFolderIfAllowed(pdfFileContent)
        initPdfViewAndLoad(binding.pdfView.fromBytes(pdfFileContent))
    }

    private fun saveToDownloadFolderIfAllowed(fileContent: ByteArray?) {
        if (canWriteToDownloadFolder(this)) {
            trySaveToDownloads(fileContent, false)
        } else {
            launchers.saveToDownloadPermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun trySaveToDownloads(fileContent: ByteArray?, showSuccessMessage: Boolean) {
        try {
            val downloadDirectory =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            writeBytesToFile(downloadDirectory, pdf.name, fileContent)
            if (showSuccessMessage) {
                Toast.makeText(this, R.string.saved_to_download, Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            Log.e(TAG, getString(R.string.save_to_download_failed), e)
            Toast.makeText(this, R.string.save_to_download_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveDownloadedFileAfterPermissionRequest(isPermissionGranted: Boolean) {
        if (isPermissionGranted) {
            trySaveToDownloads(pdf.downloadedPdf, true)
        } else {
            Toast.makeText(this, R.string.save_to_download_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun navToAppSettings() {
        launchers.settings.launch(Intent(this, SettingsActivity::class.java))
    }

    private fun setCurrentPage(pageNumber: Int, pageCount: Int) {
        pdf.pageNumber = pageNumber
        setPdfLength(pageCount)
        updateAppTitle()

        val hash = pdf.fileHash ?: computeHash(this, pdf)
        if (hash == null) {
            respondToNoFileHash()
            return
        }

        lifecycleScope.launchWhenCreated {
            databaseManager.saveLocationInBackground(hash, pageNumber)
        }
    }

    private fun setPdfLength(pageCount: Int) {
        pdf.initPdfLength(pageCount)
        if (pageCount == 1) {
            fullScreenOptionsManager.permanentlyHidePageHandle()
        }
    }

    private fun printFile() {
        if (checkHasFile()) {
            val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
            try {
                printManager.print(
                    pdf.name,
                    PdfDocumentAdapter(this, pdf.uri), null
                )
            } catch (e: Throwable) {
                Toast.makeText(this, "Failed to print. Error message: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun askForPdfPassword() {
        val dialogBinding = PasswordDialogBinding.inflate(layoutInflater)
        showAskForPasswordDialog(this, pdf, dialogBinding, ::displayFromUri)
    }

    private fun showAppFeaturesDialogOnFirstRun() {
        if (pref.getShowFeaturesDialog()) {
            Handler(mainLooper).postDelayed({ showAppFeaturesDialog(this) }, 500)
            pref.setShowFeaturesDialog(false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        menu.showOptionalIcons()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.fullscreenOption -> toggleFullscreen()
            R.id.switchThemeOption -> switchPdfTheme()
            R.id.openFileOption -> pickFile()
            R.id.copyPageText -> copyPageText(true)
            R.id.bookmarksListOption -> showBookmarks()
            R.id.goToPageOption -> goToPage()
            R.id.linksListOption -> showLinks()
            R.id.shareFileOption -> shareFile(pdf.uri, FileType.PDF)
            R.id.printFileOption -> printFile()
            R.id.searchOption -> {
                val dialog = createNewSearchDialog()
                dialog.show()
            }
            R.id.additionalOptionsOption -> showAdditionalOptions()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun createNewSearchDialog(): AlertDialog.Builder {
        val searchLayout = LayoutInflater.from(this).inflate(R.layout.input_layout, null) as TextInputLayout
        return AlertDialog.Builder(this, R.style.MJDialogThemeLight)
            .setTitle(resources.getString(R.string.search))
            .setMessage(resources.getString(R.string.search_dialog_message))
            .setView(searchLayout)
            .setPositiveButton(resources.getText(R.string.search)) { searchDialog, _ ->
                val query = searchLayout.editText?.text ?: return@setPositiveButton
                fun startSearchActivity() {
                    Intent(this, SearchActivity::class.java).also { searchIntent ->
                        searchIntent.putExtra(PDF.filePathKey, pdf.uri.toString())
                        searchIntent.putExtra(PDF.searchQueryKey, query.toString())
                        startActivityForResult(searchIntent, PDF.startSearchActivity)
                    }
                }
                if (query.isBlank() || query.length < 3) {
                    AlertDialog.Builder(this)
                        .setTitle(getString(R.string.too_short_query))
                        .setMessage(getString(R.string.too_short_query_message).format(query))
                        .setNeutralButton("Proceed Anyway") {badQueryDialog, _ ->
                            startSearchActivity()
                        }
                        .setPositiveButton(resources.getText(R.string.ok)) { badQueryDialog, _ ->
                            searchDialog.dismiss()
                            badQueryDialog.dismiss()
                            createNewSearchDialog().show()
                        }
                        .show()
                }
                else {
                    startSearchActivity()
                }
            }
            .setNegativeButton(resources.getText(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
    }

    private fun showLinks() {
        Snackbar.make(binding.root, "Under working", Snackbar.LENGTH_SHORT).show()
        showLinksDialog(this, binding.pdfView, pdf)
    }

    private fun showBookmarks() {
        binding.progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.Default).launch {
            val bookmarks = binding.pdfView.tableOfContents.map { bookmark -> Bookmark(bookmark, level = 0) }
            Intent(this@MainActivity, BookmarksActivity::class.java).also { bookmarkIntent ->
                bookmarkIntent.putExtra(PDF.pdfBookmarksKey, Gson().toJson(bookmarks))
                withContext(Dispatchers.Main) {
                    startActivityForResult(bookmarkIntent, PDF.startBookmarksActivity)
                }
            }
        }
    }

    private fun goToPage() {
        // create EditText for input
        val inputLayout = LayoutInflater.from(this)
            .inflate(R.layout.only_integers_input_layout, null) as TextInputLayout
        inputLayout.hint = "Current page ${pdf.pageNumber + 1}/${pdf.length}"

        AlertDialog.Builder(this, R.style.MJDialogThemeLight)
            .setTitle(getString(R.string.go_to_page))
            .setView(inputLayout)
            .setPositiveButton(getString(R.string.go_to)) { dialog, _ ->
                val query = inputLayout.editText?.text.toString().lowercase().trim()

                // check if the user provided input
                if (query.isEmpty()) {
                    Toast.makeText(this, getString(R.string.no_input), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (query.isDigitsOnly())
                    binding.pdfView.jumpTo(query.toInt() - 1)

                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (pref.getTurnPageByVolumeButtons()) {
            when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_DOWN -> binding.pdfView.jumpTo(++pdf.pageNumber)
                KeyEvent.KEYCODE_VOLUME_UP -> binding.pdfView.jumpTo(--pdf.pageNumber)
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun navToTextMode() {
        Toast.makeText(this, "Text Mode is not available yet", Toast.LENGTH_SHORT).show()
        return

        if (!checkHasFile()) return

        if (!pdf.isExtractingTextFinished) {
            Toast.makeText(
                this,
                getString(R.string.app_still_extracting_text), Toast.LENGTH_LONG
            ).show()

            return
        }
        val intent = Intent(this, TextModeActivity::class.java)
        extras.putExtra(pdf.uri.toString(), pdf.text)
        intent.putExtra(Preferences.uriKey, pdf.uri.toString())
        intent.putExtra(Preferences.pdfLengthKey, pdf.length)
        startActivity(intent)
    }

    private fun showAdditionalOptions() {

        data class Item(val title: String, val icon: Int)

        val settingsMap = mapOf(
            AdditionalOptions.APP_SETTINGS to Item(getString(R.string.app_settings), R.drawable.ic_settings),
            AdditionalOptions.TEXT_MODE to Item(getString(R.string.text_mode_not_available), R.drawable.ic_text),
            AdditionalOptions.METADATA to Item(getString(R.string.file_metadata), R.drawable.meta_info),
            AdditionalOptions.ADVANCED_CONFIG to Item(
                getString(R.string.advanced_config),
                R.drawable.ic_display_settings
            ),
            AdditionalOptions.ABOUT to Item(getString(R.string.action_about), R.drawable.info_icon),
        )

        // create a dialog for additional options and set their functionalities

        // Custom Adapter for the dialog so we can use icons for the items
        val items = settingsMap.values.toTypedArray()
        val adapter: ListAdapter = object : ArrayAdapter<Item>(
            this,
            android.R.layout.select_dialog_item,
            android.R.id.text1,
            items
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                // Use super class to create the View
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById(android.R.id.text1) as TextView

                textView.setCompoundDrawablesWithIntrinsicBounds(items[position].icon, 0, 0, 0)
                textView.text = items[position].title
                textView.setTextColor(resources.getColor(R.color.topBarColor))

                val padding = (10 * resources.displayMetrics.density + 0.5f).toInt()
                textView.compoundDrawablePadding = padding
                return view
            }
        }

        AlertDialog.Builder(this, R.style.MJDialogThemeDark)
            .setTitle(getString(R.string.settings))
            .setAdapter(adapter) { dialog, item ->
                when (item) {
                    AdditionalOptions.APP_SETTINGS.ordinal -> {
                        navToAppSettings()
                    }
                    AdditionalOptions.TEXT_MODE.ordinal -> {
                        navToTextMode()
                    }
                    AdditionalOptions.METADATA.ordinal -> {
                        if (checkHasFile()) showMetaDialog(this, binding.pdfView.documentMeta)
                    }
                    AdditionalOptions.ADVANCED_CONFIG.ordinal -> {
                        showPartSizeDialog(this, pref)
                    }
                    AdditionalOptions.ABOUT.ordinal -> {
                        startActivity(navIntent(this, AboutActivity::class.java))
                    }
                }
                dialog.dismiss()
            }.show()
    }

    private fun checkHasFile(): Boolean {
        if (!pdf.hasFile()) {
            Snackbar.make(
                binding.root, getString(R.string.no_pdf_in_app),
                Snackbar.LENGTH_LONG
            ).show()
            return false
        }
        return true
    }

    private fun switchPdfTheme() {
        if (checkHasFile()) {
            pref.setPdfDarkTheme(!pref.getPdfDarkTheme())
            recreate()
        }
    }

    private fun screenShot(view: View): Bitmap? {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun takeScreenshot() {
        val now = DateFormat.format("yyyy_MM_dd-hh_mm_ss", Date())
        try {
            val fileName = "${pdf.name.removeSuffix(".pdf")} - ${now}.jpg"
            val imageFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName)

            fullScreenOptionsManager.showAllTemporarilyOrHide()
            val bitmap = screenShot(binding.pdfView) ?: return

            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, PDF.SCREENSHOT_IMAGE_QUALITY, outputStream)
            outputStream.flush()
            outputStream.close()

            val uri = saveImage(bitmap, fileName)
            Snackbar.make(binding.root, getString(R.string.screenshot_saved), Snackbar.LENGTH_SHORT).also {
                it.setAction(getString(R.string.share)) { shareFile(uri, FileType.IMAGE) }
                it.show()
            }
        } catch (e: Throwable) {
            // Several error may come out with file handling or DOM
            Toast.makeText(this, getString(R.string.failed_save_screenshot), Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun saveImage(bitmap: Bitmap, fileName: String): Uri? {
        val (fileOutputStream: OutputStream?, imageUri: Uri?) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/*")

            // e.g.     ~/Pictures/app_name/screenshot1.jpg
            contentValues.put(
                MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/${getString(R.string.mj_app_name)}/"
            )

            val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            Pair(imageUri?.let { contentResolver.openOutputStream(it) }, imageUri)
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
            val image = File(imagesDir, fileName)
            Pair(FileOutputStream(image), image.toUri())
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, PDF.SCREENSHOT_IMAGE_QUALITY, fileOutputStream)
        fileOutputStream?.close()
        return imageUri
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(PDF.uriKey, pdf.uri)
        outState.putString(PDF.fileHashKey, pdf.fileHash)
        outState.putInt(PDF.pageNumberKey, pdf.pageNumber)
        outState.putString(PDF.passwordKey, pdf.password)
        outState.putBoolean(PDF.isFullScreenToggledKey, pdf.isFullScreenToggled)
        outState.putFloat(PDF.zoomKey, binding.pdfView.zoom)
        outState.putBoolean(PDF.isExtractingTextFinishedKey, pdf.isExtractingTextFinished)
        super.onSaveInstanceState(outState)
    }

    private fun restoreInstanceState(savedState: Bundle) {
        pdf.uri = savedState.getParcelable(PDF.uriKey)
        pdf.fileHash = savedState.getString(PDF.fileHashKey)
        pdf.pageNumber = savedState.getInt(PDF.pageNumberKey)
        pdf.password = savedState.getString(PDF.passwordKey)
        pdf.isFullScreenToggled = savedState.getBoolean(PDF.isFullScreenToggledKey)
        pdf.zoom = savedState.getFloat(PDF.zoomKey)
        pdf.isExtractingTextFinished = savedState.getBoolean(PDF.isExtractingTextFinishedKey)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        binding.progressBar.visibility = View.GONE
        when (requestCode) {
            PDF.startBookmarksActivity -> {
                if (resultCode == PDF.BOOKMARK_RESULT_OK) {
                    val pageNumber = intent?.getIntExtra(PDF.chosenBookmarkKey, pdf.pageNumber)
                    pageNumber?.let { binding.pdfView.jumpTo(it) }
                }
            }
            PDF.startSearchActivity -> {
                if (resultCode == PDF.SEARCH_RESULT_OK) {
                    val searchResultJson = intent?.getStringExtra(PDF.searchResultKey) ?: return
                    val searchResultType = object : TypeToken<SearchResult>() {}.type
                    val searchResult = Gson().fromJson<SearchResult>(searchResultJson, searchResultType)

                    // highlight the result text
                    val succeeded = binding.pdfView.createHighlightText(
                        searchResult.pageNumber,
                        searchResult.originalIndex,
                        searchResult.inputEnd - searchResult.inputStart,
                        true
                    )

                    if (!succeeded) {
                        Toast.makeText(this, "Failed to highlight search result", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        binding.pdfView.resetZoom()         // it won't work if the user was zoomed in before searching
                        binding.pdfView.reloadPages()
                    }

                    // remove newlines and tabs in the Snackbar message
                    val resultText = searchResult.text
                        .replace("\n", " ")
                        .replace("\t", " ")

                    // show a snackbar with a button that will remove the highlight (it wills still be cached for a bit)
                    Snackbar.make(binding.root, "Result: $resultText" , Snackbar.LENGTH_INDEFINITE)
                        .setAction(getString(R.string.ok)) {
                            binding.pdfView.clearSearchResultsHighlight(searchResult.pageNumber)
                        }
                        .show()

                    binding.pdfView.jumpUsingPageNumber(searchResult.pageNumber)
                }
            }
        }
    }

}

enum class FileType { IMAGE, PDF }


/*
    * pdf.pageNumber && pdf.length:
        will be set by PDFView::onPageChange() -> setCurrentPage()

    * pdf.password:
        will be set by PDFView::onError() -> handleFileOpeningError() -> askForPdfPassword()
 */