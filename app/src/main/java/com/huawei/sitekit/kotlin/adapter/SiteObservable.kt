package com.huawei.sitekit.kotlin.adapter

import com.huawei.hms.site.api.model.Site

data class SiteObservable(
    val siteId: String,
    val name: String,
    val location: String,
    val coordinates: String,
    val poiTypes: String
) {

    companion object {
        fun fromSite(site: Site): SiteObservable {
            val builderPoi = StringBuilder()
            for (poi in site.poi.hwPoiTypes) {
                builderPoi.append(poi).append(" ")
            }
            val coordinated = "Lat b: " + site.location.lat + " Lng: " + site.location.lng
            return SiteObservable(
                site.siteId, site.name, site.formatAddress,
                coordinated, builderPoi.toString()
            )
        }
    }

}