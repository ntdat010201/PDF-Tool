package com.example.pdftool.domain.dialog

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.pdftool.databinding.FragmentDialogEditRecentFileBinding
import com.example.pdftool.model.ModelFileItem
import com.example.pdftool.viewmodel.FileViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class DialogEditRecentFile(
    private var file: ModelFileItem,
    private var onFileChanged: (() -> Unit)? = null
) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentDialogEditRecentFileBinding
    private val fileViewModel by viewModel<FileViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDialogEditRecentFileBinding.inflate(LayoutInflater.from(requireContext()))
        initData()
        initView()
        initListener()
        return binding.root
    }

    private fun initData() {
    }

    private fun initView() {
        binding.fileName.text = file.name
        binding.textPath.text = file.path
    }

    private fun initListener() {
        detailsFile()
        shareFile()
        removeFromRecent()
    }

    private fun removeFromRecent() {
        binding.delete.setOnClickListener {
            // Remove from recent database only, not delete the actual file
            fileViewModel.removeRecentFile(file.path)
            Toast.makeText(requireContext(), "Đã xóa khỏi danh sách gần đây", Toast.LENGTH_SHORT).show()
            
            // Callback to refresh the recent files list
            onFileChanged?.invoke()
            dismiss()
        }
    }

    private fun shareFile() {
        binding.share.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "file/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, file.uri)
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ file"))
            Toast.makeText(requireContext(), "Đang tải...", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    private fun detailsFile() {
        binding.details.setOnClickListener {
            val dialogDetailsFile = DialogDetailsFile(file)
            dialogDetailsFile.show(parentFragmentManager, dialogDetailsFile.tag)
            dismiss()
        }
    }
}