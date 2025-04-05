package com.devstree.product.model

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
@Entity(tableName = "AddressSuggestModel")
data class AddressSuggestModel(
    @PrimaryKey(autoGenerate = true) val id : Int,
    @ColumnInfo(name = "address")
    val address: String? = null,
    @ColumnInfo(name = "addressTwo")
    val addressTwo: String? = null,
    @ColumnInfo(name = "city")
    val city: String? = null,
    @ColumnInfo(name = "country")
    val country: String? = null,
    @ColumnInfo(name = "state")
    val state: String? = null,
    @ColumnInfo(name = "latitude")
    val latitude: Double? = null,
    @ColumnInfo(name = "longitude")
    val longitude: Double? = null,
    @ColumnInfo(name = "placeName")
    val placeName: String? = null,
    @ColumnInfo(name = "distance")
    var distance: Double? = null,
): Parcelable
