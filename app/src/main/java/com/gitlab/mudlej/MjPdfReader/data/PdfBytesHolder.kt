package com.gitlab.mudlej.MjPdfReader.data

import android.net.Uri
import com.gitlab.mudlej.MjPdfReader.manager.extractor.PdfExtractor

object PdfBytesHolder {
    var pdfByte: ByteArray? = null
    var currentPdf: PDF? = null
    var cachedUri: Uri? = null
    var pdfExtractor: PdfExtractor? = null

    fun clear() { 
        pdfByte = null
        currentPdf = null
        cachedUri = null
        pdfExtractor = null
    }
}
