package com.example.pdftool.domain.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.pdftool.databinding.DialogSortBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SortBottomSheetDialog : BottomSheetDialogFragment() {
    
    private var _binding: DialogSortBottomSheetBinding? = null
    private val binding get() = _binding!!
    
    private var onSortSelected: ((String) -> Unit)? = null
    
    fun setOnSortSelectedListener(listener: (String) -> Unit) {
        onSortSelected = listener
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogSortBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        binding.sortAZ.setOnClickListener {
            onSortSelected?.invoke("az")
            dismiss()
        }
        
        binding.sortZA.setOnClickListener {
            onSortSelected?.invoke("za")
            dismiss()
        }
        
        binding.sortNewestToOldest.setOnClickListener {
            onSortSelected?.invoke("nto")
            dismiss()
        }
        
        binding.sortOldestToNewest.setOnClickListener {
            onSortSelected?.invoke("otn")
            dismiss()
        }
        
        binding.sortBigToSmall.setOnClickListener {
            onSortSelected?.invoke("bts")
            dismiss()
        }
        
        binding.sortSmallToBig.setOnClickListener {
            onSortSelected?.invoke("stb")
            dismiss()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        const val TAG = "SortBottomSheetDialog"
        
        fun newInstance(): SortBottomSheetDialog {
            return SortBottomSheetDialog()
        }
    }
}