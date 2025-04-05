package com.devstree.product.googlePlacesApi

import com.rapicue.patient.googlePlacesApi.PlacesDetails
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesApiInterface {
    @GET("place/queryautocomplete/json")
    fun getPlace(
        @Query("input") text: String?,
        @Query("key") key: String?
    ): retrofit2.Call<PlacesMainModel?>?

    @GET("place/details/json")
    fun getDetails(
        @Query("key") key: String?,
        @Query("place_id") place_id: String?
    ): Call<PlacesDetails>

    @GET("json")
    fun getLocation(): Call<GeoResponse?>?



}