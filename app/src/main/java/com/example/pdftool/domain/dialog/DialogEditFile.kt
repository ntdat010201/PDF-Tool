package com.example.pdftool.domain.dialog

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.pdftool.databinding.FragmentDialogEditFileBinding
import com.example.pdftool.model.ModelFileItem
import com.example.pdftool.viewmodel.FileViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.ext.android.inject


class DialogEditFile(
    private var file: ModelFileItem,
    private var onFileChanged: (() -> Unit)? = null
) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentDialogEditFileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDialogEditFileBinding.inflate(LayoutInflater.from(requireContext()))
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
        renameFile()
        shareFile()
        deletesFile()

    }

    private fun deletesFile() {
        binding.delete.setOnClickListener {
            val dialogDeleteFile = DialogDeleteFile(file) {
                // Callback khi file được xóa thành công
                onFileChanged?.invoke()
            }
            dialogDeleteFile.show(parentFragmentManager, dialogDeleteFile.tag)
            dismiss()
        }
    }

    private fun shareFile() {
        binding.share.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "file/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, file)
            startActivity(Intent.createChooser(shareIntent, "Sharing File!!"))
            Toast.makeText(requireContext(), "loading...", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    private fun renameFile() {
        binding.rename.setOnClickListener {
            val dialogRenameFile = DialogRenameFile(file) {
                // Callback khi file được rename thành công
                onFileChanged?.invoke()
            }
            dialogRenameFile.show(parentFragmentManager, dialogRenameFile.tag)
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