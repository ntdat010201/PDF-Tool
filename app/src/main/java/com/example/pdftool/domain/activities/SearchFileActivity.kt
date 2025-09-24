package com.example.pdftool.domain.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pdftool.apdater.SearchFileAdapter
import com.example.pdftool.base.BaseActivity
import com.example.pdftool.databinding.ActivitySearchFileBinding
import com.example.pdftool.domain.dialog.DialogEditFile
import com.example.pdftool.model.ModelFileItem
import com.example.pdftool.viewmodel.FileViewModel
import org.koin.android.ext.android.inject

class SearchFileActivity : BaseActivity() {
    private lateinit var binding: ActivitySearchFileBinding
    private val fileViewModel by inject<FileViewModel>()
    private var searchFileAdapter: SearchFileAdapter? = null
    private var originalFiles: ArrayList<ModelFileItem> = arrayListOf()
    private var currentQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchFileBinding.inflate(LayoutInflater.from(this))
        initData()
        initView()
        initListener()
        setContentView(binding.root)
    }

    private fun initData() {
        val files: ArrayList<ModelFileItem>? = intent.getParcelableArrayListExtra("data")
        binding.rcvSearchFile.layoutManager = LinearLayoutManager(this)
        searchFileAdapter = SearchFileAdapter()
        binding.rcvSearchFile.adapter = searchFileAdapter

        if (files != null) {
            originalFiles = files
            searchFile(files)
        }
        
        // Observe FileViewModel for updates after rename/delete
        fileViewModel.pdfFiles.observe(this) { updatedFiles ->
            originalFiles = ArrayList(updatedFiles)
            // Re-apply current search query if any
            if (currentQuery.isNotEmpty()) {
                performSearch(currentQuery)
            }
        }
    }

    private fun initView() {

        binding.searchView.isIconified = false
        binding.searchView.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.searchView, InputMethodManager.SHOW_IMPLICIT)

        binding.searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val im = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                im.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
            }
        }
    }

    private fun initListener() {
        binding.backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        searchFileAdapter?.onItemClickMore = { file ->
            val dialogEditFile = DialogEditFile(file) {
                // Callback để refresh danh sách khi file thay đổi
                fileViewModel.refreshPDFFiles()
            }
            dialogEditFile.show(supportFragmentManager, dialogEditFile.tag)
        }

        searchFileAdapter?.onItemClickItem = { file ->
            val intent = Intent(this, OpenFilePdfActivity::class.java)
            intent.putExtra("data_pdf", file)
            startActivity(intent)

        }

    }

    private fun searchFile(files: ArrayList<ModelFileItem>) {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                currentQuery = newText ?: ""
                performSearch(currentQuery)
                return true
            }
        })
    }

    private fun performSearch(query: String) {
        val mList = ArrayList<ModelFileItem>()

        if (query.isNotEmpty()) {
            val userInput = query.lowercase()
            for (file in originalFiles) {
                if (file.name.lowercase().contains(userInput)) {
                    mList.add(file)
                }
            }
        }

        searchFileAdapter?.updateFiles(mList)

        if (mList.isEmpty()) {
            binding.rcvSearchFile.visibility = View.GONE
        } else {
            binding.rcvSearchFile.visibility = View.VISIBLE
        }
    }

}