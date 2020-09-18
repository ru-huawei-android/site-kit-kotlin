package com.huawei.sitekit.kotlin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.huawei.sitekit.kotlin.R
import com.huawei.sitekit.kotlin.adapter.SiteAdapter.SiteViewHolder
import kotlinx.android.synthetic.main.item_site.view.*

class SiteAdapter : RecyclerView.Adapter<SiteViewHolder>() {

    interface SiteCallback {
        fun onSiteItemClicked(observable: SiteObservable)
    }

    private var observables: List<SiteObservable> = arrayListOf()
    private var callback: SiteCallback? = null

    fun setList(observables: List<SiteObservable>) {
        this.observables = observables
        notifyDataSetChanged()
    }

    fun setCallback(callback: SiteCallback) {
        this.callback = callback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SiteViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_site, parent, false)
        return SiteViewHolder(view)
    }

    override fun onBindViewHolder(holder: SiteViewHolder, position: Int) {
        holder.bind(observables[position])
    }

    override fun getItemCount() = observables.size

    inner class SiteViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        fun bind(observable: SiteObservable) {
            itemView.textViewItem.text = String.format(
                "${adapterPosition + 1}. ${observable.siteId}\nName: ${observable.name}\n" +
                "Location: ${observable.location}\n${observable.coordinates}\n" +
                "POI types: ${observable.poiTypes}"
            )
            itemView.setOnClickListener {
                callback?.onSiteItemClicked(observable)
            }
        }

    }
}