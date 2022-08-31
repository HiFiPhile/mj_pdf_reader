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
import android.net.Uri
import android.os.*
import android.print.PrintManager
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
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
import com.shockwave.pdfium.PdfPasswordException
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.FileNotFoundException
import java.io.IOException
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {
    enum class AdditionalOptions{ APP_SETTINGS, TEXT_MODE, METADATA, PRINT_FILE, ADVANCED_CONFIG }

    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding

    private val executor: Executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())
    private val tappingHandler = Handler(Looper.getMainLooper())

    private lateinit var pref: Preferences
    private lateinit var database: AppDatabase
    private val pdf = PDF()
    private val extras = ExtendedDataHolder.instance

    // TODO: remove this temporary variable (to workaround PdfBox-Android limitation)
    private var isPdfTooBig = false

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "-----------onCreate: ${pdf.name} ")
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // To avoid FileUriExposedException, (https://stackoverflow.com/questions/38200282/)
        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().build())

        // init
        pref = Preferences(PreferenceManager.getDefaultSharedPreferences(this))
        database = AppDatabase.getInstance(applicationContext)

        Constants.THUMBNAIL_RATIO = pref.getThumbnailRation()
        Constants.PART_SIZE = pref.getPartSize()

        // Show Into Activity and Features Dialog on the first install
        onFirstInstall()

        // Create PDF by restore it in case of activity restart OR open filer picker
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

    private fun onFirstInstall() {
        val isFirstRun = pref.getFirstInstall()
        if (isFirstRun) {
            startActivity(Intent(this, MainIntroActivity::class.java))
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
        pdf.sizeInMb = getSizeInMb(uri)
        title = pdf.name
        setTaskDescription(ActivityManager.TaskDescription(pdf.name))
        val scheme = uri.scheme
        if (scheme != null && scheme.contains("http")) {
            downloadOrShowDownloadedFile(uri)
        } else {
            initPdfViewAndLoad(binding.pdfView.fromUri(pdf.uri))

            // start extracting text in the background
            if (!pdf.isExtractingTextFinished) extractPdfText()
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
            .onLongPress { showPageTextDialog(this, pdf, pref) }
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

        // Show the page scroll handler for 3 seconds when the pdf is loaded then hide it.
        pdfView.performTap()
        tappingHandler.postDelayed({ hideButtons(pdfView.scrollHandle) },
            pref.getHideDelay().toLong())
    }

    private fun extractPdfText() {
        // --- check if file size is too big for PdfBox-Android
        Log.i(TAG, "extractPdfText: fileSize: ${pdf.sizeInMb}MB")
        if (pdf.sizeInMb > 50) {
            isPdfTooBig = true
            invalidateOptionsMenu()
            return
        }

        var document: PDDocument? = null
        val pdfStripper = PDFTextStripper()
        pdf.pagesText.clear()

        Executors.newSingleThreadExecutor().execute {
            // off UI thread
            try {
                document = PDDocument.load(contentResolver.openInputStream(pdf.uri as Uri))
                val pagesCount = document?.numberOfPages ?: 0

                for (i in 0..pagesCount) {      // <= intentional
                    pdfStripper.startPage = i
                    pdfStripper.endPage = i
                    pdf.pagesText[i] = pdfStripper.getText(document)
                }
                // back to UI thread
                handler.post {
                    Log.i(TAG, "extractPdfText: finished")
                    pdf.isExtractingTextFinished = true
                    invalidateOptionsMenu() // to update (search loading) in action bar
                    if (pref.getFinishedExtractionDialog()) {
                        Snackbar.make(
                            binding.root, getString(R.string.finished_extracting_text),
                            Snackbar.LENGTH_SHORT
                        )
                        .setAction(getString(R.string.search)) {
                            showSearchDialog(this, pdf, binding, handler)
                        }
                        .show()
                    }
                }
            }
            catch (e: IOException) {
                Log.e("PdfBox", "Exception thrown while stripping text", e)
            }
            finally {
                try {
                    document?.close()
                }
                catch (e: IOException) {
                    Log.e("PdfBox", "Exception thrown while closing document", e)
                }
            }
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun setButtonsFunctionalities() {
        binding.exitFullScreenButton.setOnClickListener {
            // set orientation to unspecified so that the screen rotation will be unlocked
            // this is because PORTRAIT / LANDSCAPE modes will lock the app in them
            toggleFullscreen(false)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            hideButtons(null)
        }
        binding.rotateScreenButton.setOnClickListener {
            requestedOrientation =
                if (pdf.isPortrait) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            pdf.togglePortrait()
        }
        binding.pickFile.setOnClickListener { pickFile() }
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
            Snackbar.make(findViewById(R.id.main),
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

    private fun hideButtons(handle: ScrollHandle?) {
        // stop any previous timer to hide them
        tappingHandler.removeCallbacksAndMessages(null)

        handle?.customHide()
        binding.exitFullScreenButton.visibility = View.INVISIBLE
        binding.rotateScreenButton.visibility = View.INVISIBLE
    }

    private fun toggleScrollAndButtonsVisibility(): Boolean {
        val handle = binding.pdfView.scrollHandle
        val exitButton = binding.exitFullScreenButton
        val rotateButton = binding.rotateScreenButton
        if (handle == null) {
            toggleButtonsVisibility()
            return true
        }

        // timer to hide them. This timer will be canceled in the else branch
        tappingHandler.removeCallbacksAndMessages(null)
        handle.cancelHideRunner()

        // set a new timer to hide
        tappingHandler.postDelayed({
            exitButton.visibility = View.INVISIBLE
            rotateButton.visibility = View.INVISIBLE
            handle.customHide()
        }, pref.getHideDelay().toLong())

        if (!handle.customShown()) {
            handle.customShow()
            if (pdf.isFullScreenToggled) {
                exitButton.visibility = View.VISIBLE
                rotateButton.visibility = View.VISIBLE
            }
        } else if (exitButton.visibility == View.GONE && pdf.isFullScreenToggled) {
            exitButton.visibility = View.VISIBLE
            rotateButton.visibility = View.VISIBLE
        } else {
            hideButtons(handle)
        }
        return true
    }

    private fun toggleButtonsVisibility() {
        if (!pdf.isFullScreenToggled) return
        val exitButton = binding.exitFullScreenButton
        val rotateButton = binding.rotateScreenButton
        if (exitButton.visibility == View.VISIBLE) {
            exitButton.visibility = View.INVISIBLE
            rotateButton.visibility = View.INVISIBLE
        } else {
            exitButton.visibility = View.VISIBLE
            rotateButton.visibility = View.VISIBLE
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
        title = pdf.getTitle()

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

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val textModeItem = menu.findItem(R.id.searchOption)
        // TODO: remove this workaround to prevent crashing
        if (isPdfTooBig)
            textModeItem.title = getString(R.string.no_search)
        else if (pdf.isExtractingTextFinished)
            textModeItem.title = getString(R.string.search_experimental)

        return super.onPrepareOptionsMenu(menu)
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
            R.id.bookmarksListOption -> showBookmarksDialog(this, binding.pdfView)
            R.id.searchOption -> showSearchDialog(this, pdf, binding, handler)
            R.id.copyPageText -> showPageTextDialog(this, pdf, pref, true)
            R.id.shareFileOption -> shareFile()
            R.id.additionalOptionsOption -> showAdditionalOptions()
            R.id.actionAboutOption -> {
                startActivity(navIntent(this, AboutActivity::class.java))
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
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
        if (!checkHasFile()) return

        // TODO: remove this workaround to prevent crashing
        if (isPdfTooBig) {
            Toast.makeText(this,
                getString(R.string.not_available_file_too_big), Toast.LENGTH_LONG).show()
            return
        }

        if (!pdf.isExtractingTextFinished) {
            Toast.makeText(this,
                getString(R.string.app_still_extracting_text), Toast.LENGTH_LONG).show()

            return
        }
        val intent = Intent(this, TextModeActivity::class.java)
        extras.putExtra(pdf.uri.toString(), pdf.pagesText)
        intent.putExtra(Preferences.uriKey, pdf.uri.toString())
        intent.putExtra(Preferences.pdfLengthKey, pdf.length)
        startActivity(intent)
    }

    private fun showAdditionalOptions() {
        // map an index to an option string
        val settingsMap = mapOf(
            AdditionalOptions.APP_SETTINGS to getString(R.string.app_settings),
            AdditionalOptions.TEXT_MODE to
                    if (pdf.isExtractingTextFinished) getString(R.string.text_mode_experimental)
                    else getString(R.string.text_mode_loading),
            AdditionalOptions.METADATA to getString(R.string.file_metadata),
            AdditionalOptions.PRINT_FILE to getString(R.string.print_file),
            AdditionalOptions.ADVANCED_CONFIG to getString(R.string.advanced_config)
        )

        // create a dialog for additional options and set their functionalities
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.settings))
            .setItems(settingsMap.values.toTypedArray()) { dialog, which ->
                when (which) {
                    AdditionalOptions.APP_SETTINGS.ordinal -> navToAppSettings()
                    AdditionalOptions.TEXT_MODE.ordinal -> navToTextMode()
                    AdditionalOptions.METADATA.ordinal ->
                        if (checkHasFile()) showMetaDialog(this, binding.pdfView.documentMeta)
                    AdditionalOptions.PRINT_FILE.ordinal -> printFile()
                    AdditionalOptions.ADVANCED_CONFIG.ordinal -> showPartSizeDialog(this, pref)
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