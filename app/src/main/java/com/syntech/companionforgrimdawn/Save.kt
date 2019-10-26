package com.syntech.companionforgrimdawn

data class Save(
    val resources: Resource,
    val devotions: MutableList<Constellation>,
    val filter: Filter,
    val pathHistory: String,
    val name: String? = null
)