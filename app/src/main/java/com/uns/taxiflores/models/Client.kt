package com.uns.taxiflores.models

import com.beust.klaxon.*

private val klaxon = Klaxon()

data class Client (
    val id: String? = "",
    val name: String? = "",
    val lastName: String? = "",
    val email: String? = "",
    val phone: String? = "",
    val image: String? = ""
) {
    public fun toJson() = klaxon.toJsonString(this)

    companion object {
        public fun fromJson(json: String) = klaxon.parse<Client>(json)
    }
}
