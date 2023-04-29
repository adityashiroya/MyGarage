package com.sokoldev.mygarage.vehicle.data


import android.os.Parcel
import android.os.Parcelable

data class MonthlyExpense(
    var carId: String? = null, var month: String? = null,var year:String?=null , var cost: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(), parcel.readString(),parcel.readString(), parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(carId)
        parcel.writeString(month)
        parcel.writeString(year)
        parcel.writeString(cost)

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MonthlyExpense> {
        override fun createFromParcel(parcel: Parcel): MonthlyExpense {
            return MonthlyExpense(parcel)
        }

        override fun newArray(size: Int): Array<MonthlyExpense?> {
            return arrayOfNulls(size)
        }
    }
}

