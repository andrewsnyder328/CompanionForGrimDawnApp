package com.syntech.companionforgrimdawn

data class Filter(
    var ascendant: Boolean = false,
    var chaos: Boolean = false,
    var eldritch: Boolean = false,
    var order: Boolean = false,
    var primordial: Boolean = false,
    var starred: Boolean = false,
    var matchAny: Boolean = false,
    var matchRequirements: Boolean = false
) {
    fun anySet(): Boolean {
        return (ascendant
                || chaos
                || eldritch
                || order
                || primordial
                || starred)
    }
}