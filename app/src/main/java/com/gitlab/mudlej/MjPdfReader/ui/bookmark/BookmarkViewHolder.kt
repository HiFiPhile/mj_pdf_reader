package com.gitlab.mudlej.MjPdfReader.ui.bookmark

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.gitlab.mudlej.MjPdfReader.R
import com.gitlab.mudlej.MjPdfReader.data.Bookmark
import com.gitlab.mudlej.MjPdfReader.data.PDF
import com.gitlab.mudlej.MjPdfReader.databinding.BookmarksListItemBinding

class BookmarkViewHolder(
    private val binding: BookmarksListItemBinding,
    private val bookmarkFunctions: BookmarkFunctions,
    private val activity: BookmarksActivity
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(bookmark: Bookmark) {
        val density = activity.resources.displayMetrics.density
        val indentation = (bookmark.level * 16 * density).toInt()
        binding.bookmarkLayout.setPadding(indentation + (10 * density).toInt(), (10 * density).toInt(), (10 * density).toInt(), (10 * density).toInt())

        binding.bookmarkText.text = bookmark.title
        binding.bookmarkText.textSize = PDF.BOOKMARK_TEXT_SIZE - bookmark.level * PDF.BOOKMARK_TEXT_SIZE_DEC

        binding.bookmarkPageNumber.text = (bookmark.pageIdx + 1).toString()
        binding.bookmarkPageNumber.textSize = PDF.BOOKMARK_TEXT_SIZE - bookmark.level * PDF.BOOKMARK_TEXT_SIZE_DEC

        if (bookmark.hasSubBookmarks()) {
            binding.toggleButton.setImageResource(
                if (bookmark.isExpanded) R.drawable.ic_small_arrow_down
                else R.drawable.ic_small_arrow_right
            )
            binding.toggleButton.setOnClickListener {
                bookmarkFunctions.onToggleClicked(bookmark)
            }
        } else {
            binding.toggleButton.setImageResource(R.drawable.ic_bullet_point)
            binding.toggleButton.setOnClickListener(null)
        }

        val clickListener = View.OnClickListener { bookmarkFunctions.onBookmarkClicked(bookmark) }
        binding.bookmarkText.setOnClickListener(clickListener)
        binding.bookmarkPageNumber.setOnClickListener(clickListener)
        binding.bookmarkLayout.setOnClickListener(clickListener)
    }
}
