package com.uns.taxiflores.models

import com.beust.klaxon.*

private val klaxon = Klaxon()

data class Driver (
    val id: String? = "",
    val name: String? = "",
    val lastName: String? = "",
    val email: String? = "",
    val phone: String? = "",
    var image: String? = "",
    val plateNumber: String? = "",
    val colorCar: String? = "",
    val brandCar: String? = ""
) {
    public fun toJson() = klaxon.toJsonString(this)

    companion object {
        public fun fromJson(json: String) = klaxon.parse<Client>(json)
    }
}