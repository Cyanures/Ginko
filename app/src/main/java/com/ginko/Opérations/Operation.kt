package com.ginko.Opérations

import android.os.Parcel
import android.os.Parcelable
import java.text.DateFormat
import java.util.*

data class Operation(
    var idOperation: Int,
    var libelleOperation: String,
    var montantOperation: Double,
    var dateOperation: Long,
    var idCompte: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readDouble(),
        parcel.readLong(),
        parcel.readInt()
    ) {
    }

    constructor(libelleOperation: String, montantOperation: Double, dateOperation: Long, idCompte: Int) : this(
        -1,
        libelleOperation,
        montantOperation,
        dateOperation,
        idCompte
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(idOperation)
        parcel.writeString(libelleOperation)
        parcel.writeDouble(montantOperation)
        parcel.writeLong(dateOperation)
        parcel.writeInt(idCompte)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Operation> {
        override fun createFromParcel(parcel: Parcel): Operation {
            return Operation(parcel)
        }

        override fun newArray(size: Int): Array<Operation?> {
            return arrayOfNulls(size)
        }
    }
}

