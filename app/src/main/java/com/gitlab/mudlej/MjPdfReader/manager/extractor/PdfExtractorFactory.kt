package com.gitlab.mudlej.MjPdfReader.manager.extractor

import android.app.Activity
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.shockwave.pdfium.PdfiumCore
import java.io.IOException
import kotlin.jvm.Throws

object PdfExtractorFactory {

    @Throws(IOException::class)
    fun create(activity: Activity, uri: Uri): PdfExtractor {
        val pdfium = PdfiumCore(activity)
        val parcelFileDescriptor = activity.contentResolver.openFileDescriptor(uri, "r")
        val pdfDocument = pdfium.newDocument(parcelFileDescriptor)
        return PdfExtractorImpl(pdfium, pdfDocument)
    }
}