package com.example.pdftool.domain.fragment

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pdftool.R
import com.example.pdftool.apdater.PDFFileAdapter
import com.example.pdftool.databinding.FragmentHomeBinding
import com.example.pdftool.model.ModelFileItem
import com.example.pdftool.viewmodel.FileViewModel
import org.koin.android.ext.android.inject


class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private var pdfFileAdapter: PDFFileAdapter? = null

    private val fileViewModel by inject<FileViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        initRecyclerView()
        observeViewModel()
        return binding.root
    }
    
    private fun initRecyclerView() {
        pdfFileAdapter = PDFFileAdapter(emptyList())
        binding.rcvHome.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pdfFileAdapter
        }
        
        // Set click listeners
        pdfFileAdapter?.onItemClickItem = { fileItem ->
            // Handle PDF file click - open PDF viewer
            Toast.makeText(context, "Mở file: ${fileItem.name}", Toast.LENGTH_SHORT).show()
        }
        
        pdfFileAdapter?.onItemClickMore = { fileItem ->
            // Handle more options click
            Toast.makeText(context, "Tùy chọn cho: ${fileItem.name}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun observeViewModel() {
        // Observe PDF files LiveData
        fileViewModel.pdfFiles.observe(viewLifecycleOwner) { files ->
            pdfFileAdapter?.updateFiles(files)
        }
        
        // Observe loading state
        fileViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // TODO: Show/hide loading indicator
            // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        // Observe error messages
        fileViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        }
        
        // Observe permission status
        fileViewModel.hasPermissions.observe(viewLifecycleOwner) { hasPermissions ->
            if (!hasPermissions) {
                Toast.makeText(context, "Cần quyền truy cập file để hiển thị PDF", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Check permissions and refresh if needed
        fileViewModel.checkPermissions()
        fileViewModel.refreshPDFFiles()
    }
    
    /**
     * Call this method when permissions are granted from MainActivity
     */
    fun onPermissionGranted() {
        fileViewModel.onPermissionGranted()
    }
    
    /**
     * Call this method when permissions are denied from MainActivity
     */
    fun onPermissionDenied() {
        fileViewModel.onPermissionDenied()
    }

}