package com.huawei.sitekit.kotlin.fragment

import android.os.Bundle
import android.text.InputFilter
import android.util.Log
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
import kotlinx.android.synthetic.main.fragment_nearby_search.view.*

class NearbySearchFragment : Fragment(), SiteCallback {

    // Declare a SearchService object.
    private val searchService: SearchService by lazy {
        SearchServiceFactory.create(context, Config.getAgcApiKey(context))
    }

    private val adapterResult: SiteAdapter by lazy {
        SiteAdapter()
    }

    private val converterLocationType = hashMapOf<String, LocationType?>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_nearby_search, container, false)

        view.editTextRadius.filters = arrayOf<InputFilter>(InputFilterMinMax(1, 50000))
        view.buttonSearch.setOnClickListener { search() }

        adapterResult.setCallback(this)

        view.recyclerViewResult.apply {
            adapter = adapterResult
            layoutManager = LinearLayoutManager(context)
        }

        LocationType.values().associateBy { it.name }.toMap(converterLocationType)

        val data = converterLocationType.keys.toMutableList().apply {
            add(0, Config.DEFAULT_LOCATION_TYPE)
        }.toTypedArray()

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

        if (latitude == null || longitude == null) {
            Toast.makeText(context, getString(R.string.enter_location), Toast.LENGTH_SHORT).show()
            return
        }

        val radiusValue = view?.editTextRadius?.text?.toString()
            ?.takeUnless { it.isEmpty() }?.toInt()
        val locationType = view?.spinnerLocationType?.selectedItem
            ?.toString()?.takeUnless { it == Config.DEFAULT_LOCATION_TYPE }
        val queryText = view?.editTextKeywordQuery?.text?.toString()

        val request = NearbySearchRequest().apply {
            location = Coordinate(latitude, longitude)
            query = queryText
            radius = radiusValue
            poiType = converterLocationType[locationType]
            language = Config.DEFAULT_LANGUAGE
            pageSize = Config.DEFAULT_PAGE_COUNT
        }

        searchService.nearbySearch(request, resultListener)
    }

    private var resultListener: SearchResultListener<NearbySearchResponse> =
        object : SearchResultListener<NearbySearchResponse> {

            override fun onSearchResult(results: NearbySearchResponse) {
                val observables = results.sites.map { SiteObservable.fromSite(it) }
                adapterResult.setList(observables)

                if (observables.isEmpty()) {
                    Toast.makeText(context, R.string.empty_response, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onSearchError(status: SearchStatus) {
                val message = "Error: " + status.errorCode
                Log.e(TAG, message)
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                adapterResult.setList(emptyList())
            }
        }

    override fun onSiteItemClicked(observable: SiteObservable) {
        val message = "Site ID " + observable.siteId + " has been saved to clipboard."
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        AndroidUtils.saveToClipboard(requireContext(), observable.siteId)
    }

    companion object {
        private const val TAG = "NEARBY_SEARCH_FRAGMENT"
    }
}