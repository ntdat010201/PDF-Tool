package com.example.pdftool.domain.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.pdftool.R
import com.example.pdftool.apdater.ViewpagerActivityAdapter
import com.example.pdftool.base.BaseActivity
import com.example.pdftool.databinding.ActivityMainBinding
import com.example.pdftool.domain.fragment.BookmarksFragment
import com.example.pdftool.domain.fragment.HomeFragment
import com.example.pdftool.domain.fragment.RecentFragment
import com.example.pdftool.utils.PermissionHelper
import org.koin.android.ext.android.inject

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding

    private var adapter: ViewpagerActivityAdapter? = null

    private val homeFragment by inject<HomeFragment>()
    private val recentFragment by inject<RecentFragment>()
    private val bookmarksFragment by inject<BookmarksFragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Request permissions before initializing UI
        requestStoragePermissions()
        
        initData()
        initView()
        initListener()
    }

    private fun initData() {
        refreshViewPager()
        viewPagerWithNav()
    }

    private fun initView() {

    }

    private fun initListener() {

    }


    private fun refreshViewPager() {
        adapter = ViewpagerActivityAdapter(this)
        adapter!!.setFragments(
            homeFragment, recentFragment, bookmarksFragment
        )
        binding.viewPager2.adapter = adapter
        binding.viewPager2.offscreenPageLimit = 3
        binding.viewPager2.isUserInputEnabled = false
    }

    private fun viewPagerWithNav() {
        binding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> {
                        binding.bottomNavigation.menu.findItem(R.id.nav_home).isChecked = true
                    }

                    1 -> {
                        binding.bottomNavigation.menu.findItem(R.id.nav_recent).isChecked = true
                    }

                    2 -> {
                        binding.bottomNavigation.menu.findItem(R.id.nav_booksmarks).isChecked = true
                    }

                    else -> {
                        binding.bottomNavigation.menu.findItem(R.id.nav_home).isChecked = true
                    }
                }
            }
        })

        binding.bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    binding.viewPager2.currentItem = 0
                    true
                }

                R.id.nav_recent -> {
                    binding.viewPager2.currentItem = 1
                    true
                }

                R.id.nav_booksmarks -> {
                    binding.viewPager2.currentItem = 2
                    true
                }

                else -> false
            }
        }
    }
    
    private fun requestStoragePermissions() {
        if (!PermissionHelper.hasBasicStoragePermission(this)) {
            PermissionHelper.requestAllPermissions(this)
        } else {
            Toast.makeText(this, "Quyền truy cập file đã được cấp", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        val permissionGranted = PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (permissionGranted) {
            Toast.makeText(this, "Quyền truy cập file đã được cấp", Toast.LENGTH_SHORT).show()
            // Notify HomeFragment about permission granted
            homeFragment.onPermissionGranted()
        } else {
            Toast.makeText(this, PermissionHelper.getPermissionMessage(this), Toast.LENGTH_LONG).show()
            // Notify HomeFragment about permission denied
            homeFragment.onPermissionDenied()
            
            if (!PermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // User permanently denied permissions
                PermissionHelper.openAppSettings(this)
            }
        }
    }
}