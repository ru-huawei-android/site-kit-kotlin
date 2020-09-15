package com.huawei.sitekit.kotlin.common

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager

object AndroidUtils {

    private const val ANIMATION_DURATION = 300L

    fun changeFilterVisible(parent: ConstraintLayout, child: View) {
        val height = (child.layoutParams as ConstraintLayout.LayoutParams).height
        val newConstrainedHeight = if (height == 0) ConstraintSet.WRAP_CONTENT else 0

        val autoTransition = AutoTransition().apply {
            duration = ANIMATION_DURATION
        }

        TransitionManager.beginDelayedTransition(parent, autoTransition)

        ConstraintSet().apply {
            clone(parent)
            constrainHeight(child.id, newConstrainedHeight)
            applyTo(parent)
        }
    }

    fun saveToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("text", text)
        clipboard.setPrimaryClip(clip)
    }
}