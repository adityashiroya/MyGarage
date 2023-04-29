package com.sokoldev.mygarage.vehicle.data

import android.os.Parcel
import android.os.Parcelable

data class Car(
    var id: String? = null,
    var make: String? = null,
    var model: String? = null,
    var year: String? = null,
    var color: String? = null,
    var vinNumber: String? = null,
    var drivers: String? = null,
    var insuranceProvider: String? = null,
    var supportNumber: String? = null,
    var milesDriven: String? = null,
    var gasUsed: String? = null,
    var lastOilChange: String? = null,
    var lastTireChange: String? = null,
    var alignment: String? = null,
    var balancing: String? = null,
    var accidentHistory: Accident? = null,
    var owner: String? = null,
    var image: String? = null,
    var monthlyExpenses: List<MonthlyExpense>? = null,

    ) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(Accident::class.java.classLoader),
        parcel.readString(),
        parcel.readString(),
        parcel.createTypedArrayList(MonthlyExpense.CREATOR)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(make)
        parcel.writeString(model)
        parcel.writeString(year)
        parcel.writeString(color)
        parcel.writeString(vinNumber)
        parcel.writeString(drivers)
        parcel.writeString(insuranceProvider)
        parcel.writeString(supportNumber)
        parcel.writeString(milesDriven)
        parcel.writeString(gasUsed)
        parcel.writeString(lastOilChange)
        parcel.writeString(lastTireChange)
        parcel.writeString(alignment)
        parcel.writeString(balancing)
        parcel.writeParcelable(accidentHistory, flags)
        parcel.writeString(owner)
        parcel.writeString(image)
        parcel.writeTypedList(monthlyExpenses)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Car> {
        override fun createFromParcel(parcel: Parcel): Car {
            return Car(parcel)
        }

        override fun newArray(size: Int): Array<Car?> {
            return arrayOfNulls(size)
        }
    }
}

