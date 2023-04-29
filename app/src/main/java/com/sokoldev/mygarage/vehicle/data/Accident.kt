package com.sokoldev.mygarage.vehicle.data

import android.os.Parcel
import android.os.Parcelable

data class Accident(
    var history: String? = null,
    var image: String? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(history)
        parcel.writeString(image)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Accident> {
        override fun createFromParcel(parcel: Parcel): Accident {
            return Accident(parcel)
        }

        override fun newArray(size: Int): Array<Accident?> {
            return arrayOfNulls(size)
        }
    }
}

