package com.syntech.companionforgrimdawn

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class ConstellationAdapter(
    val resources: Resource,
    val addItemListener: (Constellation) -> Unit,
    val removeItemListener: (Constellation) -> Unit
) : RecyclerView.Adapter<ConstellationAdapter.ViewHolder>() {

    private val dataItems = mutableListOf<Constellation>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ConstellationView(parent.context))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.row.setup(dataItems[position], resources, addItemListener, removeItemListener)
    }

    override fun getItemCount(): Int = dataItems.size

    fun setItems(data: List<Constellation>) {
        this.dataItems.clear()
        this.dataItems.addAll(data)
        notifyDataSetChanged()
    }

    class ViewHolder(val row: ConstellationView) : RecyclerView.ViewHolder(row)
}