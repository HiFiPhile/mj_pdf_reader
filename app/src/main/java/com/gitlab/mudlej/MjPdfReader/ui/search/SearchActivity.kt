package com.gitlab.mudlej.MjPdfReader.ui.search

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.gitlab.mudlej.MjPdfReader.R
import com.gitlab.mudlej.MjPdfReader.data.PDF
import com.gitlab.mudlej.MjPdfReader.data.SearchResult
import com.gitlab.mudlej.MjPdfReader.databinding.ActivitySearchBinding
import com.gitlab.mudlej.MjPdfReader.manager.extractor.PdfExtractor
import com.gitlab.mudlej.MjPdfReader.manager.extractor.PdfExtractorFactory
import com.gitlab.mudlej.MjPdfReader.util.indexesOf
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random
import kotlin.system.measureTimeMillis

class SearchActivity : AppCompatActivity(), SearchResultFunctions {

    private val tag = javaClass.name
    private lateinit var binding: ActivitySearchBinding
    private val searchResultAdapter = SearchResultAdapter(this)
    private var searchResults: MutableList<SearchResult> = mutableListOf()
    private lateinit var pdfExtractor: PdfExtractor

    private val lastPageLiveData = MutableLiveData<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showProgressBar()
        initPdfExtractor()
        initActionBar()
        initSearchResults()
        initLoadingProgressBar()
        initRecyclerView()
    }

    private fun initLoadingProgressBar() {
        binding.searchProgressBar.show()
        binding.searchProgressBar.max = pdfExtractor.getPageCount()
        lastPageLiveData.observe(this@SearchActivity) { pageNumber ->
            binding.searchProgressBar.progress = pageNumber
        }
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun initActionBar() {
        // add back button to the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.searching)
    }

    private fun initPdfExtractor() {
        val pdfPath = intent.getStringExtra(PDF.filePathKey)
        pdfExtractor = PdfExtractorFactory.create(this, Uri.parse(pdfPath))
    }

    private fun initRecyclerView() {
        searchResultAdapter.submitList(searchResults)
        searchResultAdapter.progressBar = binding.progressBar

        binding.searchRecyclerView.apply {
            adapter = searchResultAdapter
            layoutManager = LinearLayoutManager(this@SearchActivity)
        }
    }

    private fun initSearchResults() {
        val query: String? = intent.getStringExtra(PDF.searchQueryKey)
        if (query.isNullOrEmpty() || query.isBlank()) {
            return
        }

        CoroutineScope(Dispatchers.Default).launch {
            searchResults = search(query)
            searchResultAdapter.submitList(searchResults)
            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.GONE
                binding.searchProgressBar.hide()
                postSearch()
            }
        }
        return
    }

    private fun search(query: String): MutableList<SearchResult> {
        val searchResults = mutableListOf<SearchResult>()
        val time = measureTimeMillis {
            for (pageNumber in 1 .. pdfExtractor.getPageCount()) {
                val pageText = pdfExtractor.getPageText(pageNumber)
                if (pageText.isEmpty() || pageText.isBlank()) {
                    continue
                }

                pageText.indexesOf(query, ignoreCase = true).forEach { indexInPage ->
                    searchResults.add(getPageResult(query, indexInPage, pageText, pageNumber))
                }
                lastPageLiveData.postValue(pageNumber)
            }
        }
        Log.d(tag, "getSearchResults: elapsed time: ${time / 1000F}s")
        return searchResults
    }

    private fun getPageResult(
        query: String, indexInPage: Int, pageText: String, pageNumber: Int, textOffset: Int? = null
    ): SearchResult {
        val offset = PDF.SEARCH_RESULT_OFFSET
        val count = query.length

        val resultText = pageText.substring(
            startIndex = max(0, indexInPage - offset),
            endIndex = min(pageText.length, indexInPage + count + offset)
        )

        // trim half words
        val index = resultText.indexOf(query, ignoreCase = true)
        val firstSpace = resultText.indexOf(" ") + 1
        val start = if (firstSpace != -1) min(firstSpace, index) else 0

        val lastSpace = resultText.lastIndexOf(" ")
        val end = if (lastSpace != -1) max(lastSpace, index + count) else resultText.length

        val trimmedText = resultText.substring(start, end)
        val newStart = index - start

        return SearchResult(
            originalIndex = indexInPage,
            inputStart = newStart,
            inputEnd = newStart + count,
            text = trimmedText,
            pageNumber = pageNumber
        )
    }

    private fun postSearch() {
        // set up the title in the App Bar
        title = "${"%,d".format(searchResults.size)} ${getString(R.string.search_results)}"

        // show too many results message
        if (searchResults.size > PDF.TOO_MANY_RESULTS) {
            Snackbar.make(binding.root,getString(R.string.too_many_results_may_be_slow), Snackbar.LENGTH_INDEFINITE).also {
                it.setAction(getText(R.string.ok)) { }
                it.show()
            }
        }
    }

    private fun initFakeData(size: Int = 5000) {
        // generate fake data
        val input = "big"
        val texts = listOf(
            "When the big guy hit the big wall with a very very very big hammer.",
            "When the big guy have big hammer.",
            "Change the color of a few characters, make them big clickable, scale the size of the text or even draw custom bullet points",
        )

        fun createSearchResult(): SearchResult {
            val index = Random.nextInt(0, texts.size)
            val start = texts[index].indexOf(input)
            val end = start + input.length
            return SearchResult(originalIndex = start, start, end, texts[index], pageNumber = Random.nextInt())
        }

        searchResults = MutableList(size) { _ -> createSearchResult() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        // set search functionality
        val searchView = menu.findItem(R.id.search_in_search_activity).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String) = false

            override fun onQueryTextChange(query: String): Boolean {
                searchResultAdapter.nestedQuery = query
                binding.progressBar.visibility = View.VISIBLE
                val filteredList = searchResults.filter { it.text.contains(query, true) }
                searchResultAdapter.submitList(filteredList)
                searchResultAdapter.notifyDataSetChanged() // because the comparator doesn't see the difference in text style
                Snackbar.make(
                    binding.root,
                    getString(R.string.number_of_filtered_results).format(filteredList.size),
                    Snackbar.LENGTH_SHORT
                ).show()
                return false
            }
        })
        searchView.setOnCloseListener {
            searchResultAdapter.submitList(searchResults)
            true
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onSearchResultClicked(searchResult: SearchResult) {
        val resultIntent = Intent()
        resultIntent.putExtra(PDF.searchResultKey, Gson().toJson(searchResult))
        setResult(PDF.SEARCH_RESULT_OK, resultIntent)
        finish()
    }

    // TODO: Not working properly yet
    override fun onShowMoreResultTextClicked(searchResult: SearchResult, searchResultIndex: Int): SearchResult {
        val query = searchResult.text.substring(searchResult.inputStart, searchResult.inputEnd)
        val pageText = pdfExtractor.getPageText(searchResult.pageNumber)
        val newInputIndex = pageText.indexOf(searchResult.text) + searchResult.inputStart

        return searchResult.apply {
            val newSearchResult = SearchResult(
                originalIndex = searchResult.originalIndex,
                inputStart = newInputIndex,
                inputEnd = newInputIndex + query.length,
                pageText,
                searchResult.pageNumber,
                expanded = true,
                longText = true
            )
            searchResults[searchResultIndex] = newSearchResult
            searchResultAdapter.notifyItemChanged(searchResultIndex)
        }
    }
}