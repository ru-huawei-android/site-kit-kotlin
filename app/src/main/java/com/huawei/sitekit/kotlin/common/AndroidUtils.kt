package com.huawei.sitekit.kotlin.common

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

object AndroidUtils {


    fun saveToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("text", text)
        clipboard.setPrimaryClip(clip)
    }
}