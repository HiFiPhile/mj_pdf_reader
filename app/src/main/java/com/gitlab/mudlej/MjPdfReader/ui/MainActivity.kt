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

package com.gitlab.mudlej.MjPdfReader.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.AssetFileDescriptor
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.*
import android.print.PrintManager
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.text.isDigitsOnly
import androidx.core.view.*
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.github.barteksc.pdfviewer.PDFView.Configurator
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.github.barteksc.pdfviewer.scroll.ScrollHandle
import com.github.barteksc.pdfviewer.util.Constants
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.gitlab.mudlej.MjPdfReader.PdfDocumentAdapter
import com.gitlab.mudlej.MjPdfReader.R
import com.gitlab.mudlej.MjPdfReader.data.AppDatabase
import com.gitlab.mudlej.MjPdfReader.data.PDF
import com.gitlab.mudlej.MjPdfReader.data.Preferences
import com.gitlab.mudlej.MjPdfReader.data.SavedLocation
import com.gitlab.mudlej.MjPdfReader.databinding.ActivityMainBinding
import com.gitlab.mudlej.MjPdfReader.databinding.PasswordDialogBinding
import com.gitlab.mudlej.MjPdfReader.util.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.shockwave.pdfium.PdfPasswordException
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.android.synthetic.main.activity_main.*
import java.io.FileNotFoundException
import java.io.IOException
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.math.absoluteValue
import kotlin.math.sign
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    enum class AdditionalOptions{ APP_SETTINGS, TEXT_MODE, METADATA, ADVANCED_CONFIG, ABOUT }

    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding

    private val executor: Executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())
    private val tappingHandler = Handler(Looper.getMainLooper())

    private lateinit var pref: Preferences
    private lateinit var database: AppDatabase
    private val pdf = PDF()
    private val extras = ExtendedDataHolder.instance

    lateinit var activityTitleTextView: TextView

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
        database = AppDatabase.getInstance(applicationContext)

        Constants.THUMBNAIL_RATIO = pref.getThumbnailRation()
        Constants.PART_SIZE = pref.getPartSize()

        // Show Into Activity and Features Dialog on the first install
        onFirstInstall()

        // Create PDF by restoring it in case of an activity restart OR open filer picker
        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState)
        }
        else {
            pdf.uri = intent.data
            if (pdf.uri == null) pickFile()
        }

        displayFromUri(pdf.uri)
        setButtonsFunctionalities()
        showAppFeaturesDialogOnFirstRun()
    }

    private fun setCustomActionBar() {
        val actionBar = supportActionBar
        // Disable the default and enable the custom
        actionBar?.setDisplayShowTitleEnabled(false)
        actionBar?.setDisplayShowCustomEnabled(true)

        val customView: View = layoutInflater.inflate(R.layout.actionbar_title, null)
        activityTitleTextView = customView.findViewById(R.id.actionbarTitle)

        // Change the font family (optional)
        activityTitleTextView.setTypeface(Typeface.SERIF)

        activityTitleTextView.setOnClickListener {
            val title = pdf.getTitle()
            if (title.isNotEmpty()) {
                Toast.makeText(this, title, Toast.LENGTH_LONG).show()
            }
        }
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
            documentPickerLauncher.launch(arrayOf(PDF.FILE_TYPE))
        } catch (e: ActivityNotFoundException) {
            // alert user that file manager not working
            Toast.makeText(this, R.string.toast_pick_file_error, Toast.LENGTH_LONG).show()
        }
    }

    fun displayFromUri(uri: Uri?) {
        if (uri == null) return

        pdf.name = getFileName(this, uri)
        //pdf.sizeInMb = getSizeInMb(uri)       // shouldn't be needed anymore
        activityTitleTextView.text = pdf.getTitle()

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

    private fun getSizeInMb(uri: Uri): Double {
        var fileDescriptor: AssetFileDescriptor? = null
        try {
            fileDescriptor = applicationContext.contentResolver.openAssetFileDescriptor(uri, "r")
        }
        catch (e: FileNotFoundException) {
            Log.e(TAG, "getSizeInMb: ${e.message}")
        }
        val fileSizeInBytes: Long = fileDescriptor?.length ?: 50
        return fileSizeInBytes.toDouble() / (1024 * 1024)
    }

    private fun initPdfViewAndLoad(viewConfigurator: Configurator) {
        // attempt to find a saved location for the pdf else assign zero
        if (pdf.pageNumber == 0) {
            executor.execute {
                // off UI thread
                pdf.fileHash = computeHash(this, pdf)
                pdf.pageNumber = database.savedLocationDao().findSavedPage(pdf.fileHash) ?: 0

                // back to UI thread
                handler.post {
                    initPdfViewAndLoad(viewConfigurator, pdf.pageNumber)
                }
            }
        }
        else initPdfViewAndLoad(viewConfigurator, pdf.pageNumber)
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
            .onPageChange { page: Int, pageCount: Int -> setCurrentPage(page, pageCount)}
            .enableAnnotationRendering(Preferences.annotationRenderingDefault)
            .enableAntialiasing(pref.getAntiAliasing())
            .onTap { toggleScrollAndButtonsVisibility() }
            .onLongPress { copyPageText(false) }
            .scrollHandle(DefaultScrollHandle(this))
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

        // hide page scroll handle if the pdf consists of only one page
        // it needs to be delayed so pdf.length is set
        Handler(mainLooper).postDelayed({
            if (pdf.length == 1)
                binding.pdfView.scrollHandle?.permanentHide()
        }, 750)

        // Show the page scroll handler for 3 seconds when the pdf is loaded then hide it.
        pdfView.performTap()
        tappingHandler.postDelayed({ hideButtonsAndHandle(pdfView.scrollHandle) },
            pref.getHideDelay().toLong())
    }

    private fun copyPageText(bypass: Boolean) {
        //showPageTextDialog(this, pdf, pref, true)
        val pageNumber = pdf.pageNumber + 1
        // don't extract the text if it's already extracted
        if (pdf.extractedPagesIndexes.value?.contains(pageNumber) != true) {
            extractPageText(pageNumber)
        }
        showCopyPageTextDialog(pageNumber ,this, pdf, pref, bypass)
    }

    private fun extractPageText(number: Int) {
        var document: PDDocument? = null
        val pdfStripper = PDFTextStripper()

        Executors.newSingleThreadExecutor().execute {
            // off UI thread
            try {
                document = PDDocument.load(contentResolver.openInputStream(pdf.uri as Uri))
                val pagesCount = document?.numberOfPages ?: 0

                if (pagesCount < 1 || number < 0 || number > pagesCount)
                    return@execute

                pdfStripper.startPage = number
                pdfStripper.endPage = number
                pdf.text[number] = pdfStripper.getText(document)

                // back to UI thread
                handler.post {
                    pdf.updatePagesTextLiveData(pdf.text)
                    pdf.updateExtractedPagesIndexesLiveData(pdf.text.keys)
                }
            }
            catch (e: IOException) {
                Log.e("PdfBox", "extractPageText($number): error while stripping text", e)
            }
            finally {
                try {
                    document?.close()
                }
                catch (e: IOException) {
                    Log.e("PdfBox", "extractPageText($number): error while closing document", e)
                }
            }
        }
    }

    private fun extractAllPagesText() {
        val pdfIndexes = (1..pdf.length).toSet()
        val extractedIndexes = pdf.extractedPagesIndexes.value ?: setOf()
        val indexesToExtract =  pdfIndexes - extractedIndexes

        Log.d(TAG, "extractAllPagesText: extractedIndexes: $extractedIndexes")
        Log.d(TAG, "extractAllPagesText: indexesToExtract: $indexesToExtract")
        var isCanceled = false

        if (indexesToExtract.isNotEmpty()) {
            //val progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
            val progressBarLayout = LayoutInflater.from(this).inflate(R.layout.extracting_data_layout, null) as ConstraintLayout
            val progressBar = progressBarLayout.get(0) as ProgressBar

            progressBar.max = pdf.length
            val title = getString(R.string.extracting_text_title)
            val extractingDialog = AlertDialog.Builder(this, R.style.MJDialogThemeLight)
                .setTitle(title)
                .setView(progressBarLayout)
                .setNegativeButton(getString(R.string.stop)) { _, _ -> isCanceled = true}
                .setPositiveButton(getString(R.string.hide)) { dialog, _ -> dialog.dismiss() }
                .create()

            //var counter = 1
            val iterator = indexesToExtract.iterator()
            extractPageText(iterator.next())
            lifecycleScope.launchWhenCreated {
                pdf.extractedPagesIndexes.observe(this@MainActivity) { indexes ->
                    if (isCanceled) return@observe

                    if (indexes.size == pdf.length) {
                        extractingDialog.dismiss()
                        showSearchDialog(this@MainActivity, pdf, binding, handler)
                        return@observe
                    }

                    Log.d(TAG, "extractAllPagesText: rate: ${indexes.size}/${pdf.length} ")
                    progressBar.progress = indexes.size
                    if (iterator.hasNext())
                        extractPageText(iterator.next())
                    extractingDialog.setTitle("$title (${indexes.size}/${pdf.length})")
                }
            }
            extractingDialog.show()
        }
        else {
            showSearchDialog(this, pdf, binding, handler)
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun setButtonsFunctionalities() {
        binding.apply {
            exitFullScreenButton.setOnClickListener {
                // set orientation to unspecified so that the screen rotation will be unlocked
                // this is because PORTRAIT / LANDSCAPE modes will lock the app in them
                toggleFullscreen(false)
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                hideButtonsAndHandle(null)

                // this WON'T give the brightness control back to the system. (that was not a good idea)
                // val brightness = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
                // updateBrightness(brightness)
            }
            rotateScreenButton.setOnClickListener {
                requestedOrientation =
                    if (pdf.isPortrait) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                pdf.togglePortrait()
            }
            brightnessButton.setOnClickListener {
                if (brightnessSeekBar.isVisible) {
                    brightnessSeekBar.visibility = View.GONE
                    brightnessPercentage.visibility = View.GONE
                }
                else {
                    brightnessSeekBar.visibility = View.VISIBLE
                    brightnessPercentage.visibility = View.VISIBLE
                }
            }
            autoScrollButton.setOnClickListener {
                if (toggleAutoScrollButton.isVisible) {
                    increaseScrollSpeedButton.visibility = View.GONE
                    decreaseScrollSpeedButton.visibility = View.GONE
                    reverseAutoScrollButton.visibility = View.GONE
                    toggleAutoScrollButton.visibility = View.GONE
                }
                else {
                    increaseScrollSpeedButton.visibility = View.VISIBLE
                    decreaseScrollSpeedButton.visibility = View.VISIBLE
                    reverseAutoScrollButton.visibility = View.VISIBLE
                    toggleAutoScrollButton.visibility = View.VISIBLE
                }
            }

            // init the seekbar
            val brightness = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
            brightnessSeekBar.progress = brightness
            brightnessPercentage.text = "$brightness%"

            brightnessSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onStopTrackingTouch(seekBar: SeekBar?) { }
                override fun onStartTrackingTouch(p0: SeekBar?) { }
                override fun  onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (seekBar == null) return
                    updateBrightness(progress)
                }
            })
            pickFile.setOnClickListener { pickFile() }

            var isAutoScrolling = false
            val delay = 1L
            val interval = 1
            var scrollBy = -interval
            val scrollingHandler = Handler(mainLooper)

            increaseScrollSpeedButton.setOnClickListener {
                    scrollBy = (scrollBy.absoluteValue + interval) *  scrollBy.sign
            }
            decreaseScrollSpeedButton.setOnClickListener {
                if (scrollBy.absoluteValue > interval)
                    scrollBy = (scrollBy.absoluteValue - interval) *  scrollBy.sign
            }
            reverseAutoScrollButton.setOnClickListener { scrollBy = -scrollBy }

            toggleAutoScrollButton.setOnClickListener {
                isAutoScrolling = !isAutoScrolling

                if (!isAutoScrolling) {
                    toggleAutoScrollButton.setImageResource(R.drawable.ic_start)
                    scrollingHandler.removeCallbacksAndMessages(null)
                    return@setOnClickListener
                }
                else {
                    toggleAutoScrollButton.setImageResource(R.drawable.ic_pause)
                }

                fun scroll() {
                    scrollingHandler.postDelayed({
                        pdfView.moveRelativeTo(0F, scrollBy.toFloat())
                        pdfView.loadPages()
                        //pdfView.scrollBy(0, scrollBy)
                        //mainLayout.scrollBy(0, scrollBy)

                        if (isAutoScrolling || pdf.pageNumber < pdf.length)
                            scroll()
                    }, delay)
                }
                scroll()
            }
        }
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
        if (pdf.isFullScreenToggled) toggleFullscreen(true)

        // Prompt the user to restore the previous zoom if there is one saved other than the default
        // pdfZoom != binding.pdfView.getZoom())   // doesn't work for some peculiar reason
        if (pdf.zoom != 1f) {
            Snackbar.make(findViewById(R.id.mainLayout),
                getString(R.string.ask_restore_zoom), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.restore)) {
                    binding.pdfView.zoomWithAnimation(pdf.zoom)
                }
                .show()
        }
        fixButtonsColor()
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
    }

    private fun shareFile() {
        val uri = pdf.uri
        if (uri == null) {
            checkHasFile()  // only to show the message
            return
        }
        val sharingIntent: Intent = 
            if (uri.scheme != null && uri.scheme!!.startsWith("http"))
                plainTextShareIntent(getString(R.string.share_file), pdf.uri.toString())
            else 
                fileShareIntent(getString(R.string.share_file), pdf.name, uri)
        
        startActivity(sharingIntent)
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

    private fun hideButtonsAndHandle(handle: ScrollHandle?) {
        // stop any previous timer to hide them
        tappingHandler.removeCallbacksAndMessages(null)

        handle?.customHide()
        binding.exitFullScreenButton.visibility = View.INVISIBLE
        binding.rotateScreenButton.visibility = View.INVISIBLE
        binding.brightnessButtonLayout.visibility = View.INVISIBLE
        binding.autoScrollLayout.visibility = View.INVISIBLE
    }

    private fun showFullScreenButtons() = changeFullScreenButtonsVisibility(true)
    private fun hideFullScreenButtons() = changeFullScreenButtonsVisibility(false)

    private fun changeFullScreenButtonsVisibility(isVisible: Boolean) {
        val visibility = if (isVisible) View.VISIBLE else View.GONE
        binding.exitFullScreenButton.visibility = visibility
        binding.rotateScreenButton.visibility = visibility
        binding.brightnessButtonLayout.visibility = visibility
        binding.autoScrollLayout.visibility = visibility
    }

    private fun toggleScrollAndButtonsVisibility(): Boolean {
        val handle = binding.pdfView.scrollHandle

        if (handle == null) {
            toggleButtonsVisibility()
            return true
        }

        // timer to hide them. This timer will be canceled in the else branch
        tappingHandler.removeCallbacksAndMessages(null)
        handle.cancelHideRunner()

        // set a new timer to hide
        tappingHandler.postDelayed({
            hideFullScreenButtons()
            handle.customHide()
        }, pref.getHideDelay().toLong())

        if (!handle.customShown()) {
            handle.customShow()
            if (pdf.isFullScreenToggled) {
                showFullScreenButtons()
            }
        }
        else if (binding.exitFullScreenButton.visibility == View.GONE && pdf.isFullScreenToggled) {
            showFullScreenButtons()
        }
        else {
            hideButtonsAndHandle(handle)
        }
        return true
    }

    private fun toggleButtonsVisibility() {
        if (!pdf.isFullScreenToggled) return

        val exitButton = binding.exitFullScreenButton
        val rotateButton = binding.rotateScreenButton
        val brightnessButton = binding.brightnessButton

        if (exitButton.visibility == View.VISIBLE) {
            exitButton.visibility = View.INVISIBLE
            rotateButton.visibility = View.INVISIBLE
            brightnessButtonLayout.visibility = View.INVISIBLE
        }
        else {
            exitButton.visibility = View.VISIBLE
            rotateButton.visibility = View.VISIBLE
            brightnessButtonLayout.visibility = View.VISIBLE
        }
    }

    private fun handleFileOpeningError(exception: Throwable) {
        if (exception is PdfPasswordException) {
            if (pdf.password != null) {
                Toast.makeText(this, R.string.wrong_password, Toast.LENGTH_SHORT).show()
                pdf.password = null // prevent the toast if the user rotates the screen
            }
            askForPdfPassword()
        } else if (couldNotOpenFileDueToMissingPermission(exception)) {
            readFileErrorPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            Toast.makeText(this, R.string.file_opening_error, Toast.LENGTH_LONG).show()
            Log.e(TAG, getString(R.string.file_opening_error), exception)
        }
    }

    private fun couldNotOpenFileDueToMissingPermission(e: Throwable): Boolean {
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED) return false
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

    private fun toggleFullscreen(fixFullScreen: Boolean) {
        val view: View = binding.pdfView
        if (!pdf.isFullScreenToggled || fixFullScreen) {
            supportActionBar?.hide()
            pdf.isFullScreenToggled = true
            view.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)

            // hide the scroll handle
            if (!fixFullScreen) {
                val handle = binding.pdfView.scrollHandle
                handle?.customHide()
            }

            // show how to dialog
            if (pref.getShowFeaturesDialog()) showHowToExitFullscreenDialog(this, pref)
        } else {
            supportActionBar?.show()
            pdf.isFullScreenToggled = false
            view.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }


//  private fun toggleFullscreen(fixFullScreen: Boolean) {
//        if (!pdf.isFullScreenToggled || fixFullScreen) {
//            supportActionBar?.hide()
//            pdf.isFullScreenToggled = true
//
//            hideSystemUI()
//            // hide the scroll handle
//            if (!fixFullScreen) {
//                val handle = binding.pdfView.scrollHandle
//                handle?.customHide()
//            }
//
//            // show how to dialog
//            if (pref.getShowFeaturesDialog()) showHowToExitFullscreenDialog(this, pref)
//        } else {
//            supportActionBar?.show()
//            pdf.isFullScreenToggled = false
//            showSystemUI()
//        }
//    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

//        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//            or View.SYSTEM_UI_FLAG_FULLSCREEN
//            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    private fun showSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, binding.root).show(WindowInsetsCompat.Type.systemBars())
//        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
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
            saveToDownloadPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
        settingsLauncher.launch(Intent(this, SettingsActivity::class.java))
    }

    private fun setCurrentPage(pageNumber: Int, pageCount: Int) {
        pdf.pageNumber = pageNumber             // I think this may need to be incremented
        pdf.setPageCount(pageCount)
        //title = pdf.getTitle()
        activityTitleTextView.text = pdf.getTitle()

        val hash = pdf.fileHash              // Don't want fileContentHash to change out from under us
        if (hash != null) executor.execute {    // off UI thread
            database.savedLocationDao().insert(SavedLocation(hash, pdf.pageNumber))
        }
    }

    private fun printFile() {
        if (checkHasFile()) {
            val mgr = getSystemService(Context.PRINT_SERVICE) as PrintManager
            mgr.print(pdf.name, PdfDocumentAdapter(this, pdf.uri), null)
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
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.fullscreenOption -> toggleFullscreen(false)
            R.id.switchThemeOption -> switchPdfTheme()
            R.id.openFileOption -> pickFile()
            R.id.copyPageText -> { copyPageText(true) }
            R.id.goToPageOption -> { goToPage() }
            R.id.bookmarksListOption -> showBookmarksDialog(this, binding.pdfView)
            R.id.searchOption -> extractAllPagesText()
            R.id.shareFileOption -> shareFile()
            R.id.printFileOption -> printFile()
            R.id.additionalOptionsOption -> showAdditionalOptions()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
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
            .setNegativeButton(getString(R.string.cancel)) {dialog, _ -> dialog.dismiss() }
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
            Toast.makeText(this,
                getString(R.string.app_still_extracting_text), Toast.LENGTH_LONG).show()

            return
        }
        val intent = Intent(this, TextModeActivity::class.java)
        extras.putExtra(pdf.uri.toString(), pdf.text)
        intent.putExtra(Preferences.uriKey, pdf.uri.toString())
        intent.putExtra(Preferences.pdfLengthKey, pdf.length)
        startActivity(intent)
    }

    private fun showAdditionalOptions() {
        // map an index to an option string
        val settingsMap = mapOf(
            AdditionalOptions.APP_SETTINGS to getString(R.string.app_settings),
            AdditionalOptions.TEXT_MODE to getString(R.string.text_mode_not_available),
            AdditionalOptions.METADATA to getString(R.string.file_metadata),
            AdditionalOptions.ADVANCED_CONFIG to getString(R.string.advanced_config),
            AdditionalOptions.ABOUT to getString(R.string.action_about)
        )

        // create a dialog for additional options and set their functionalities
        AlertDialog.Builder(this, R.style.MJDialogThemeDark)
            .setTitle(getString(R.string.settings))
            .setItems(settingsMap.values.toTypedArray()) { dialog, which ->
                when (which) {
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
            }
            .show()
    }

    private fun checkHasFile(): Boolean {
        if (!pdf.hasFile()) {
            Snackbar.make(binding.root, getString(R.string.no_pdf_in_app),
                Snackbar.LENGTH_LONG).show()
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

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(PDF.uriKey, pdf.uri)
        outState.putInt(PDF.pageNumberKey, pdf.pageNumber)
        outState.putString(PDF.passwordKey, pdf.password)
        outState.putBoolean(PDF.isFullScreenToggledKey, pdf.isFullScreenToggled)
        outState.putFloat(PDF.zoomKey, binding.pdfView.zoom)
        outState.putBoolean(PDF.isExtractingTextFinishedKey, pdf.isExtractingTextFinished)
        super.onSaveInstanceState(outState)
    }

    private fun restoreInstanceState(savedState: Bundle) {
        pdf.uri = savedState.getParcelable(PDF.uriKey)
        pdf.pageNumber = savedState.getInt(PDF.pageNumberKey)
        pdf.password = savedState.getString(PDF.passwordKey)
        pdf.isFullScreenToggled = savedState.getBoolean(PDF.isFullScreenToggledKey)
        pdf.zoom = savedState.getFloat(PDF.zoomKey)
        pdf.isExtractingTextFinished = savedState.getBoolean(PDF.isExtractingTextFinishedKey)
    }

    private val documentPickerLauncher = registerForActivityResult(OpenDocument()) {
            selectedDocumentUri: Uri? -> openSelectedDocument(this, pdf, selectedDocumentUri)
    }

    private val saveToDownloadPermissionLauncher = registerForActivityResult(RequestPermission()) {
            isPermissionGranted: Boolean -> saveDownloadedFileAfterPermissionRequest(isPermissionGranted)
    }

    private val readFileErrorPermissionLauncher = registerForActivityResult(RequestPermission()) {
            isPermissionGranted: Boolean -> restartAppIfGranted(isPermissionGranted)
    }

    private val settingsLauncher = registerForActivityResult(StartActivityForResult()) {
        displayFromUri(pdf.uri)
    }

}


/*
    * pdf.pageNumber && pdf.length:
        will be set by PDFView::onPageChange() -> setCurrentPage()

    * pdf.password:
        will be set by PDFView::onError() -> handleFileOpeningError() -> askForPdfPassword()
 */