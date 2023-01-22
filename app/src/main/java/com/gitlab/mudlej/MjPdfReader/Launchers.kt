package com.gitlab.mudlej.MjPdfReader

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.gitlab.mudlej.MjPdfReader.data.PDF
import com.gitlab.mudlej.MjPdfReader.ui.MainActivity
import com.gitlab.mudlej.MjPdfReader.util.openSelectedDocument

class Launcher(private val activity: MainActivity, private val pdf: PDF) {
    fun pdfPicker(): ActivityResultLauncher<Array<String>>
        = activity.registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            selectedDocumentUri: Uri? -> openSelectedDocument(activity, pdf, selectedDocumentUri)
        }

    fun saveToDownloadPermission(requestFunction:(Boolean) -> (Unit))
        = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            isPermissionGranted: Boolean -> requestFunction(isPermissionGranted)
        }

    fun readFileErrorPermission(requestFunction:(Boolean) -> (Unit))
        = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            isPermissionGranted: Boolean -> requestFunction(isPermissionGranted)
        }

    fun settings(requestFunction: (Uri?) -> Unit)
        = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            requestFunction(pdf.uri)
        }
}

class Launchers(
    val pdfPicker: ActivityResultLauncher<Array<String>>,
    val saveToDownloadPermission: ActivityResultLauncher<String>,
    val readFileErrorPermission: ActivityResultLauncher<String>,
    val settings: ActivityResultLauncher<Intent>,
)