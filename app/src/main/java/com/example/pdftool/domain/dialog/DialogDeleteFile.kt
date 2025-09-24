package com.example.pdftool.domain.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.pdftool.databinding.FragmentDialogDeleteFileBinding
import com.example.pdftool.model.ModelFileItem
import com.example.pdftool.viewmodel.FileViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.ext.android.inject

class DialogDeleteFile(
    private var file: ModelFileItem,
    private var onFileDeleted: (() -> Unit)? = null
) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentDialogDeleteFileBinding
    private val fileViewModel by inject<FileViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentDialogDeleteFileBinding.inflate(LayoutInflater.from(requireContext()))
        initData()
        initView()
        initListener()
        return binding.root
    }

    private fun initData() {
    }

    private fun initView() {
        binding.fileName.text = file.name
        binding.filePath.text = file.path
    }

    private fun initListener() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.btnOk.setOnClickListener {
            val success = fileViewModel.deleteFile(file)
            if (success) {
                Toast.makeText(requireContext(), "xóa thành công ", Toast.LENGTH_SHORT).show()
                // Gọi callback để thông báo cho fragment refresh
                onFileDeleted?.invoke()
            } else {
                Toast.makeText(requireContext(), "không thể xóa file ", Toast.LENGTH_SHORT)
                    .show()
            }
            dismiss()
        }
    }


}