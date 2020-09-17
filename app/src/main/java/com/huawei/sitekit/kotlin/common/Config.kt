package com.huawei.sitekit.kotlin.common

import android.content.Context
import android.net.Uri
import com.huawei.agconnect.config.AGConnectServicesConfig

object Config {

    const val DEFAULT_LOCATION_TYPE = "ALL"
    const val DEFAULT_COUNTRY_CODE = "Us"
    const val DEFAULT_LANGUAGE = "en"
    const val DEFAULT_PAGE_COUNT = 10

    private const val API_KEY_PATH = "client/api_key"

    fun getAgcApiKey(context: Context?): String {
        return Uri.encode(AGConnectServicesConfig.fromContext(context).getString(API_KEY_PATH))
    }
}