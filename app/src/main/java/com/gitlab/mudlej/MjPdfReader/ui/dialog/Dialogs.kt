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

import android.app.Dialog
import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.github.barteksc.pdfviewer.PDFView
import com.gitlab.mudlej.MjPdfReader.BuildConfig
import com.gitlab.mudlej.MjPdfReader.R
import com.gitlab.mudlej.MjPdfReader.data.PDF
import com.gitlab.mudlej.MjPdfReader.data.Preferences
import com.gitlab.mudlej.MjPdfReader.databinding.ActivityMainBinding
import com.gitlab.mudlej.MjPdfReader.databinding.PasswordDialogBinding
import com.gitlab.mudlej.MjPdfReader.ui.main.MainActivity
import com.gitlab.mudlej.MjPdfReader.ui.text_mode.TextModeActivity
import com.gitlab.mudlej.MjPdfReader.util.copyToClipboard
import com.gitlab.mudlej.MjPdfReader.util.indexesOf
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.shockwave.pdfium.PdfDocument
import java.util.concurrent.Executors

private const val TAG = "Dialogs"

fun showAppFeaturesDialog(context: Context) {
    val end = "\n\n"
    val dialog = AlertDialog.Builder(context, R.style.MJDialogThemeLight)
        .setTitle("${context.resources.getString(R.string.mj_app_name)} ${BuildConfig.VERSION_NAME} Features")
        .setMessage(
            "* Fast & smooth experience." + end +
            "* Minimalist & simple user interface." + end +
            "* Remembers the last opened page." + end +
            "* Dark mode for the app and the PDF." + end +
            "* True full screen with hidable buttons." + end +
            "* Search the PDF file. (experimental)" + end +
            "* Text mode to view PDFs like E-readers. (experimental)" + end +
            "* An option to keep the screen on." + end +
            "* Open online PDFs through links." + end +
            "* Share & print PDFs." + end +
            "* Open multiple PDFs." + end +
            "* FOSS and totally private. (see About)."
        )
        .setPositiveButton(context.resources.getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
        .create()

    try {
        dialog.show()
    }
    catch (e: Throwable) {
        Log.e(TAG, "showAppFeaturesDialog: Error showing the dialog.(${e.message})")
    }
}

fun showMetaDialog(context: Context, meta: PdfDocument.Meta) {
    AlertDialog.Builder(context, R.style.MJDialogThemeLight)
        .setTitle(R.string.metadata)
        .setMessage(
            "${context.getString(R.string.pdf_title)}: ${meta.title}\n" +
            "${context.getString(R.string.pdf_author)}: ${meta.author}\n" +
            "${context.getString(R.string.pdf_creation_date)}: ${meta.creationDate.format()}\n"
        )
        .setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
        .setIcon(R.drawable.info_icon)
        .create()
        .show()
}

fun showHowToExitFullscreenDialog(context: Context, pref: Preferences) {
    AlertDialog.Builder(context, R.style.MJDialogThemeLight)
        .setTitle(context.getString(R.string.exit_fullscreen_title))
        .setMessage(context.getString(R.string.exit_fullscreen_message))
        .setPositiveButton(context.getString(R.string.exit_fullscreen_positive)) { _, _ ->
            pref.setShowFeaturesDialog(false)
        }
        .setNegativeButton(context.getString(R.string.ok)) {
                dialog: DialogInterface, _ -> dialog.dismiss()
        }
        .create()
        .show()
}

fun showAskForPasswordDialog(
    context: Context,
    pdf: PDF,
    dialogBinding: PasswordDialogBinding,
    displayFunc: (Uri?) -> Unit)
{
    val alert = AlertDialog.Builder(context, R.style.MJDialogThemeLight)
        .setTitle(R.string.protected_pdf)
        .setView(dialogBinding.root)
        .setIcon(R.drawable.lock_icon)
        .setPositiveButton(R.string.ok) { _, _ ->
            pdf.password = dialogBinding.passwordInput.text.toString()
            displayFunc(pdf.uri)
        }
        .create()

    alert.setCanceledOnTouchOutside(false)
    alert.show()
}

fun showPartSizeDialog(activity: MainActivity, pref: Preferences) {
    // min values for the seekbars or sliders
    val minPartSize = Preferences.minPartSize
    val minMaxZoom = Preferences.minMaxZoom

    // create dialog layout
    val dialog = Dialog(activity)
    val inflater = activity.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val layout: View = inflater.inflate(R.layout.advanced_dialog,
        activity.findViewById(R.id.partSizeSeekbar))
    dialog.setContentView(layout)

    // set partSize TextView and Seekbar
    val partSizeText = layout.findViewById(R.id.partSizeText) as TextView
    partSizeText.text = pref.getPartSize().toInt().toString()

    val partSizeBar = layout.findViewById(R.id.partSizeSeekbar) as SeekBar
    partSizeBar.max = Preferences.maxPartSize.toInt()
    partSizeBar.progress = pref.getPartSize().toInt()
    partSizeBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            partSizeText.text = if(p1 > minPartSize) p1.toString() else minPartSize.toString()
        }
        override fun onStartTrackingTouch(p0: SeekBar?) {}
        override fun onStopTrackingTouch(p0: SeekBar?) {}
    })

    // set maxZoom TextView and Seekbar
    val maxZoomText = layout.findViewById(R.id.maxZoomText) as TextView
    maxZoomText.text = pref.getMaxZoom().toInt().toString()

    val maxZoomBar = layout.findViewById(R.id.maxZoomSeekbar) as SeekBar
    maxZoomBar.max = Preferences.maxMaxZoom.toInt()
    maxZoomBar.progress = pref.getMaxZoom().toInt()
    maxZoomBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            maxZoomText.text = if(p1 > minMaxZoom) p1.toString() else minMaxZoom.toString()
        }
        override fun onStartTrackingTouch(p0: SeekBar?) {}
        override fun onStopTrackingTouch(p0: SeekBar?) {}
    })

    // set buttons functionalities
    val applyButton = layout.findViewById(R.id.applyButton) as Button
    applyButton.setOnClickListener {
        pref.setPartSize(partSizeText.text.toString().toFloat())
        pref.setMaxZoom(maxZoomText.text.toString().toFloat())
        activity.recreate()
    }

    val cancelButton = layout.findViewById(R.id.cancelButton) as Button
    cancelButton.setOnClickListener {
        dialog.dismiss()
    }

    val resetButton = layout.findViewById(R.id.resetButton) as Button
    resetButton.setOnClickListener {
        pref.setPartSize(Preferences.partSizeDefault)
        pref.setMaxZoom(Preferences.maxZoomDefault)
        activity.recreate()
    }

    dialog.show()
}

fun showBookmarksDialog(activity: MainActivity, pdfView: PDFView) {
    // get bookmarks or set an appropriate message for the user
    var bookmarks = pdfView.tableOfContents.map { "${it.title} - P${it.pageIdx + 1}" }

    if (bookmarks.isEmpty()) bookmarks = listOf(activity.getString(R.string.no_bookmarks))

    // create and show the bookmarks dialog
    AlertDialog.Builder(activity, R.style.MJDialogThemeLight)
        .setTitle(activity.getString(R.string.bookmarks))
        .setItems(bookmarks.toTypedArray()) { dialog, which ->
            if (pdfView.tableOfContents.isEmpty()) return@setItems

            val page = pdfView.tableOfContents[which].pageIdx
            pdfView.jumpTo(page.toInt())
            dialog.dismiss()
        }
        .show()
}

fun showLinksDialog(activity: MainActivity, pdfView: PDFView, pdf: PDF) {
    val originalPageNumber = pdf.pageNumber
    val links = mutableListOf<PdfDocument.Link?>()
    // This is not working because getLinks(i) can only
    // see links in pages the pdfView visited and stayed there for a bit
    for (i in 0..pdf.length) {
        pdfView.jumpTo(i)
        pdfView.loadPages()
    }
    for (i in 0..pdf.length) {
        //pdfView.jumpTo(i)
        Log.d(TAG, "showLinksDialog: pdfView.getLinks($i).size.: ${pdfView.getLinks(i)?.size}")
        pdfView.getLinks(i)?.let {
            it.forEach { link -> if (link.uri != null) links.add(link)}
        }
    }

    pdfView.jumpTo(originalPageNumber)
    Log.d(TAG, "showLinksDialog: links.size: ${links.size}")
    var linksLabels = links.map {
        // e.g. Page 48: (NOT USED NOW)
        //      https://ww.startpage.com/
        //"${activity.getString(R.string.page)} ${it?.destPageIdx?.plus(1)}:\n${it?.uri}"
        it?.uri.toString()
    }

    if (linksLabels.isEmpty()) linksLabels = listOf(activity.getString(R.string.no_links))

    // create and show the bookmarks dialog
    AlertDialog.Builder(activity, R.style.MJDialogThemeLight)
        .setTitle(activity.getString(R.string.pdf_links) + "(${links.size})")
        .setItems(linksLabels.toTypedArray()) { dialog, which ->
            if (links.isEmpty()) return@setItems

            val page = links[which]?.destPageIdx ?: return@setItems
            pdfView.jumpTo(page)
            dialog.dismiss()
        }
        .show()
}

fun showCopyPageTextDialog(
    activity: MainActivity,
    pageNumber: Int,
    pageText: String,
    pref: Preferences,
    bypass: Boolean = false
) {
    if (!bypass && !pref.getCopyTextDialog()) return

    // create a custom view to make the text selectable
    val pageTextView = TextView(activity)
    pageTextView.setPadding(30, 20, 30, 0)
    pageTextView.setTextIsSelectable(true)
    pageTextView.setTextColor(ContextCompat.getColor(activity, R.color.topBarBackgroundColor))
    pageTextView.textSize = 18f
    pageTextView.text = pageText

    val scrollView = ScrollView(activity)
    scrollView.addView(pageTextView)
    //scrollView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
    //scrollView.scrollBarSize = 2

    AlertDialog.Builder(activity, R.style.MJDialogThemeLight)
        .setView(scrollView)
        .setTitle("${activity.getString(R.string.selectable_text)} #${pageNumber + 1}")
        .setNegativeButton(activity.getString(R.string.close)) { dialog, _ -> dialog.dismiss() }
        .setPositiveButton(activity.getString(R.string.copy_all)) { dialog, _ ->
            val copyLabel = "${activity.getString(R.string.page)} #${pageNumber} Text"
            copyToClipboard(activity, copyLabel, pageText)

            // show message to user before closing
            Toast.makeText(activity, activity.getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        .also {
            // don't show this button if the click came from the action bar
            if (!bypass) {
                it.setNeutralButton(activity.getString(R.string.dont_pop_up)) { dialog, _ ->
                    pref.setCopyTextDialog(false)
                    dialog.dismiss()
                }
            }
        }
        .show()
}

fun showUnderDevelopmentDialog(activity: TextModeActivity) {
    AlertDialog.Builder(activity, R.style.MJDialogThemeLight)
        .setTitle(activity.getString(R.string.this_is_experimental))
        .setMessage(activity.getString(R.string.this_is_experimental_message))
        .setPositiveButton(activity.getString(R.string.ok)) { dialog, _ -> dialog.dismiss()}
        .setNegativeButton(activity.getString(R.string.go_back)) { dialog, _ ->
            dialog.dismiss(); activity.finish()
        }
        .show()
}