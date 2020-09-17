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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_query_suggestion, container, false)

        adapterResult.setCallback(this)

        view.editTextKeywordQuery.apply {

            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {}
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    search()
                }
            })
        }

        view.editTextRadius.filters = arrayOf<InputFilter>(InputFilterMinMax(1, 50000))

        view.recyclerViewResult.apply {
            adapter = adapterResult
            layoutManager = LinearLayoutManager(context)
        }

        val data = LocationType.values().map { it.name }.toMutableList().apply {
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
        val queryText =  view?.editTextKeywordQuery?.text?.toString()?.trim { it <= ' ' }

        val latitude = view?.editTextLocationLatitude?.text?.toString()
            ?.takeUnless { it.isEmpty() }?.toDouble()
        val longitude = view?.editTextLocationLongitude?.text?.toString()
            ?.takeUnless { it.isEmpty() }?.toDouble()
        val radiusValue = view?.editTextRadius?.text?.toString()
            ?.takeUnless { it.isEmpty() }?.toInt()
        val locationType = view?.spinnerLocationType?.selectedItem
            ?.toString()?.takeUnless { it == Config.DEFAULT_LOCATION_TYPE }

        val request = QuerySuggestionRequest().apply {
            query = queryText

            if (latitude != null && longitude != null) {
                location = Coordinate(latitude.toDouble(), longitude.toDouble())
            }

            poiTypes = locationType?.let { listOf(LocationType.valueOf(locationType)) }
            radius = radiusValue
            countryCode = Config.DEFAULT_COUNTRY_CODE
            language = Config.DEFAULT_LANGUAGE
        }

        searchService.querySuggestion(request, resultListener)
    }

    private var resultListener: SearchResultListener<QuerySuggestionResponse> =
        object : SearchResultListener<QuerySuggestionResponse> {

            override fun onSearchResult(results: QuerySuggestionResponse) {
                val observables = results.sites?.map { SiteObservable.fromSite(it) } ?: emptyList()
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
        private const val TAG = "QUERY_SUGGEST_FRAGMENT"
    }
}