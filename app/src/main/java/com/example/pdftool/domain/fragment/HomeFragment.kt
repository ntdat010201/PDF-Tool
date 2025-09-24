package com.example.pdftool.domain.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pdftool.apdater.PDFFileAdapter
import com.example.pdftool.databinding.FragmentHomeBinding
import com.example.pdftool.domain.activities.OpenFilePdfActivity
import com.example.pdftool.domain.dialog.DialogEditFile
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
        initData()
        initView()
        initListener()
        return binding.root
    }

    private fun initData() {
        pdfFileAdapter = PDFFileAdapter(emptyList())
        binding.rcvHome.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pdfFileAdapter
        }

    }

    private fun initView() {
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
                Toast.makeText(
                    context,
                    "Cần quyền truy cập file để hiển thị PDF",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun initListener(){
        // Set click listeners
        pdfFileAdapter?.onItemClickItem = { fileItem ->
            val intent = Intent(requireContext(), OpenFilePdfActivity::class.java)
            intent.putExtra("data_pdf", fileItem)
            startActivity(intent)
        }

        pdfFileAdapter?.onItemClickMore = { fileItem ->

            val dialogEditFile = DialogEditFile(fileItem) {
                // Callback để refresh danh sách khi file thay đổi
                fileViewModel.refreshPDFFiles()
            }
            dialogEditFile.show(parentFragmentManager, dialogEditFile.tag)

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