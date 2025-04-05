package com.rapicue.patient.googlePlacesApi

data class PlacesDetails(
    val html_attributions: List<Any?>,
    val result: Result,
    val status: String
) {

    data class Result(
        val address_components: List<AddressComponents>,
        val adr_address: String? = null,
        val business_status: String? = null,
        val current_opening_hours: CurrentOpeningTime,
        val formatted_address: String? = null,
        val formatted_phone_number: String? = null,
        val geometry: LatLong? = null,
        val name: String? = null
    ) {
        data class AddressComponents(
            val long_name: String? = null,
            val short_name: String? = null,
            val types: List<String>? = null,
        )

        data class CurrentOpeningTime(
            val open_now: Boolean? = null,
        )

        data class LatLong(
            val location: locationLatLong? = null
        ) {
            data class locationLatLong(
                val lat: Double? = null,
                val lng: Double? = null,
            )
        }
    }
}