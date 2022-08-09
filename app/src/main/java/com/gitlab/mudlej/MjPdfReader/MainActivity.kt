package com.gitlab.mudlej.MjPdfReader

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.preference.PreferenceManager
import android.print.PrintManager
import android.provider.OpenableColumns
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.DialogFragment
import com.github.barteksc.pdfviewer.PDFView.Configurator
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.github.barteksc.pdfviewer.scroll.ScrollHandle
import com.github.barteksc.pdfviewer.util.Constants
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.gitlab.mudlej.MjPdfReader.data.PDF
import com.gitlab.mudlej.MjPdfReader.data.Preferences
import com.gitlab.mudlej.MjPdfReader.databinding.ActivityMainBinding
import com.gitlab.mudlej.MjPdfReader.databinding.PasswordDialogBinding
import com.google.android.material.snackbar.Snackbar
import com.shockwave.pdfium.PdfPasswordException
import java.io.FileNotFoundException
import java.io.IOException
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.math.min

class MainActivity() : AppCompatActivity() {
    private var viewBinding: ActivityMainBinding? = null

    private val executor: Executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())
    private val tappingHandler = Handler()

    private var mgr: PrintManager? = null
    private var appDb: AppDatabase? = null
    private var pref: Preferences? = null
    private val pdf = PDF()

    private var downloadedPdfFileContent: ByteArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding!!.root)
        Constants.THUMBNAIL_RATIO = 1f

        // Workaround for https://stackoverflow.com/questions/38200282/
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        pref = Preferences(PreferenceManager.getDefaultSharedPreferences(this))
        mgr = getSystemService(Context.PRINT_SERVICE) as PrintManager
        appDb = AppDatabase.getInstance(applicationContext)
        onFirstInstall()
        onFirstUpdate()
        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState)
        } else {
            pdf.uri = intent.data
            if (pdf.uri == null) pickFile()
        }
        displayFromUri(pdf.uri)
        setButtonsFunctionalities()
        showAppFeaturesDialogOnFirstRun()
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun setButtonsFunctionalities() {
        viewBinding!!.exitFullScreenButton.setOnClickListener { view ->
            // set orientation to unspecified so that the screen rotation will be unlocked
            // this is because PORTRAIT / LANDSCAPE modes will lock the app in them
            toggleFullscreen(false)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            hideButtons(null)
        }
        viewBinding!!.rotateScreenButton.setOnClickListener { view ->
            requestedOrientation =
                if (pdf.isPortrait) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            pdf.togglePortrait()
        }
        viewBinding!!.pickFile.setOnClickListener { view -> pickFile() }
    }

    public override fun onResume() {
        super.onResume()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (pref!!.getScreenOn()) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // restore the full screen mode if was toggled On
        if (pdf.isFullScreenToggled) toggleFullscreen(true)

        // Prompt the user to restore the previous zoom if there is one saved other than the default
        // pdfZoom != viewBinding.pdfView.getZoom())   // doesn't work for some peculiar reason
        if (pdf.zoom != 1f) {
            Snackbar.make(
                findViewById(R.id.main),
                getString(R.string.ask_restore_zoom), Snackbar.LENGTH_LONG
            )
                .setAction(getString(R.string.restore)) { _ ->
                    viewBinding!!.pdfView.zoomWithAnimation(pdf.zoom)
                }
                .show()
        }
        fixButtonsColor()

        // if there data in the pdf source variable (local path or url), hide the pickFile Button
        if (pdf.uri != null) viewBinding!!.pickFile.visibility = View.GONE
    }

    private fun fixButtonsColor() {
        // changes buttons color
        val color = if (pref!!.getPdfDarkTheme()) R.color.bright else R.color.dark
        DrawableCompat.setTint(
            DrawableCompat.wrap(viewBinding!!.exitFullScreenImage.drawable),
            ContextCompat.getColor(this, color)
        )
        DrawableCompat.setTint(
            DrawableCompat.wrap(viewBinding!!.rotateScreenImage.drawable),
            ContextCompat.getColor(this, color)
        )
    }

    private fun onFirstInstall() {
        val isFirstRun = pref!!.getFirstInstall()
        if (isFirstRun) {
            startActivity(Intent(this, MainIntroActivity::class.java))
            pref!!.setFirstInstall(false)
            pref!!.setShowFeaturesDialog(true)
        }
    }

    private fun onFirstUpdate() {
        val isFirstRun = pref!!.getAppVersion()
        if (isFirstRun) pref!!.setAppVersion(false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(PDF.uriKey, pdf.uri)
        outState.putInt(PDF.pageNumberKey, pdf.pageNumber)
        outState.putString(PDF.passwordKey, pdf.password)
        outState.putBoolean(PDF.isFullScreenToggledKey, pdf.isFullScreenToggled)
        outState.putFloat(PDF.zoomKey, viewBinding!!.pdfView.zoom)
        super.onSaveInstanceState(outState)
    }

    private fun restoreInstanceState(savedState: Bundle) {
        pdf.uri = savedState.getParcelable(PDF.uriKey)
        pdf.pageNumber = savedState.getInt(PDF.pageNumberKey)
        pdf.password = savedState.getString(PDF.passwordKey)
        pdf.isFullScreenToggled = savedState.getBoolean(PDF.isFullScreenToggledKey)
        pdf.zoom = savedState.getFloat(PDF.zoomKey)
    }

    fun shareFile() {
        val sharingIntent: Intent
        if (pdf.uri == null) return  // notify user?
        if (pdf.uri!!.scheme != null && pdf.uri!!.scheme!!.startsWith("http")) sharingIntent =
            Utils.plainTextShareIntent(
                getString(R.string.share),
                pdf.uri.toString()
            ) else sharingIntent =
            Utils.fileShareIntent(getString(R.string.share), pdf.name, pdf.uri)
        startActivity(sharingIntent)
    }

    private fun openSelectedDocument(selectedDocumentUri: Uri?) {
        if (selectedDocumentUri == null) return
        if (pdf.uri == null || selectedDocumentUri == pdf.uri) {
            pdf.uri = selectedDocumentUri
            displayFromUri(pdf.uri)
        } else {
            val intent = Intent(this, javaClass)
            intent.data = selectedDocumentUri
            startActivity(intent)
        }
    }

    private fun pickFile() {
        try {
            documentPickerLauncher.launch(arrayOf(PDF.FILE_TYPE))
        } catch (e: ActivityNotFoundException) {
            //alert user that file manager not working
            Toast.makeText(this, R.string.toast_pick_file_error, Toast.LENGTH_SHORT).show()
        }
    }

    fun computeHash(): String? {
        if (pdf.uri == null || downloadedPdfFileContent == null) return null
        try {
            val digester = MessageDigest.getInstance("MD5")
            if (downloadedPdfFileContent != null) {
                val size = min(PDF.HASH_SIZE, downloadedPdfFileContent!!.size)
                digester.update(downloadedPdfFileContent as ByteArray, 0, size)
            } else {
                val inputStream = contentResolver.openInputStream(pdf.uri as Uri) ?: return null
                val buffer = ByteArray(PDF.HASH_SIZE)
                val amountRead = inputStream.read(buffer)
                if (amountRead == -1) {
                    return null
                }
                digester.update(buffer, 0, amountRead)
            }
            return String.format("%032x", BigInteger(1, digester.digest()))
        } catch (e: NoSuchAlgorithmException) {
            return null
        } catch (e: IOException) {
            return null
        }
    }

    fun configurePdfViewAndLoad(viewConfigurator: Configurator) {
        if (pdf.pageNumber == 0) { // attempt to find a saved location
            executor.execute { // off UI thread
                pdf.contentHash = computeHash()
                handler.post { // back on UI thread
                    configurePdfViewAndLoadWithPageNumber(
                        viewConfigurator,
                        (if (pdf.contentHash == null) Integer.valueOf(0)
                        else appDb!!.savedLocationDao().findSavedPage(pdf.contentHash)) ?: 0
                    )
                }
            }
        } else {
            configurePdfViewAndLoadWithPageNumber(viewConfigurator, pdf.pageNumber)
        }
    }

    fun configurePdfViewAndLoadWithPageNumber(viewConfigurator: Configurator, pageNum: Int) {
        val pdfView = viewBinding!!.pdfView
        configureTheme()
        pdfView.useBestQuality(pref!!.getHighQuality())
        pdfView.minZoom = Preferences.minZoomDefault
        pdfView.midZoom = Preferences.midZoomDefault
        pdfView.maxZoom = Preferences.maxZoomDefault
        pdfView.zoomTo(pdf.zoom)

        viewConfigurator
            .defaultPage(pageNum)
            .onPageChange { page: Int, pageCount: Int -> setCurrentPage(page, pageCount)}
            .enableAnnotationRendering(Preferences.annotationRenderingDefault)
            .enableAntialiasing(pref!!.getAntiAliasing())
            .onTap { event: MotionEvent -> toggleScrollAndButtonsVisibility(event) }
            .scrollHandle(DefaultScrollHandle(this))
            .spacing(Preferences.spacingDefault)
            .onError { exception: Throwable -> handleFileOpeningError(exception) }
            .onPageError { page: Int, error: Throwable -> reportLoadPageError(page,error) }
            .pageFitPolicy(FitPolicy.WIDTH)
            .password(pdf.password)
            .swipeHorizontal(pref!!.getHorizontalScroll())
            .autoSpacing(pref!!.getHorizontalScroll())
            .pageSnap(pref!!.getPageSnap())
            .pageFling(pref!!.getPageFling())
            .nightMode(pref!!.getPdfDarkTheme())
            .load()

        // Show the page scroll handler for 3 seconds when the pdf is loaded then hide it.
        pdfView.performTap()
        tappingHandler.postDelayed(
            { hideButtons(pdfView.getScrollHandle()) },
            pref!!.getHideDelay().toLong()
        )
    }

    private fun configureTheme() {
        val pdfView = viewBinding!!.pdfView

        // set background color behind pages
        if (!pref!!.getPdfDarkTheme()) pdfView.setBackgroundColor(Preferences.pdfDarkBackgroundColor) else pdfView.setBackgroundColor(
            Preferences.pdfLightBackgroundColor
        )
        if (pref!!.getAppFollowSystemTheme()) {
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
        /* TODO:
             the below removeCallbacksAndMessages will delete the timer for handle to be hidden
             which will cause the handle to be shown until the user taps the screen again */
        // stop any previous timer to hide them
        tappingHandler.removeCallbacksAndMessages(null)

//        if (handle != null) handle.activateHandlerHideDelayed();
        handle?.customHide()
        viewBinding!!.exitFullScreenButton.visibility = View.INVISIBLE
        viewBinding!!.rotateScreenButton.visibility = View.INVISIBLE
    }

    private fun toggleScrollAndButtonsVisibility(event: MotionEvent): Boolean {
        val handle = viewBinding!!.pdfView.scrollHandle
        val exitButton = viewBinding!!.exitFullScreenButton
        val rotateButton = viewBinding!!.rotateScreenButton
        if (handle == null) {
            toggleButtonsVisibility()
            return true
        }

        // timer to hide them. This timer will be canceled in the else branch
        tappingHandler.removeCallbacksAndMessages(null)
        handle.cancelHideRunner()

        // set a new timer to hide
        tappingHandler.postDelayed({
            exitButton.setVisibility(View.INVISIBLE)
            rotateButton.setVisibility(View.INVISIBLE)
            handle.customHide()
        }, pref!!.getHideDelay().toLong())
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
        val exitButton = viewBinding!!.exitFullScreenButton
        val rotateButton = viewBinding!!.rotateScreenButton
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
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) return false
        val exceptionMessage = e.message
        return e is FileNotFoundException && exceptionMessage != null && exceptionMessage.contains(
            getString(R.string.permission_denied)
        )
    }

    private fun restartAppIfGranted(isPermissionGranted: Boolean) {
        if (isPermissionGranted) {
            // This is a quick and dirty way to make the system restart the current activity *and the current app process*.
            // This is needed because on Android 6 storage permission grants do not take effect until
            // the app process is restarted. // Mudelj: Why not just user recreate()?
            System.exit(0)
        } else {
            Toast.makeText(this, R.string.file_opening_error, Toast.LENGTH_LONG).show()
        }
    }

    private fun toggleFullscreen(fixFullScreen: Boolean) {
        val view: View = viewBinding!!.pdfView
        if (!pdf.isFullScreenToggled || fixFullScreen) {
            supportActionBar?.hide()
            pdf.isFullScreenToggled = true
            view.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)

            // hide the scroll handle
            if (!fixFullScreen) {
                val handle = viewBinding!!.pdfView.scrollHandle
                handle?.customHide()
            }

            // show how to dialog
            if (pref!!.getShowFeaturesDialog()) showHowToExitFullscreenDialog()
        } else {
            supportActionBar?.show()
            pdf.isFullScreenToggled = false
            view.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    fun displayFromUri(uri: Uri?) {
        if (uri == null) {
            return
        }
        pdf.name = getFileName(uri)!!
        title = pdf.name
        setTaskDescription(ActivityManager.TaskDescription(pdf.name))
        val scheme = uri.scheme
        if (scheme != null && scheme.contains("http")) {
            downloadOrShowDownloadedFile(uri)
        } else {
            configurePdfViewAndLoad(viewBinding!!.pdfView.fromUri(uri))
        }
    }

    private fun downloadOrShowDownloadedFile(uri: Uri) {
        if (downloadedPdfFileContent == null) {
            downloadedPdfFileContent = lastCustomNonConfigurationInstance as ByteArray?
        }
        if (downloadedPdfFileContent != null) {
            configurePdfViewAndLoad(viewBinding!!.pdfView.fromBytes(downloadedPdfFileContent))
        } else {
            // we will get the pdf asynchronously with the DownloadPDFFile object
            viewBinding!!.progressBar.visibility = View.VISIBLE
            val downloadPDFFile = DownloadPDFFile(this)
            downloadPDFFile.execute(uri.toString())
        }
    }

    override fun onRetainCustomNonConfigurationInstance(): Any? {
        return downloadedPdfFileContent
    }

    fun hideProgressBar() {
        viewBinding!!.progressBar.visibility = View.GONE
    }

    fun saveToFileAndDisplay(pdfFileContent: ByteArray?) {
        downloadedPdfFileContent = pdfFileContent
        saveToDownloadFolderIfAllowed(pdfFileContent)
        configurePdfViewAndLoad(viewBinding!!.pdfView.fromBytes(pdfFileContent))
    }

    private fun saveToDownloadFolderIfAllowed(fileContent: ByteArray?) {
        if (Utils.canWriteToDownloadFolder(this)) {
            trySaveToDownloadFolder(fileContent, false)
        } else {
            saveToDownloadPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun trySaveToDownloadFolder(fileContent: ByteArray?, showSuccessMessage: Boolean) {
        try {
            val downloadDirectory =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            Utils.writeBytesToFile(downloadDirectory, pdf.name, fileContent)
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
            trySaveToDownloadFolder(downloadedPdfFileContent, true)
        } else {
            Toast.makeText(this, R.string.save_to_download_failed, Toast.LENGTH_SHORT).show()
        }
    }

    fun navToSettings() {
        settingsLauncher.launch(Intent(this, SettingsActivity::class.java))
    }

    private fun setCurrentPage(page: Int, pageCount: Int) {
        val hash = pdf.contentHash // Don't want fileContentHash to change out from under us
        if (hash != null) executor.execute { // off UI thread
            appDb!!.savedLocationDao().insert(SavedLocation(hash, pdf.pageNumber))
        }
        pdf.pageNumber = page
        pdf.setPageCount(viewBinding!!.pdfView.pageCount)
        title = pdf.getTitle()
    }

    fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme != null && uri.scheme == "content") {
            try {
                contentResolver.query(uri, null, null, null, null).use { cursor ->
                    if (cursor != null && cursor.moveToFirst()) {
                        val indexDisplayName: Int =
                            cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (indexDisplayName != -1) {
                            result = cursor.getString(indexDisplayName)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, getString(R.string.error_load_file_name), e)
            }
        }
        if (result == null) {
            result = uri.lastPathSegment
        }
        return result
    }

    private fun printDocument() {
        mgr!!.print(pdf.name, PdfDocumentAdapter(this, pdf.uri), null)
    }

    fun askForPdfPassword() {
        val dialogBinding = PasswordDialogBinding.inflate(layoutInflater)
        val alert = AlertDialog.Builder(this)
            .setTitle(R.string.protected_pdf)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.ok) { _, _ ->
                pdf.password = dialogBinding.passwordInput.getText().toString()
                displayFromUri(pdf.uri)
            }
            .setIcon(R.drawable.lock_icon)
            .create()
        alert.setCanceledOnTouchOutside(false)
        alert.show()
    }

    fun showPdfMetaDialog() {
        val meta = viewBinding!!.pdfView.documentMeta
        if (meta != null) {
            val dialogArgs = Bundle()
            dialogArgs.putString(PdfMetaDialog.TITLE_ARGUMENT, meta.title)
            dialogArgs.putString(PdfMetaDialog.AUTHOR_ARGUMENT, meta.author)
            dialogArgs.putString(PdfMetaDialog.CREATION_DATE_ARGUMENT, meta.creationDate)
            val dialog: DialogFragment = PdfMetaDialog()
            dialog.arguments = dialogArgs
            dialog.show(supportFragmentManager, "meta_dialog")
        }
    }

    fun showHowToExitFullscreenDialog() {
        AlertDialog.Builder(this)
            .setTitle(resources.getString(R.string.exit_fullscreen_title))
            .setMessage(resources.getString(R.string.exit_fullscreen_message))
            .setPositiveButton(resources.getString(R.string.exit_fullscreen_positive)) { _, _ ->
                pref!!.setShowFeaturesDialog(false)
            }
            .setNegativeButton(resources.getString(R.string.ok)) {
                    dialog: DialogInterface, _ -> dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun showAppFeaturesDialogOnFirstRun() {
        if (pref!!.getShowFeaturesDialog()) {
            Handler().postDelayed({ Utils.showAppFeaturesDialog(this) }, 500)
            pref!!.setShowFeaturesDialog(false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_about ->
                startActivity(Utils.navIntent(this, AboutActivity::class.java))
            R.id.theme ->
                startActivity(Utils.navIntent(applicationContext, SettingsActivity::class.java))
            R.id.settings -> navToSettings()
            R.id.share_file -> if (pdf.uri != null) shareFile()
            R.id.fullscreen_option -> toggleFullscreen(false)
            R.id.switch_theme -> switchPdfTheme()
            R.id.open_file -> pickFile()
            R.id.meta_data -> if (pdf.uri != null) showPdfMetaDialog()
            R.id.print_file -> printDocument()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun switchPdfTheme() {
        pref!!.setPdfDarkTheme(!pref!!.getPdfDarkTheme())
        //configurePdfViewAndLoadWithPageNumber(viewBinding.pdfView.fromUri(uri), pageNumber);
        recreate()
    }

    class PdfMetaDialog() : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(requireContext())
            return builder.setTitle(R.string.meta)
                .setMessage(
                    getString(R.string.pdf_title, requireArguments().getString(TITLE_ARGUMENT)) + "\n" +
                            getString(
                                R.string.pdf_author,
                                requireArguments().getString(AUTHOR_ARGUMENT)
                            ) + "\n" +
                            getString(
                                R.string.pdf_creation_date,
                                requireArguments().getString(CREATION_DATE_ARGUMENT)
                            )
                )
                .setPositiveButton(R.string.ok) { dialog, which -> }
                .setIcon(R.drawable.info_icon)
                .create()
        }

        companion object {
            const val TITLE_ARGUMENT = "title"
            const val AUTHOR_ARGUMENT = "author"
            const val CREATION_DATE_ARGUMENT = "creation_date"
        }
    }

    private val documentPickerLauncher = registerForActivityResult(OpenDocument()) {
            selectedDocumentUri: Uri? -> openSelectedDocument(selectedDocumentUri)
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

    companion object {
        private val TAG = "MainActivity"
    }
}