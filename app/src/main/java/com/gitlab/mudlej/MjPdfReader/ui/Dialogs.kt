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
import android.content.ClipData
import android.content.ClipboardManager
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
import com.github.barteksc.pdfviewer.PDFView
import com.gitlab.mudlej.MjPdfReader.BuildConfig
import com.gitlab.mudlej.MjPdfReader.R
import com.gitlab.mudlej.MjPdfReader.data.PDF
import com.gitlab.mudlej.MjPdfReader.data.Preferences
import com.gitlab.mudlej.MjPdfReader.databinding.ActivityMainBinding
import com.gitlab.mudlej.MjPdfReader.databinding.PasswordDialogBinding
import com.gitlab.mudlej.MjPdfReader.util.copyToClipboard
import com.gitlab.mudlej.MjPdfReader.util.indexesOf
import com.gitlab.mudlej.MjPdfReader.util.putEditTextInLinearLayout
import com.google.android.material.snackbar.Snackbar
import com.shockwave.pdfium.PdfDocument
import java.util.concurrent.Executors

private const val TAG = "Dialogs"

fun showAppFeaturesDialog(context: Context) {
    val end = "\n\n"
    AlertDialog.Builder(context)
        .setTitle("${context.resources.getString(R.string.app_name)} ${BuildConfig.VERSION_NAME} Features")
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
        .show()
}

fun showMetaDialog(context: Context, meta: PdfDocument.Meta) {
    AlertDialog.Builder(context)
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
    AlertDialog.Builder(context)
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
    val alert = AlertDialog.Builder(context)
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
    AlertDialog.Builder(activity)
        .setTitle(activity.getString(R.string.bookmarks))
        .setItems(bookmarks.toTypedArray()) { dialog, which ->
            if (pdfView.tableOfContents.isEmpty()) return@setItems

            val page = pdfView.tableOfContents[which].pageIdx
            pdfView.jumpTo(page.toInt())
            dialog.dismiss()
        }
        .show()
}

fun showPageTextDialog(activity: MainActivity, pdf: PDF, pref: Preferences, bypass: Boolean = false) {
    if (!bypass && !pref.getCopyTextDialog()) return

    // TODO: remove this workaround to prevent crashing
    if (pdf.sizeInMb > 50) {
        Toast.makeText(activity,
            activity.getString(R.string.not_available_file_too_big), Toast.LENGTH_LONG).show()
        return
    }

    var hasText = true
    // copy page's text or set an appropriate message
    val pageText =
        if (!pdf.isExtractingTextFinished)
            "Try few seconds later, still extracting text from the PDF."
        else
            (pdf.pagesText[pdf.pageNumber + 1] ?: "").trim().ifEmpty {
                hasText = false
                activity.getString(R.string.could_not_text)
            }

    // create a custom view to make the text selectable
    val pageTextView = TextView(activity)
    pageTextView.setPadding(30, 20, 30, 0)
    pageTextView.setTextIsSelectable(true)
    pageTextView.text = pageText
    pageTextView.textSize = 16f

    AlertDialog.Builder(activity)
        .setView(pageTextView)
        .setTitle("${activity.getString(R.string.selectable_text)} " +
                "#${pdf.pageNumber + 1} (${activity.getString(R.string.experimental)})")
        .setNegativeButton(activity.getString(R.string.close)) { dialog, _ -> dialog.dismiss() }
        .also {
            if (hasText)
                it.setPositiveButton(activity.getString(R.string.copy_all)) { dialog, _ ->
                // copy page's text to clipboard
                val copyLabel = "${activity.getString(R.string.page)} #${pdf.pageNumber} Text"
                copyToClipboard(activity, copyLabel, pageText)

                // show message to user before closing
                Toast.makeText(activity, activity.getString(R.string.copied_to_clipboard),
                    Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
        .show()
}



fun showUnderDevelopmentDialog(activity: TextModeActivity) {
    AlertDialog.Builder(activity)
        .setTitle(activity.getString(R.string.this_is_experimental))
        .setMessage(activity.getString(R.string.this_is_experimental_message))
        .setPositiveButton(activity.getString(R.string.ok)) { dialog, _ -> dialog.dismiss()}
        .setNegativeButton(activity.getString(R.string.go_back)) { dialog, _ ->
            dialog.dismiss(); activity.finish()
        }
        .show()
}

fun showSearchDialog(
    activity: MainActivity,
    pdf: PDF,
    binding: ActivityMainBinding,
    handler: Handler)
{
    // TODO: remove this workaround to prevent crashing
    if (pdf.sizeInMb > 50) {
        Toast.makeText(activity,
            activity.getString(R.string.not_available_file_too_big), Toast.LENGTH_LONG).show()
        return
    }

    if (!pdf.isExtractingTextFinished) {
        Toast.makeText(activity, activity.getString(R.string.app_still_extracting_text),
            Toast.LENGTH_LONG).show()
        return
    }

    val resultMap = mutableMapOf<Int, MutableList<String>>()
    val lineNumbers = mutableListOf<Int>()

    val searchInput = EditText(activity)
    val layout = LinearLayout(activity)
    putEditTextInLinearLayout(activity, searchInput, layout)

    AlertDialog.Builder(activity)
        .setTitle(activity.getString(R.string.search_dialog_title))
        .setMessage(activity.getString(R.string.search_dialog_message))
        .setView(layout)
        .setPositiveButton(activity.getString(R.string.search)) { dialog, _ ->
            val query = searchInput.text.toString().lowercase().trim()

            // check if the user provided input
            if (query.isEmpty()) {
                Toast.makeText(activity,
                    activity.getString(R.string.search_input_empty), Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            dialog.dismiss()
            binding.progressBar.visibility = View.VISIBLE
            Executors.newSingleThreadExecutor().execute {
                val offset = 70

                // search each page
                for ((page, text) in pdf.pagesText) {

                    // find the indexes of each match of the query in the page
                    for (index in text.lowercase().indexesOf(query)) {
                        // calculate the line number of the result and store it
                        lineNumbers.add(text.substring(0, index).count { it == '\n'})

                        // slice the query with some text (offset) after if there is
                        // val startIndex = if (index - offset < 0) 0 else index - offset
                        // start from the beginning of the line
                        val lineStartIndex = text.substring(0, index).lastIndexOf("\n")
                        val startIndex = if (lineStartIndex == -1) 0 else lineStartIndex
                        val lastIndex = if (index + offset >= text.length) text.lastIndex
                                        else index + offset

                        // marked text is: " this query indeed" -> " this {query} indeed"
                        // made it this way to key the original case of text
                        val markedText = text.substring(0, index) + "{" +
                                text.substring(index, index + query.length) + "}" +
                                text.substring(index + query.length, text.length)

                        val result = markedText
                            .substring(startIndex + 1, lastIndex + 1)   // +1 because of marking
                            .replace("\n", " ")         // remove newlines

                        // create result list for the page if there is none
                        if (resultMap[page] == null) resultMap[page] = mutableListOf()
                        resultMap[page]?.add(result)
                    }
                }
                handler.post {
                    binding.progressBar.visibility = View.GONE
                    showSearchResultDialog(resultMap, lineNumbers, activity, binding)
                }
            }
        }
        .show()
}

fun showSearchResultDialog(
    resultMap: Map<Int, MutableList<String>>,
    lineNumbers: List<Int>,
    activity: MainActivity,
    binding: ActivityMainBinding,)
{
    val resultArrow = "->"
    val dividerHeight = 3

    // create a list of formatted results to show and a list of  their page numbers
    var count = 0
    val resultList = mutableListOf<String>()
    val pageNumbers = mutableListOf<Int>()
    for ((pageNumber, pages) in resultMap)
        for (i in pages.indices) {
            // format the result message. e.g. (Line 25 - Page 12) -> this is {query} text...
            resultList.add(
                "(${activity.getString(R.string.line)} ${lineNumbers[count++]} - " +
                "${activity.getString(R.string.page)} $pageNumber) $resultArrow ${pages[i]}..."
            )
            pageNumbers.add(pageNumber)
        }

    val resultsDialog = AlertDialog.Builder(activity)
        // title e.g. Search Results: 345 found.
        .setTitle("${activity.getString(R.string.search_results)} ${resultList.size} ${activity.getString(R.string.found)}.")
        .setItems(resultList.toTypedArray()) { dialog, i ->
            dialog.dismiss()
            binding.pdfView.jumpTo(pageNumbers[i] - 1)  // because starting from 0 and 1 thing

            // show a message that will stay until the user dismiss so he can find the line in page
            Snackbar.make(binding.root, resultList[i], Snackbar.LENGTH_INDEFINITE)
                .setAction(activity.getString(R.string.ok)) { }
                .show()
        }
        .create()
    resultsDialog.listView.divider = ColorDrawable(Color.LTGRAY)    // create divider
    resultsDialog.listView.dividerHeight = dividerHeight
    resultsDialog.show()
}