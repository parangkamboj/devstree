package com.devstree.product.googlePlacesApi

import com.google.gson.annotations.SerializedName

class GeoResponse {
    @SerializedName("as")
    val as1: String? = null
    val city: String? = null
    val country: String? = null
    val countryCode: String? = null
    val isp: String? = null

    @SerializedName("lat")
    val latitude = 0.0

    @SerializedName("lon")
    val longitude = 0.0
    val org: String? = null
    val query: String? = null
    val region: String? = null
    val regionName: String? = null
    val timezone: String? = null
}