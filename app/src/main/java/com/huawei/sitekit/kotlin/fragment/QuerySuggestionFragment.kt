package com.huawei.sitekit.kotlin.fragment

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
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
import kotlinx.android.synthetic.main.fragment_query_suggestion.*
import kotlinx.android.synthetic.main.fragment_query_suggestion.view.*
import java.util.*

class QuerySuggestionFragment : Fragment(), SiteCallback {

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
        val view = inflater.inflate(R.layout.fragment_keyword_search, container, false)

        view.editTextRadius.filters = arrayOf<InputFilter>(InputFilterMinMax(1, 50000))
        view.buttonFilter.setOnClickListener {
            AndroidUtils.changeFilterVisible(constraintLayoutKeyword, constraintLayoutFilter)
        }

        view.editTextKeywordQuery.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                search()
            }
        })

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
        val latitude = view?.editTextLocationLatitude?.text?.toString()
            ?.takeUnless { it.isEmpty() }?.toDouble()
        val longitude = view?.editTextLocationLongitude?.text?.toString()
            ?.takeUnless { it.isEmpty() }?.toDouble()
        val radiusValue = view?.editTextRadius?.text?.toString()
            ?.takeUnless { it.isEmpty() }?.toInt()
        val locationType = spinnerLocationType.selectedItem as String

        val request = TextSearchRequest().apply {
            query = editTextKeywordQuery.text.toString().trim { it <= ' ' }

            if (latitude != null && longitude != null) {
                location = Coordinate(latitude.toDouble(), longitude.toDouble())
            }

            radius = radiusValue
            poiType = converterLocationType[locationType]
            countryCode = "En"
            language = "en"
            pageSize = 10
        }

        searchService.textSearch(request, resultListener)
    }

    private var resultListener: SearchResultListener<TextSearchResponse> =
        object : SearchResultListener<TextSearchResponse> {

            override fun onSearchResult(results: TextSearchResponse) {
                val observables = results.sites.map { SiteObservable.fromSite(it) }
                adapterResult.setList(observables)
            }

            override fun onSearchError(status: SearchStatus) {
                Log.e(TAG, "Error: " + status.errorCode + " - " + status.errorMessage)
                adapterResult.setList(ArrayList())
            }
        }

    override fun onSiteItemClicked(observable: SiteObservable) {
        val message = "Site ID " + observable.siteId + " has been saved to clipboard."
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        AndroidUtils.saveToClipboard(requireContext(), observable.siteId)
    }

    companion object {
        private const val TAG = "KEYWORD_SEARCH_FRAGMENT"
    }
}