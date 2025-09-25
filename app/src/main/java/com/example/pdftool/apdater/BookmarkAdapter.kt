package com.example.pdftool.apdater

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pdftool.data.database.entities.BookmarkEntity
import com.example.pdftool.databinding.ItemBookmarkBinding
import java.text.SimpleDateFormat
import java.util.*

class BookmarkAdapter(
    private val onBookmarkClick: (BookmarkEntity) -> Unit,
    private val onDeleteClick: (BookmarkEntity) -> Unit
) : ListAdapter<BookmarkEntity, BookmarkAdapter.BookmarkViewHolder>(BookmarkDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        val binding = ItemBookmarkBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookmarkViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BookmarkViewHolder(
        private val binding: ItemBookmarkBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(bookmark: BookmarkEntity) {
            binding.apply {
                // Set bookmark title
                tvBookmarkTitle.text = bookmark.title

                // Set file name
                tvFileName.text = bookmark.fileName

                // Set page number
                tvPageNumber.text = "Page ${bookmark.pageNumber + 1}"

                // Set note (show/hide based on availability)
                if (bookmark.note.isNullOrBlank()) {
                    tvNote.visibility = View.GONE
                } else {
                    tvNote.visibility = View.VISIBLE
                    tvNote.text = bookmark.note
                }

                // Set created time
                tvCreatedTime.text = formatTime(bookmark.createdTime)

                // Set click listeners
                root.setOnClickListener {
                    onBookmarkClick(bookmark)
                }

                btnDeleteBookmark.setOnClickListener {
                    onDeleteClick(bookmark)
                }
            }
        }

        private fun formatTime(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            return when {
                diff < 60 * 1000 -> "Vừa xong"
                diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} phút trước"
                diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} giờ trước"
                diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)} ngày trước"
                else -> {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    sdf.format(Date(timestamp))
                }
            }
        }
    }

    class BookmarkDiffCallback : DiffUtil.ItemCallback<BookmarkEntity>() {
        override fun areItemsTheSame(oldItem: BookmarkEntity, newItem: BookmarkEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BookmarkEntity, newItem: BookmarkEntity): Boolean {
            return oldItem == newItem
        }
    }
}