package com.alchembright.dev.langtrackapp.data.model

import android.os.Parcel
import android.os.Parcelable

data class SkipLogic (
    var ifChosen: Int = -99,
    var goto: Int = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(ifChosen)
        parcel.writeInt(goto)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SkipLogic> {
        override fun createFromParcel(parcel: Parcel): SkipLogic {
            return SkipLogic(parcel)
        }

        override fun newArray(size: Int): Array<SkipLogic?> {
            return arrayOfNulls(size)
        }
    }
}