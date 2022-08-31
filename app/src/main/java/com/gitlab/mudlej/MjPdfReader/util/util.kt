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

package com.gitlab.mudlej.MjPdfReader.util

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.gitlab.mudlej.MjPdfReader.BuildConfig
import com.gitlab.mudlej.MjPdfReader.R
import com.gitlab.mudlej.MjPdfReader.data.PDF
import com.gitlab.mudlej.MjPdfReader.ui.MainActivity
import java.io.*
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.math.min
import com.gitlab.mudlej.MjPdfReader.ui.MainActivity.*

fun openSelectedDocument(activity: MainActivity, pdf: PDF, selectedDocumentUri: Uri?) {
    if (selectedDocumentUri == null) return

    if (pdf.uri == null || selectedDocumentUri == pdf.uri) {
        pdf.uri = selectedDocumentUri
        activity.displayFromUri(pdf.uri)
    } else {
        val intent = Intent(activity, activity.javaClass)
        intent.data = selectedDocumentUri
        activity.startActivity(intent)
    }
}

fun computeHash(context: Context, pdf: PDF): String? {
    if (pdf.uri == null) return null
    try {
        val digester = MessageDigest.getInstance("MD5")
        if (pdf.downloadedPdf != null) {
            val size = min(PDF.HASH_SIZE, pdf.downloadedPdf!!.size)
            digester.update(pdf.downloadedPdf as ByteArray, 0, size)
        } else {
            val inputStream = context.contentResolver.openInputStream(pdf.uri as Uri) ?: return null
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
    } catch (e: SecurityException) {
        return null
    }
}

fun getFileName(context: Context, uri: Uri): String {
    var result: String? = null
    if (uri.scheme != null && uri.scheme == "content") {
        try {
            context.contentResolver.query(uri, null, null, null, null).use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    val indexDisplayName: Int = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (indexDisplayName != -1) result = cursor.getString(indexDisplayName)
                }
            }
        } catch (e: Exception) {
            Log.w("getFileName", context.getString(R.string.error_load_file_name), e)
        }
    }

    return result ?: uri.lastPathSegment ?: ""
}

fun emailIntent(emailAddress: String, subject: String, text: String): Intent {
    val email = Intent(Intent.ACTION_SENDTO)
    email.data = Uri.parse("mailto:$emailAddress")
    email.putExtra(Intent.EXTRA_SUBJECT, subject)
    email.putExtra(Intent.EXTRA_TEXT, text)
    return email
}

fun plainTextShareIntent(chooserTitle: String, text: String): Intent {
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "text/plain"
    intent.putExtra(Intent.EXTRA_TEXT, text)
    return Intent.createChooser(intent, chooserTitle)
}

fun fileShareIntent(chooserTitle: String, fileName: String, fileUri: Uri): Intent {
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "application/pdf"
    intent.putExtra(Intent.EXTRA_STREAM, fileUri)
    intent.clipData = ClipData(fileName, arrayOf("application/pdf"), ClipData.Item(fileUri))
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    return Intent.createChooser(intent, chooserTitle)
}

fun linkIntent(url: String?) = Intent(Intent.ACTION_VIEW, Uri.parse(url))

fun navIntent(context: Context, activity: Class<*>) = Intent(context, activity)

fun getAppVersion() = BuildConfig.VERSION_NAME

@Throws(IOException::class)
fun writeBytesToFile(directory: File, fileName: String, fileContent: ByteArray?) {
    val file = File(directory, fileName)
    FileOutputStream(file).use { stream -> stream.write(fileContent) }
}

fun canWriteToDownloadFolder(context: Context): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) true
    else ContextCompat.checkSelfPermission(context,
        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

@Throws(IOException::class)
fun readBytesToEnd(inputStream: InputStream): ByteArray? {
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(8 * 1024)
    var bytesRead: Int
    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
        output.write(buffer, 0, bytesRead)
    }
    return output.toByteArray()
}

class ExtendedDataHolder private constructor() {
    companion object {
        val instance = ExtendedDataHolder()
    }

    private val extras: MutableMap<String, Any> = HashMap()

    fun putExtra(name: String, obj: Any) { extras[name] = obj }
    fun getExtra(name: String): Any? = extras[name]
    fun hasExtra(name: String): Boolean = extras.containsKey(name)
    fun clear() = extras.clear()
}


fun putEditTextInLinearLayout(activity: MainActivity, searchInput: EditText, layout: LinearLayout) {
    val layoutParams = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
    )
    layoutParams.setMargins(16, 0, 16, 0)
    layout.layoutParams = layoutParams
    layout.gravity = Gravity.CENTER
    
    searchInput.maxLines = 1
    searchInput.inputType = InputType.TYPE_CLASS_TEXT
    searchInput.hint = activity.getString(R.string.enter_text_to_search)
    searchInput.layoutParams = layoutParams
    
    layout.addView(searchInput)
}

fun copyToClipboard(activity: MainActivity, label: String, text: String) {
    val clipboard: ClipboardManager = activity.getSystemService(Context.CLIPBOARD_SERVICE)
            as ClipboardManager
    val clip: ClipData = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
}

// ------------------------------ Coding Utils ------------------------------

fun ignoreCaseOpt(ignoreCase: Boolean) =
    if (ignoreCase) setOf(RegexOption.IGNORE_CASE) else emptySet()

fun String?.indexesOf(pat: String, ignoreCase: Boolean = true): List<Int> =
    Regex.escape(pat)       // to disable any special meaning of query's characters
        .toRegex(ignoreCaseOpt(ignoreCase))
        .findAll(this?: "")
        .map { it.range.first }
        .toList()

val File.size get() = if (!exists()) 0.0 else length().toDouble()
val File.sizeInKb get() = size / 1024
val File.sizeInMb get() = sizeInKb / 1024
val File.sizeInGb get() = sizeInMb / 1024
val File.sizeInTb get() = sizeInGb / 1024
