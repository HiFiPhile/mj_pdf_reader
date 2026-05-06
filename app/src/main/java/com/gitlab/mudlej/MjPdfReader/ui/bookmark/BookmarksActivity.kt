package com.gitlab.mudlej.MjPdfReader.ui.bookmark

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gitlab.mudlej.MjPdfReader.R
import com.gitlab.mudlej.MjPdfReader.data.Bookmark
import com.gitlab.mudlej.MjPdfReader.data.PDF
import com.gitlab.mudlej.MjPdfReader.data.PdfBytesHolder
import com.gitlab.mudlej.MjPdfReader.databinding.ActivityBookmarksBinding
import com.gitlab.mudlej.MjPdfReader.manager.extractor.PdfExtractor
import com.gitlab.mudlej.MjPdfReader.util.ColorUtil
import com.gitlab.mudlej.MjPdfReader.util.createPdfExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BookmarksActivity : AppCompatActivity(), BookmarkFunctions {
    private lateinit var binding: ActivityBookmarksBinding
    private lateinit var pdfExtractor: PdfExtractor
    private val bookmarkAdapter = BookmarkAdapter(this, this)
    private var bookmarks: List<Bookmark> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookmarksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showProgressBar()
        lifecycleScope.launch {
            initPdfExtractor()
            if (::pdfExtractor.isInitialized) {
                initActionBar()
                initBookmarks()
                initUi()
            } else {
                finish()
            }
        }
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun initPdfExtractor() {
        val pdfPath = intent.getStringExtra(PDF.filePathKey)
        val pdfPassword = intent.getStringExtra(PDF.passwordKey)
        try {
            pdfExtractor = createPdfExtractor(this, Uri.parse(pdfPath), pdfPassword)
        }
        catch (throwable: Throwable) {
            Toast.makeText(
                this,
                "Failed to read bookmarks! (file move or deleted?)",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private suspend fun initBookmarks() = withContext(Dispatchers.Default) {
        bookmarks = pdfExtractor.getAllBookmarks()
        val currentPdf = PdfBytesHolder.currentPdf
        
        if (currentPdf != null) {
            if (currentPdf.expandedBookmarkTitles.isEmpty()) {
                // First opening of this file: Expand first level by default
                for (bookmark in bookmarks) {
                    bookmark.isExpanded = true
                    currentPdf.expandedBookmarkTitles.add(bookmark.title)
                }
            } else {
                // Reopening: Restore state from PDF object
                restoreExpansionState(bookmarks, currentPdf.expandedBookmarkTitles)
            }
        }

        withContext(Dispatchers.Main) {
            bookmarkAdapter.setBookmarks(bookmarks)
            binding.progressBar.visibility = View.GONE
            postGettingBookmarks()
        }
    }

    private fun restoreExpansionState(bookmarks: List<Bookmark>, expandedTitles: Set<String>) {
        for (bookmark in bookmarks) {
            if (expandedTitles.contains(bookmark.title)) {
                bookmark.isExpanded = true
            }
            if (bookmark.hasSubBookmarks()) {
                restoreExpansionState(bookmark.subBookmarks, expandedTitles)
            }
        }
    }

    private fun postGettingBookmarks() {
        if (bookmarks.isNotEmpty()) {
            binding.message.visibility = View.GONE
        }
        else {
            binding.message.text = getString(R.string.no_table_of_contents);
        }
    }

    private fun initActionBar() {
        // add back button to the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.table_of_contents)
    }

    private fun initUi() {
        ColorUtil.colorize(this, window, supportActionBar)
        binding.bookmarksRecyclerView.apply {
            adapter = bookmarkAdapter
            layoutManager = LinearLayoutManager(this@BookmarksActivity)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onBookmarkClicked(bookmark: Bookmark) {
        val resultIntent = Intent()
        resultIntent.putExtra(PDF.chosenBookmarkKey, bookmark.pageIdx.toInt())
        setResult(PDF.BOOKMARK_RESULT_OK, resultIntent)
        finish()
    }

    override fun onToggleClicked(bookmark: Bookmark) {
        bookmark.isExpanded = !bookmark.isExpanded
        
        // Sync with the PDF object
        val currentPdf = PdfBytesHolder.currentPdf
        if (currentPdf != null) {
            if (bookmark.isExpanded) {
                currentPdf.expandedBookmarkTitles.add(bookmark.title)
            } else {
                currentPdf.expandedBookmarkTitles.remove(bookmark.title)
            }
        }
        
        bookmarkAdapter.setBookmarks(bookmarks)
    }

    companion object {
        const val TAG = "BookmarksActivity"
    }

}
