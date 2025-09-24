package com.example.pdftool.domain.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pdftool.apdater.PDFFileRecentAdapter
import com.example.pdftool.databinding.FragmentRecentBinding
import com.example.pdftool.domain.activities.OpenFilePdfActivity
import com.example.pdftool.domain.dialog.DialogEditRecentFile
import com.example.pdftool.viewmodel.FileViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class RecentFragment : Fragment() {
    private lateinit var binding: FragmentRecentBinding
    private var pdfFileRecentAdapter: PDFFileRecentAdapter? = null
    private val fileViewModel by viewModel<FileViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecentBinding.inflate(layoutInflater)
        initData()
        initView()
        initListener()
        return binding.root
    }

    private fun initData() {
        pdfFileRecentAdapter = PDFFileRecentAdapter(emptyList())
        binding.rcvRecent.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pdfFileRecentAdapter
        }
    }

    private fun initView() {
        // Observe recent files LiveData
        fileViewModel.recentFiles.observe(viewLifecycleOwner) { files ->
            pdfFileRecentAdapter?.updateFiles(files)
        }

        // Observe error messages
        fileViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initListener() {
        // Set click listeners
        pdfFileRecentAdapter?.onItemClickItem = { fileItem ->
            val intent = Intent(requireContext(), OpenFilePdfActivity::class.java)
            intent.putExtra("data_pdf", fileItem)
            startActivity(intent)
        }

        pdfFileRecentAdapter?.onItemClickMore = { fileItem ->
            val dialogEditRecentFile = DialogEditRecentFile(fileItem) {
                // Callback để refresh danh sách recent files
                // Không cần refresh PDF files vì chỉ xóa khỏi recent database
            }
            dialogEditRecentFile.show(parentFragmentManager, dialogEditRecentFile.tag)
        }
    }
}