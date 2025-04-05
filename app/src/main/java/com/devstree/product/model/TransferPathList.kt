package com.devstree.product.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class TransferPathList(var list : List<PathData>): Parcelable
