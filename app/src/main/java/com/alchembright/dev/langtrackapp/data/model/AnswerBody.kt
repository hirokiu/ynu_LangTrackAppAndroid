package com.alchembright.dev.langtrackapp.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AnswerBody(
    var index: Int,
    var type: String,
    var intValue: Int?,
    var multiValue: MutableList<Int>?,
    var stringValue: String?
) : Parcelable