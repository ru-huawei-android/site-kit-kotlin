package com.huawei.sitekit.kotlin.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.huawei.hms.site.api.SearchResultListener
import com.huawei.hms.site.api.SearchService
import com.huawei.hms.site.api.SearchServiceFactory
import com.huawei.hms.site.api.model.DetailSearchRequest
import com.huawei.hms.site.api.model.DetailSearchResponse
import com.huawei.hms.site.api.model.SearchStatus
import com.huawei.sitekit.kotlin.R
import com.huawei.sitekit.kotlin.common.Config
import kotlinx.android.synthetic.main.fragment_place_detail.view.*

class PlaceDetailFragment : Fragment() {

    // Declare a SearchService object.
    private val searchService: SearchService by lazy {
        SearchServiceFactory.create(context, Config.getAgcApiKey(context))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_place_detail, container,false).also {
        view -> view.buttonSearch.setOnClickListener { search() }
    }

    /**
     * Search method
     */
    private fun search() {
        val siteIdValue = view?.editTextSiteId?.text.toString().trim { it <= ' ' }

        val request = DetailSearchRequest().apply {
            language = "en"
            siteId = siteIdValue
        }

        searchService.detailSearch(request, resultListener)
    }

    private var resultListener: SearchResultListener<DetailSearchResponse> =
        object : SearchResultListener<DetailSearchResponse> {

            @SuppressLint("SetTextI18n")
            override fun onSearchResult(results: DetailSearchResponse) {
                val site = results.site
                val poiTypes = site.poi.hwPoiTypes.joinToString(", ")

                view?.textViewDetailResult?.text =
                    "SITE ID: ${site.siteId}" +
                    "NAME: ${site.name}" +
                    "ADDRESS: ${site.formatAddress}" +
                    "LAT: ${site.location.lat} LNG: ${site.location.lng}" +
                    "POI: $poiTypes" +
                    "DISTANCE: ${site.distance}"
            }

            override fun onSearchError(status: SearchStatus) {
                Log.e(TAG, "Error: " + status.errorCode + " - " + status.errorMessage)
                view?.textViewDetailResult?.text = ""
            }
        }

    companion object {
        private const val TAG = "PLACE_DETAIL_FRAGMENT"
    }
}