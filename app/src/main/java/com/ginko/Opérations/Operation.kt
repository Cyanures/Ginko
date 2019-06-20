package com.ginko.Opérations

import android.os.Parcel
import android.os.Parcelable

data class Operation(var idOperation:Int, var libelleOperation:String, var montantOperation:Double, var idCompte:Int) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readDouble(),
        parcel.readInt()
    ) {
    }

    constructor(libelleOperation: String, montantOperation: Double, idCompte: Int) : this(-1,libelleOperation,montantOperation,idCompte)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(idOperation)
        parcel.writeString(libelleOperation)
        parcel.writeDouble(montantOperation)
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

