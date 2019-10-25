package com.syntech.companionforgrimdawn

interface IConstellationView {
    fun onAddItemClicked(item: Constellation)
    fun onRemoveItemClicked(item: Constellation)
    fun onItemStarred(item: Constellation)
}