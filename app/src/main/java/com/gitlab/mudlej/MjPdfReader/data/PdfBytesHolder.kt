package com.gitlab.mudlej.MjPdfReader.data

object PdfBytesHolder {
    var pdfByte: ByteArray? = null
    var currentPdf: PDF? = null

    fun clear() { 
        pdfByte = null
        currentPdf = null
    }
}
