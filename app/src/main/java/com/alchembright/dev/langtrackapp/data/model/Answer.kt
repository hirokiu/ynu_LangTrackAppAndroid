package com.alchembright.dev.langtrackapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Answer(
    var type: String = "",
    var index: Int = 0,
    var likertAnswer: Int? = null,
    var fillBlankAnswer: Int? = null,
    var multipleChoiceAnswer: MutableList<Int>? = null,
    var singleMultipleAnswer: Int? = null,
    var openEndedAnswer: String? = null,
    var timeDurationAnswer: Int? = null,
    var sliderScaleAnswer: Int? = null
    ): Parcelable