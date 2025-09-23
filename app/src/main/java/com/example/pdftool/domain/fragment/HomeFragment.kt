package com.example.pdftool.domain.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.pdftool.R
import com.example.pdftool.apdater.PDFFileAdapter
import com.example.pdftool.databinding.FragmentHomeBinding
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
        return binding.root
    }

    private fun initData() {

    }

}