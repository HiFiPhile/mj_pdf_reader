package com.gitlab.mudlej.MjPdfReader.ui.bookmark

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.gitlab.mudlej.MjPdfReader.data.Bookmark
import com.gitlab.mudlej.MjPdfReader.databinding.BookmarksListItemBinding


class BookmarkAdapter(
    private val bookmarkFunctions: BookmarkFunctions,
    val activity: BookmarksActivity
) : ListAdapter<Bookmark, BookmarkViewHolder>(BookmarkComparator()) {

    private var rootBookmarks: List<Bookmark> = listOf()

    fun setBookmarks(bookmarks: List<Bookmark>) {
        rootBookmarks = bookmarks
        updateVisibleBookmarks()
    }

    private fun updateVisibleBookmarks() {
        val visibleList = mutableListOf<Bookmark>()
        for (bookmark in rootBookmarks) {
            addBookmarkAndChildrenIfExpanded(visibleList, bookmark)
        }
        submitList(visibleList)
    }

    private fun addBookmarkAndChildrenIfExpanded(list: MutableList<Bookmark>, bookmark: Bookmark) {
        list.add(bookmark)
        if (bookmark.isExpanded) {
            for (child in bookmark.subBookmarks) {
                addBookmarkAndChildrenIfExpanded(list, child)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        return BookmarkViewHolder(
            BookmarksListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            bookmarkFunctions,
            activity
        )
    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, i: Int) {
        getItem(i)?.let { bookmark ->
            holder.bind(bookmark)
        }
    }

}
