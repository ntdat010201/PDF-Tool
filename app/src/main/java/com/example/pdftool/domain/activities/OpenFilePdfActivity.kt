package com.example.pdftool.domain.activities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.pdftool.base.BaseActivity
import com.example.pdftool.databinding.ActivityOpenFilePdfBinding
import com.example.pdftool.model.ModelFileItem
import com.example.pdftool.viewmodel.FileViewModel
import com.example.pdftool.data.repository.BookmarkRepository
import com.example.pdftool.data.database.entities.BookmarkEntity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.pdftool.R
import com.github.barteksc.pdfviewer.listener.*
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.shockwave.pdfium.PdfDocument
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.android.ext.android.inject
import java.io.File


class OpenFilePdfActivity : BaseActivity() {
    private lateinit var binding: ActivityOpenFilePdfBinding
    private var file: ModelFileItem? = null
    private val fileViewModel: FileViewModel by viewModel()
    private val bookmarkRepository: BookmarkRepository by inject()
    
    // PDF viewer state variables
    private var currentPage = 0
    private var totalPages = 0
    private var isNightMode = false
    private var currentZoom = 1.0f
    private var isToolbarVisible = true
    private var bookmarks = mutableListOf<PdfDocument.Bookmark>()
    private var tableOfContents = mutableListOf<PdfDocument.Bookmark>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpenFilePdfBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initData()
        initView()
        initListener()
    }


    private fun initData() {
        file = intent.getParcelableExtra("data_pdf")
        
        // Handle direct file path from bookmark
        val filePath = intent.getStringExtra("file_path")
        val fileName = intent.getStringExtra("file_name")
        val bookmarkPage = intent.getIntExtra("bookmark_page", -1)
        
        if (filePath != null && fileName != null) {
            // Create ModelFileItem from bookmark data
            val fileItem = File(filePath)
            file = ModelFileItem(
                name = fileName,
                path = filePath,
                type = "pdf",
                lastModified = fileItem.lastModified(),
                size = fileItem.length(),
                uri = null
            )
            
            // Set current page to bookmark page
            if (bookmarkPage >= 0) {
                currentPage = bookmarkPage
            }
        }
        
        Log.d("DAT", "initData: ${file?.path}, bookmarkPage: $bookmarkPage")
    }

    private fun initView() {
        if (file == null) {
            Toast.makeText(this, "file không tồn tại", Toast.LENGTH_SHORT).show()
            onBackPressedDispatcher.onBackPressed()
        } else {
            setupPdfViewer()
        }
    }
    
    private fun setupPdfViewer() {
        val pdfFile = File(file!!.path)

        binding.pdfViewer.fromFile(pdfFile)
            // Display all pages by default
            .enableSwipe(true) // Enable swipe navigation
            .swipeHorizontal(false) // Vertical scrolling
            .enableDoubletap(true) // Enable double tap to zoom
            .defaultPage(currentPage) // Start from current page (bookmark or first page)
            .enableAnnotationRendering(true) // Render annotations
            .password(null) // No password protection
            .scrollHandle(DefaultScrollHandle(this)) // Add scroll handle
            .enableAntialiasing(true) // Smooth rendering
            .spacing(10) // Space between pages
            .autoSpacing(false) // Manual spacing
            .pageFitPolicy(FitPolicy.WIDTH) // Fit to width
            .fitEachPage(false) // Scale relative to largest page
            .pageSnap(true) // Snap to page boundaries
            .pageFling(true) // Enable page fling
            .nightMode(isNightMode) // Night mode toggle

            // Enhanced drawing with page separators
            .onDrawAll(object : OnDrawListener {
                override fun onLayerDrawn(
                    canvas: Canvas,
                    pageWidth: Float,
                    pageHeight: Float,
                    displayedPage: Int
                ) {
                    // Draw page separator line
                    val paint = Paint().apply {
                        color = if (isNightMode) Color.GRAY else Color.LTGRAY
                        strokeWidth = 2f
                        isAntiAlias = true
                    }
                    canvas.drawLine(0f, pageHeight, pageWidth, pageHeight, paint)
                    
                    // Draw page number in corner
                    val textPaint = Paint().apply {
                        color = if (isNightMode) Color.WHITE else Color.BLACK
                        textSize = 24f
                        isAntiAlias = true
                    }
                    canvas.drawText(
                        "Page ${displayedPage + 1}",
                        pageWidth - 100f,
                        30f,
                        textPaint
                    )
                }
            })

            // Page change listener
            .onPageChange(object : OnPageChangeListener {
                override fun onPageChanged(page: Int, pageCount: Int) {
                    currentPage = page
                    totalPages = pageCount
                    updatePageInfo()
                    updateBookmarkIcon() // Update bookmark icon when page changes
                    Log.d("PDF_VIEWER", "Page changed: ${page + 1}/$pageCount")
                }
            })

            // Page scroll listener
            .onPageScroll(object : OnPageScrollListener {
                override fun onPageScrolled(page: Int, positionOffset: Float) {
                    // Update scroll position
                    Log.d("PDF_VIEWER", "Page scrolled: $page, offset: $positionOffset")
                }
            })

            // Tap listener for UI controls
            .onTap(object : OnTapListener {
                override fun onTap(e: MotionEvent): Boolean {
                    toggleToolbarVisibility()
                    return true
                }
            })

            // Long press listener for context menu
            .onLongPress(object : OnLongPressListener {
                override fun onLongPress(e: MotionEvent) {
                    showContextMenu(e)
                }
            })

            // Error handling
            .onError(object : OnErrorListener {
                override fun onError(t: Throwable) {
                    Log.e("PDF_VIEWER", "Error loading PDF: ${t.message}")
                    Toast.makeText(this@OpenFilePdfActivity, "Lỗi khi tải PDF: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })

            // Page error handling
            .onPageError(object : OnPageErrorListener {
                override fun onPageError(page: Int, t: Throwable) {
                    Log.e("PDF_VIEWER", "Error loading page $page: ${t.message}")
                    Toast.makeText(this@OpenFilePdfActivity, "Lỗi tải Page $page", Toast.LENGTH_SHORT).show()
                }
            })

            // Render listener
            .onRender(object : OnRenderListener {
                override fun onInitiallyRendered(nbPages: Int) {
                    totalPages = nbPages
                    updatePageInfo()
                    Log.d("PDF_VIEWER", "PDF rendered with $nbPages pages")
                }
            })

            // Load complete listener
            .onLoad(object : OnLoadCompleteListener {
                override fun loadComplete(nbPages: Int) {
                    totalPages = nbPages
                    updatePageInfo()
                    loadTableOfContents()
                    
                    // Save file to recent files when successfully loaded
                    file?.let { fileItem ->
                        fileViewModel.addRecentFile(fileItem)
                    }
                    
                    Log.d("PDF_VIEWER", "PDF loaded successfully with $nbPages pages")
                    Toast.makeText(this@OpenFilePdfActivity, "Đã tải PDF thành công ($nbPages Page)", Toast.LENGTH_SHORT).show()
                }
            })

            .load()
    }

    private fun initListener() {
        binding.icBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        
        binding.icBookmark.setOnClickListener {
            toggleBookmark()
        }
        
        // Update bookmark icon when page changes
        updateBookmarkIcon()
    }
    
    // Update page information display
    private fun updatePageInfo() {
        // This method can be used to update UI elements showing current page info
        // For now, we'll just log the information
        Log.d("PDF_VIEWER", "Current page: ${currentPage + 1}/$totalPages")
    }
    
    // Toggle toolbar visibility
    private fun toggleToolbarVisibility() {
        isToolbarVisible = !isToolbarVisible
        // You can implement UI toolbar show/hide logic here
        Log.d("PDF_VIEWER", "Toolbar visibility: $isToolbarVisible")
    }
    
    // Show context menu on long press
    private fun showContextMenu(e: MotionEvent) {
        val options = arrayOf(
            "Chế độ ban đêm",
            "Zoom vào",
            "Zoom ra", 
            "Đi đến page",
            "Mục lục",
            "Thông tin tài liệu"
        )
        
        AlertDialog.Builder(this)
            .setTitle("Tùy chọn PDF")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> toggleNightMode()
                    1 -> zoomIn()
                    2 -> zoomOut()
                    3 -> showGoToPageDialog()
                    4 -> showTableOfContents()
                    5 -> showDocumentInfo()
                }
            }
            .show()
    }
    
    // Toggle night mode
    private fun toggleNightMode() {
        isNightMode = !isNightMode
        binding.pdfViewer.setNightMode(isNightMode)
        binding.pdfViewer.invalidate() // Force immediate refresh
        Toast.makeText(this, if (isNightMode) "Đã bật chế độ ban đêm" else "Đã tắt chế độ ban đêm", Toast.LENGTH_SHORT).show()
    }
    
    // Zoom in
    private fun zoomIn() {
        currentZoom = (currentZoom * 1.2f).coerceAtMost(binding.pdfViewer.maxZoom)
        binding.pdfViewer.zoomTo(currentZoom)
        binding.pdfViewer.invalidate() // Force immediate refresh
        Toast.makeText(this, "Zoom: ${(currentZoom * 100).toInt()}%", Toast.LENGTH_SHORT).show()
    }
    
    // Zoom out
    private fun zoomOut() {
        currentZoom = (currentZoom / 1.2f).coerceAtLeast(binding.pdfViewer.minZoom)
        binding.pdfViewer.zoomTo(currentZoom)
        binding.pdfViewer.invalidate() // Force immediate refresh
        Toast.makeText(this, "Zoom: ${(currentZoom * 100).toInt()}%", Toast.LENGTH_SHORT).show()
    }
    
    // Show go to page dialog
    private fun showGoToPageDialog() {
        val input = android.widget.EditText(this)
        input.hint = "Nhập số page (1-$totalPages)"
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        
        AlertDialog.Builder(this)
            .setTitle("Đi đến page")
            .setView(input)
            .setPositiveButton("Đi") { _, _ ->
                val pageNumber = input.text.toString().toIntOrNull()
                if (pageNumber != null && pageNumber in 1..totalPages) {
                    binding.pdfViewer.jumpTo(pageNumber - 1, true)
                    Toast.makeText(this, "Đã chuyển đến page $pageNumber", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Số page không hợp lệ", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
    
    // Load table of contents
    private fun loadTableOfContents() {
        try {
            tableOfContents = binding.pdfViewer.tableOfContents.toMutableList()
            Log.d("PDF_VIEWER", "Loaded ${tableOfContents.size} table of contents entries")
        } catch (e: Exception) {
            Log.e("PDF_VIEWER", "Error loading table of contents: ${e.message}")
        }
    }
    
    // Show table of contents
    private fun showTableOfContents() {
        if (tableOfContents.isEmpty()) {
            Toast.makeText(this, "Tài liệu này không có mục lục", Toast.LENGTH_SHORT).show()
            return
        }
        
        val titles = tableOfContents.map { "${it.title} (page ${it.pageIdx + 1})" }.toTypedArray()
        
        AlertDialog.Builder(this)
            .setTitle("Mục lục")
            .setItems(titles) { _, which ->
                val bookmark = tableOfContents[which]
                binding.pdfViewer.jumpTo(bookmark.pageIdx.toInt(), true)
                Toast.makeText(this, "Đã chuyển đến: ${bookmark.title}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Đóng", null)
            .show()
    }
    
    // Show document information
    private fun showDocumentInfo() {
        try {
            val meta = binding.pdfViewer.documentMeta
            val info = StringBuilder()
            
            info.append("Tên file: ${file?.name ?: "Không rõ"}\n")
            info.append("Số page: $totalPages\n")
            info.append("Page hiện tại: ${currentPage + 1}\n")
            info.append("Zoom: ${(currentZoom * 100).toInt()}%\n")
            
            meta?.let {
                info.append("Tiêu đề: ${it.title ?: "Không rõ"}\n")
                info.append("Tác giả: ${it.author ?: "Không rõ"}\n")
                info.append("Chủ đề: ${it.subject ?: "Không rõ"}\n")
                info.append("Từ khóa: ${it.keywords ?: "Không rõ"}\n")
                info.append("Người tạo: ${it.creator ?: "Không rõ"}\n")
                info.append("Nhà sản xuất: ${it.producer ?: "Không rõ"}\n")
                info.append("Ngày tạo: ${it.creationDate ?: "Không rõ"}\n")
                info.append("Ngày sửa đổi: ${it.modDate ?: "Không rõ"}\n")
            }
            
            AlertDialog.Builder(this)
                .setTitle("Thông tin tài liệu")
                .setMessage(info.toString())
                .setPositiveButton("Đóng", null)
                .show()
                
        } catch (e: Exception) {
            Log.e("PDF_VIEWER", "Error getting document info: ${e.message}")
            Toast.makeText(this, "Không thể lấy thông tin tài liệu", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Navigation methods
    fun goToNextPage() {
        if (currentPage < totalPages - 1) {
            binding.pdfViewer.jumpTo(currentPage + 1, true)
        }
    }
    
    fun goToPreviousPage() {
        if (currentPage > 0) {
            binding.pdfViewer.jumpTo(currentPage - 1, true)
        }
    }
    
    fun goToFirstPage() {
        binding.pdfViewer.jumpTo(0, true)
    }
    
    fun goToLastPage() {
        binding.pdfViewer.jumpTo(totalPages - 1, true)
    }
    
    // Zoom methods
    fun resetZoom() {
        currentZoom = 1.0f
        binding.pdfViewer.resetZoom()
        binding.pdfViewer.invalidate() // Force immediate refresh
        Toast.makeText(this, "Đã đặt lại zoom", Toast.LENGTH_SHORT).show()
    }
    
    fun fitToWidth() {
        binding.pdfViewer.fitToWidth(currentPage)
        binding.pdfViewer.invalidate() // Force immediate refresh
        Toast.makeText(this, "Đã vừa với chiều rộng", Toast.LENGTH_SHORT).show()
    }
    
    // Bookmark methods
    private fun toggleBookmark() {
        file?.let { fileItem ->
            lifecycleScope.launch {
                try {
                    val isBookmarked = bookmarkRepository.isPageBookmarked(fileItem.path, currentPage)
                    
                    if (isBookmarked) {
                        // Remove bookmark
                        bookmarkRepository.deleteBookmarkByFileAndPage(fileItem.path, currentPage)
                        Toast.makeText(this@OpenFilePdfActivity, "Đã xóa bookmark page ${currentPage + 1}", Toast.LENGTH_SHORT).show()
                    } else {
                        // Add bookmark
                        val bookmark = BookmarkEntity(
                            filePath = fileItem.path,
                            fileName = fileItem.name,
                            pageNumber = currentPage,
                            title = "Page ${currentPage + 1}",
                            note = null,
                            createdTime = System.currentTimeMillis(),
                            fileUri = fileItem.uri?.toString()
                        )
                        bookmarkRepository.insertBookmark(bookmark)
                        Toast.makeText(this@OpenFilePdfActivity, "Đã thêm bookmark page ${currentPage + 1}", Toast.LENGTH_SHORT).show()
                    }
                    
                    updateBookmarkIcon()
                } catch (e: Exception) {
                    Log.e("BOOKMARK", "Error toggling bookmark: ${e.message}")
                    Toast.makeText(this@OpenFilePdfActivity, "Lỗi khi xử lý bookmark", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun updateBookmarkIcon() {
        file?.let { fileItem ->
            lifecycleScope.launch {
                try {
                    val isBookmarked = bookmarkRepository.isPageBookmarked(fileItem.path, currentPage)
                    
                    // Update bookmark icon based on bookmark status
                    if (isBookmarked) {
                        binding.icBookmark.setColorFilter(
                            androidx.core.content.ContextCompat.getColor(this@OpenFilePdfActivity, R.color.primary)
                        )
                    } else {
                        binding.icBookmark.clearColorFilter()
                    }
                } catch (e: Exception) {
                    Log.e("BOOKMARK", "Error updating bookmark icon: ${e.message}")
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Clean up PDF viewer resources
        binding.pdfViewer.recycle()
    }
}