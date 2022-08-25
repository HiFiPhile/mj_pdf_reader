/*
* Note: This is an experimental class, and the code is just a draft
* several operations should be done off UI
* */

package com.gitlab.mudlej.MjPdfReader.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import com.gitlab.mudlej.MjPdfReader.R
import com.gitlab.mudlej.MjPdfReader.data.Preferences
import com.gitlab.mudlej.MjPdfReader.databinding.ActivityTextModeBinding
import com.gitlab.mudlej.MjPdfReader.util.ExtendedDataHolder
import com.google.android.material.snackbar.Snackbar
import java.util.concurrent.Executors

class TextModeActivity : AppCompatActivity() {
    private val TAG = "TextMode"
    private lateinit var binding: ActivityTextModeBinding

    private val extras = ExtendedDataHolder.instance
    private lateinit var pdfText: Map<Int, String>
    private lateinit var allText: String
    private var isAllTextReady = false

    private var pageNum = 0
    private var pdfLength = 0

    private var fontSize = 16f
    private val minFontSize = 3f
    private val maxFontSize = 150f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextModeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initActionBar()
        initPdfText()

        // get pages count
        pdfLength = intent.getIntExtra(Preferences.pdfLengthKey, Preferences.pdfLengthDefault)

        // init font size
        binding.pageTextView.textSize = fontSize

        // set viewing mode
        //setPdfPagesMode()
        setContinuousMode()
    }

    private fun initActionBar() {
        // add back button to the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // set title
        title = getString(R.string.text_mode_experimental)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.text_mode_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        if (isAllTextReady) {
            menu.findItem(R.id.search_in_text_mode).isVisible = true

            // set search functionality
            val searchView = menu.findItem(R.id.search_in_text_mode).actionView as SearchView
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    binding.progressBar.visibility = View.VISIBLE
                    findInTextView(query)
                    return false
                }
                override fun onQueryTextChange(newText: String): Boolean {
                    return false
                }
            })
//            searchView.setOnCloseListener {
//                resetTextViewColor()
//                true
//            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.increase_font -> increaseFontSize()
            R.id.decrease_font -> decreaseFontSize()
            android.R.id.home -> finish()
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun findInTextView(query: String) {
        var queryIndex = -1
        var lineNumber = 0
        var resultCount = 0
        var highlighted = ""

        // Off UI thread
        Executors.newSingleThreadExecutor().execute {
            val textView = binding.pageTextView
            queryIndex = allText.indexOf(query)
            resultCount = query.count { allText.contains(it)}

            // if there's a match, find line number and color it with RED
            if (queryIndex != -1) {
                lineNumber = textView.layout.getLineForOffset(queryIndex)
                highlighted = "<font color='red'>$query</font>"
            }

            // back to UI thread
            Handler(Looper.getMainLooper()).post {
                // apply results
                if (queryIndex != -1) {
                    textView.text = Html.fromHtml(allText.replace(query, highlighted))
                    binding.pageTextScrollView.scrollTo(0, textView.layout.getLineTop(lineNumber))
                }
                else {
                    resetTextView()
                }
                Snackbar.make(binding.root, "Found $resultCount occurrences",
                    Snackbar.LENGTH_LONG).show()
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun resetTextView() {
//        allText = "<font color='black'>$allText</font>"
//        binding.pageTextView.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
        binding.pageTextView.text = allText
    }

    private fun increaseFontSize() {
        if (fontSize < maxFontSize) binding.pageTextView.textSize = ++fontSize
    }

    private fun decreaseFontSize() {
        if (fontSize > minFontSize) binding.pageTextView.textSize = --fontSize
    }

    private fun initPdfText() {
        if (intent.getStringExtra(Preferences.uriKey) == null) finish()

        val key = intent.getStringExtra(Preferences.uriKey) as String
        if (extras.hasExtra(key))
            try {
                pdfText = extras.getExtra(key) as Map<Int, String>

                // combine all text in one string off UI
                Executors.newSingleThreadExecutor().execute {
                    allText = pdfText.values
                        .reduce { acc, s -> "$acc $s" }
                        .replace("\n+", "\n")   // replace one or more newline

                    // back to UI thread
                    Handler(Looper.getMainLooper()).post {
                        isAllTextReady = true
                        invalidateOptionsMenu()
                        binding.pageTextView.text = allText
                        binding.progressBar.visibility = View.GONE
                        showUnderDevelopmentDialog(this)
                    }
                }
            }
            catch (e: ClassCastException) {
                Log.e(TAG, "Error couldn't cast: ${e.message}")
                finish()
            }
        else {
            Log.e(TAG, "Error: couldn't find pdfText map in extras")
            finish()
        }

    }

    private fun setContinuousMode() {
        binding.apply {
            buttonsLayout.visibility = View.GONE
        }
    }

    private fun setPdfPagesMode() {
        binding.apply {
            buttonsLayout.visibility = View.VISIBLE
            pageTextView.text = textOrEmpty(pageNum)
            updatePageCounterView()
            prevButton.setOnClickListener {
                if (pageNum > 0) {
                    pageTextView.text = textOrEmpty(--pageNum)
                    updatePageCounterView()
                }
            }
            nextButton.setOnClickListener {
                if (pageNum < pdfLength) {
                    pageTextView.text = textOrEmpty(++pageNum)
                    updatePageCounterView()
                }
            }
        }
    }

    private fun updatePageCounterView() {
        binding.pageCounter.text = "${getString(R.string.page)}: ${pageNum + 1}/${pdfLength}"
    }

    // if null or empty return Empty page, otherwise return it as it is
    private fun textOrEmpty(i: Int): String
        = (pdfText[i] ?: "").ifEmpty { getString(R.string.empty_page) }
}