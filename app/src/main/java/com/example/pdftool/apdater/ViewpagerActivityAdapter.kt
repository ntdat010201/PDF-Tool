package com.example.pdftool.apdater

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.pdftool.domain.fragment.BookmarksFragment
import com.example.pdftool.domain.fragment.HomeFragment
import com.example.pdftool.domain.fragment.RecentFragment

class ViewpagerActivityAdapter(
    fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity) {

    private var homeFragment: HomeFragment? = null
    private var recentFragment: RecentFragment? = null
    private var bookmarksFragment: BookmarksFragment? = null


    fun setFragments(
        homeFragment: HomeFragment,
        recentFragment: RecentFragment,
        bookmarksFragment: BookmarksFragment
    ) {
        this.homeFragment = homeFragment
        this.recentFragment = recentFragment
        this.bookmarksFragment = bookmarksFragment

    }

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> homeFragment!!
            1 -> recentFragment!!
            2 -> bookmarksFragment!!
            else -> homeFragment!!
        }

    }
}