package com.example.pdftool.domain.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider

import com.example.pdftool.databinding.FragmentDialogRenameFileBinding
import com.example.pdftool.model.ModelFileItem
import com.example.pdftool.viewmodel.FileViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.ext.android.inject
import java.io.File


class DialogRenameFile(
    private var file: ModelFileItem,
    private var onFileRenamed: (() -> Unit)? = null
) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentDialogRenameFileBinding
    private val fileViewModel by inject<FileViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDialogRenameFileBinding.inflate(LayoutInflater.from(requireContext()))
        initData()
        initView()
        initListener()
        return binding.root
    }

    private fun initData() {
    }

    private fun initView() {
        val nameWithoutExtension = File(file.path).nameWithoutExtension
        binding.edtFileName.setText(nameWithoutExtension)
    }

    private fun initListener() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.btnOk.setOnClickListener {
            val newFileName = binding.edtFileName.text.toString().trim()
            if (newFileName.isNotEmpty()) {
                // Gọi hàm đổi tên và giữ nguyên phần mở rộng
                val success = fileViewModel.renameFile(file, newFileName)
                if (success) {
                    Toast.makeText(requireContext(), "đổi tên thành công", Toast.LENGTH_SHORT).show()
                    // Gọi callback để thông báo cho fragment refresh
                    onFileRenamed?.invoke()
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), "không thể đổi tên", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "không thể để trống", Toast.LENGTH_SHORT).show()
            }
        }
    }


}