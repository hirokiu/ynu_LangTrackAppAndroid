package com.alchembright.dev.langtrackapp.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import com.alchembright.dev.langtrackapp.util.toDate
import java.util.*

@Parcelize
data class Assignment(
    var survey: Survey,
    var updatedAt: String,
    var createdAt: String,
    var userId: String,
    var dataset: Dataset?,
    var publishAt: String,
    var expireAt: String,
    var id: String
) : Parcelable{

    fun isActive(): Boolean{

        val assignmentDate = this.expireAt.toDate() ?: Date(Date().time + 1000)
        var isActive: Boolean
        isActive = this.dataset == null
        if (isActive){
            isActive = assignmentDate.time > Date().time
        }
        return isActive
    }
}

/*
* var survey = Survey()
    var updatedAt = ""
    var createdAt = ""
    var userId = ""
    var dataset: Dataset? = nil
    var published = ""
    var expiry = ""
    var id = ""

    func timeLeftToExpiryInMilli() -> Int64{
        let now = Date()
        let exp = DateParser.getDate(dateString: expiry) ?? now
        let millisecondsLeft = exp.millisecondsSince1970 - now.millisecondsSince1970
        if millisecondsLeft <= 0{
            return 0
        }else{
            return millisecondsLeft
        }
    }
    * */