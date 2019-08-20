package com.syntech.companionforgrimdawn

data class Constellation(
    val name: String,
    val points: Int,
    val requirements: Resource,
    val rewards: Resource,
    val imageSrc: String,
    var selected: Boolean,
    val description: String
) {

    fun isAvailable(resources: Resource): Boolean {
        return resources.ascendant >= requirements.ascendant
                && resources.chaos >= requirements.chaos
                && resources.eldritch >= requirements.eldritch
                && resources.order >= requirements.order
                && resources.primordial >= requirements.primordial
    }

}