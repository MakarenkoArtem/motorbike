package com.example.bike.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Device(
    val address: String,
    val name: String
): Parcelable