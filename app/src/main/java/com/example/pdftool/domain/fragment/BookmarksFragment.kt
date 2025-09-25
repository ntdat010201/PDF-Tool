package com.example.pdftool.domain.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pdftool.apdater.BookmarkAdapter
import com.example.pdftool.data.database.entities.BookmarkEntity
import com.example.pdftool.data.repository.BookmarkRepository
import com.example.pdftool.databinding.FragmentBookmarksBinding
import com.example.pdftool.domain.activities.OpenFilePdfActivity
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.File

class BookmarksFragment : Fragment() {
    private lateinit var binding: FragmentBookmarksBinding
    private lateinit var bookmarkAdapter: BookmarkAdapter
    private val bookmarkRepository: BookmarkRepository by inject()
    private var isRecyclerViewVisible = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookmarksBinding.inflate(layoutInflater)
        initViews()
        initData()
        return binding.root
    }

    private fun initViews() {
        setupRecyclerView()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        bookmarkAdapter = BookmarkAdapter(
            onBookmarkClick = { bookmark ->
                openPdfAtBookmark(bookmark)
            },
            onDeleteClick = { bookmark ->
                deleteBookmark(bookmark)
            }
        )

        binding.recyclerViewBookmarks.apply {
            adapter = bookmarkAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupClickListeners() {
        binding.imgBookmarks.setOnClickListener {
            toggleRecyclerViewVisibility()
        }
    }

    private fun initData() {
        observeBookmarks()
    }

    private fun observeBookmarks() {
        bookmarkRepository.getAllBookmarks().observe(viewLifecycleOwner) { bookmarks ->
            updateBookmarksList(bookmarks)
            updateBookmarkCount(bookmarks.size)
        }
    }

    private fun updateBookmarksList(bookmarks: List<BookmarkEntity>) {
        bookmarkAdapter.submitList(bookmarks)

        if (bookmarks.isEmpty()) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.recyclerViewBookmarks.visibility = View.GONE
        } else {
            binding.layoutEmptyState.visibility = View.GONE
            binding.recyclerViewBookmarks.visibility = View.VISIBLE
            isRecyclerViewVisible = true // Tự động set visible khi có dữ liệu
        }
    }

    private fun updateBookmarkCount(count: Int) {
        binding.tvBookmarkCount.text = count.toString()
    }

    private fun toggleRecyclerViewVisibility() {
        isRecyclerViewVisible = !isRecyclerViewVisible

        if (isRecyclerViewVisible) {
            // Show RecyclerView
            if (bookmarkAdapter.itemCount > 0) {
                binding.recyclerViewBookmarks.visibility = View.VISIBLE
                binding.layoutEmptyState.visibility = View.GONE
            } else {
                binding.recyclerViewBookmarks.visibility = View.GONE
                binding.layoutEmptyState.visibility = View.VISIBLE
            }
        } else {
            // Hide RecyclerView
            binding.recyclerViewBookmarks.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.GONE
        }
    }

    private fun openPdfAtBookmark(bookmark: BookmarkEntity) {
        try {
            val file = File(bookmark.filePath)
            if (file.exists()) {
                val intent = Intent(requireContext(), OpenFilePdfActivity::class.java).apply {
                    putExtra("file_path", bookmark.filePath)
                    putExtra("file_name", bookmark.fileName)
                    putExtra("bookmark_page", bookmark.pageNumber)
                }
                startActivity(intent)
            } else {
                Toast.makeText(
                    requireContext(),
                    "File không tồn tại: ${bookmark.fileName}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Lỗi khi mở file: ${e.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun deleteBookmark(bookmark: BookmarkEntity) {
        lifecycleScope.launch {
            try {
                bookmarkRepository.deleteBookmark(bookmark)
                Toast.makeText(requireContext(), "Đã xóa bookmark", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Lỗi khi xóa bookmark: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}