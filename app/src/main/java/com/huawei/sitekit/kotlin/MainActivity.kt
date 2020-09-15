package com.huawei.sitekit.kotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.tabs.TabLayoutMediator.TabConfigurationStrategy
import com.huawei.sitekit.kotlin.fragment.KeywordSearchFragment
import com.huawei.sitekit.kotlin.fragment.NearbySearchFragment
import com.huawei.sitekit.kotlin.fragment.PlaceDetailFragment
import com.huawei.sitekit.kotlin.fragment.QuerySuggestionFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), TabConfigurationStrategy {

    private lateinit var tabTitles: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolBar)

        // TabLayout titles
        tabTitles = resources.getStringArray(R.array.tabs_title)

        // Setup adapter
        viewPager.adapter = SearchViewPagerAdapter(this)

        // Setup interaction
        TabLayoutMediator(tabLayout, viewPager, this).attach()
    }

    override fun onConfigureTab(tab: TabLayout.Tab, position: Int) {
        tab.text = tabTitles[position]
        viewPager.setCurrentItem(tab.position, true)
    }

    class SearchViewPagerAdapter(
        fragmentActivity: FragmentActivity
    ) : FragmentStateAdapter(fragmentActivity) {

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                KEYWORD_SEARCH_INDEX -> KeywordSearchFragment()
                NEARBY_SEARCH_INDEX -> NearbySearchFragment()
                PLACE_DETAIL_INDEX -> PlaceDetailFragment()
                else -> QuerySuggestionFragment()
            }
        }

        override fun getItemCount() = PAGE_COUNT

        companion object {
            private const val PAGE_COUNT = 4

            const val KEYWORD_SEARCH_INDEX = 0
            const val NEARBY_SEARCH_INDEX = 1
            const val PLACE_DETAIL_INDEX = 2
        }
    }
}