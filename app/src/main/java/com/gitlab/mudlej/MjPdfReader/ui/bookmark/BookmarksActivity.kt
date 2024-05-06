package com.gitlab.mudlej.MjPdfReader.ui.bookmark

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.gitlab.mudlej.MjPdfReader.R
import com.gitlab.mudlej.MjPdfReader.data.Bookmark
import com.gitlab.mudlej.MjPdfReader.data.PDF
import com.gitlab.mudlej.MjPdfReader.databinding.ActivityBookmarksBinding
import com.gitlab.mudlej.MjPdfReader.manager.extractor.PdfExtractor
import com.gitlab.mudlej.MjPdfReader.manager.extractor.PdfExtractorFactory
import com.gitlab.mudlej.MjPdfReader.util.ColorUtil
import kotlinx.coroutines.CoroutineScope
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
        initPdfExtractor()
        initActionBar()
        initBookmarks()
        initUi()
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun initPdfExtractor() {
        val pdfPath = intent.getStringExtra(PDF.filePathKey)
        try {
            pdfExtractor = PdfExtractorFactory.create(this, Uri.parse(pdfPath))
        }
        catch (throwable: Throwable) {
            Log.e(TAG, "initPdfExtractor: Failed to create PdfExtractor!", throwable)
            Toast.makeText(
                this@BookmarksActivity,
                "Failed to read bookmarks (try re-open the file)",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }

    private fun initBookmarks() {
        CoroutineScope(Dispatchers.Default).launch {
            bookmarks = pdfExtractor.getAllBookmarks()
            bookmarkAdapter.submitList(bookmarks)

            // back to the UI
            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.GONE
                postGettingBookmarks()
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

        title = "Searching..."
    }

    private fun initUi() {
        ColorUtil.colorize(this, window)
        title = getString(R.string.table_of_contents)
        bookmarkAdapter.submitList(bookmarks)
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

    companion object {
        const val TAG = "BookmarksActivity"
    }

}