package com.syntech.companionforgrimdawn

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_constellation.view.*

class ConstellationAdapter(
    private val resources: Resource,
    private val listener: IConstellationView
) : RecyclerView.Adapter<ConstellationAdapter.ViewHolder>() {

    private val dataItems = mutableListOf<Constellation>()
    private var enforceRules = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ConstellationView(parent.context))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.row.cb_starred.setOnClickListener {
            listener.onItemStarred(dataItems[position])
        }
        holder.row.setup(
            dataItems[position],
            resources,
            listener,
            enforceRules
        )
    }

    override fun getItemCount(): Int = dataItems.size

    fun setItems(data: List<Constellation>) {
        this.dataItems.clear()
        this.dataItems.addAll(data)
        notifyDataSetChanged()
    }

    fun updateEnforceRules(enforceRules: Boolean) {
        this.enforceRules = enforceRules
    }

    class ViewHolder(val row: ConstellationView) : RecyclerView.ViewHolder(row)
}