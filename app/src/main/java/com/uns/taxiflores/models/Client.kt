package com.uns.taxiflores.models

import com.beust.klaxon.*

private val klaxon = Klaxon()

data class Client (
    var id: String? = "",
    val name: String? = "",
    val lastName: String? = "",
    val email: String? = "",
    val phone: String? = "",
    var image: String? = ""
) {
    public fun toJson() = klaxon.toJsonString(this)

    companion object {
        public fun fromJson(json: String) = klaxon.parse<Client>(json)
    }
}
