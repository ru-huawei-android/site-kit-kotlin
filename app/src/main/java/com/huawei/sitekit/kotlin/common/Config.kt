package com.huawei.sitekit.kotlin.common

import android.content.Context
import android.net.Uri
import com.huawei.agconnect.config.AGConnectServicesConfig

object Config {
    fun getAgcApiKey(context: Context?): String {
        return Uri.encode(AGConnectServicesConfig.fromContext(context).getString("client/api_key"))
    }
}