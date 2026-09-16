package com.alchembright.dev.langtrackapp.data.model

import android.os.Parcel
import android.os.Parcelable

class IncludeIf (
    var ifIndex: Int = 0,
    var ifValue: Int = 0
    ): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(ifIndex)
        parcel.writeInt(ifValue)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<IncludeIf> {
        override fun createFromParcel(parcel: Parcel): IncludeIf {
            return IncludeIf(parcel)
        }

        override fun newArray(size: Int): Array<IncludeIf?> {
            return arrayOfNulls(size)
        }
    }
}