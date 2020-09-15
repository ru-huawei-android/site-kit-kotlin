package com.huawei.sitekit.kotlin.fragment

import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.huawei.hms.site.api.SearchResultListener
import com.huawei.hms.site.api.SearchService
import com.huawei.hms.site.api.SearchServiceFactory
import com.huawei.hms.site.api.model.*
import com.huawei.sitekit.kotlin.R
import com.huawei.sitekit.kotlin.adapter.SiteAdapter
import com.huawei.sitekit.kotlin.adapter.SiteAdapter.SiteCallback
import com.huawei.sitekit.kotlin.adapter.SiteObservable
import com.huawei.sitekit.kotlin.common.AndroidUtils
import com.huawei.sitekit.kotlin.common.Config
import com.huawei.sitekit.kotlin.common.InputFilterMinMax
import kotlinx.android.synthetic.main.fragment_keyword_search.view.*
import kotlinx.android.synthetic.main.fragment_nearby_search.view.*
import kotlinx.android.synthetic.main.fragment_nearby_search.view.buttonFilter
import kotlinx.android.synthetic.main.fragment_nearby_search.view.buttonSearch
import kotlinx.android.synthetic.main.fragment_nearby_search.view.constraintLayoutFilter
import kotlinx.android.synthetic.main.fragment_nearby_search.view.constraintLayoutKeyword
import kotlinx.android.synthetic.main.fragment_nearby_search.view.editTextKeywordQuery
import kotlinx.android.synthetic.main.fragment_nearby_search.view.editTextLocationLatitude
import kotlinx.android.synthetic.main.fragment_nearby_search.view.editTextLocationLongitude
import kotlinx.android.synthetic.main.fragment_nearby_search.view.editTextRadius
import kotlinx.android.synthetic.main.fragment_nearby_search.view.recyclerViewResult
import kotlinx.android.synthetic.main.fragment_nearby_search.view.spinnerLocationType

class NearbySearchFragment : Fragment(), SiteCallback {

    // Declare a SearchService object.
    private val searchService: SearchService by lazy {
        SearchServiceFactory.create(context, Config.getAgcApiKey(context))
    }

    private val adapterResult: SiteAdapter by lazy {
        SiteAdapter()
    }

    private var converterLocationType: Map<String, LocationType> = emptyMap()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_nearby_search, container, false)

        view.editTextRadius.filters = arrayOf<InputFilter>(InputFilterMinMax(1, 50000))
        view.buttonFilter.setOnClickListener {
            AndroidUtils.changeFilterVisible(
                view.constraintLayoutKeyword,
                view.constraintLayoutFilter
            )
        }

        view.buttonSearch.setOnClickListener { search() }
        adapterResult.setCallback(this)

        view.recyclerViewResult.apply {
            adapter = adapterResult
            layoutManager = LinearLayoutManager(context)
        }

        converterLocationType = LocationType.values().associateBy { it.name }
        val data = converterLocationType.keys.toTypedArray()
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, data).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        view.spinnerLocationType.adapter = adapter

        return view
    }

    /**
     * Search method
     */
    private fun search() {
        val latitude = view?.editTextLocationLatitude?.text
            ?.toString()?.takeUnless { it.isEmpty() }?.toDouble()
        val longitude = view?.editTextLocationLongitude?.text
            ?.toString()?.takeUnless { it.isEmpty() }?.toDouble()
        val radiusValue = view?.editTextRadius?.text?.toString()
            ?.takeUnless { it.isEmpty() }?.toInt()
        val locationType = view?.spinnerLocationType?.selectedItem as? String
        val queryText = view?.editTextKeywordQuery?.text?.toString()

        val request = NearbySearchRequest().apply {

            if (latitude != null && longitude != null) {
                location = Coordinate(latitude.toDouble(), longitude.toDouble())
            }

            query = queryText
            radius = radiusValue
            poiType = converterLocationType[locationType]
            language = "en"
            pageSize = 10
        }

        searchService.nearbySearch(request, resultListener)
    }

    private var resultListener: SearchResultListener<NearbySearchResponse> =
        object : SearchResultListener<NearbySearchResponse> {

            override fun onSearchResult(results: NearbySearchResponse) {
                val observables = results.sites.map { SiteObservable.fromSite(it) }
                adapterResult.setList(observables)
            }

            override fun onSearchError(status: SearchStatus) {
                adapterResult.setList(emptyList())
            }
        }

    override fun onSiteItemClicked(observable: SiteObservable) {
        val message = "Site ID " + observable.siteId + " has been saved to clipboard."
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        AndroidUtils.saveToClipboard(requireContext(), observable.siteId)
    }
}