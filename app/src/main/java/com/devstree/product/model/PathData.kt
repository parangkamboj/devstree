package com.devstree.product.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class PathData(
    var lat : Double? = null,
    var long: Double? = null,
    var name : String? = null
): Parcelable
