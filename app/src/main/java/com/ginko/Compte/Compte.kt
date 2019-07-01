package com.ginko.Compte

import android.os.Parcel
import android.os.Parcelable

data class Compte(var idCompte: Int, var nomCompte: String, var solde: Double, var includedInBalance: Int) :
    Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readDouble(),
        parcel.readInt()
    ) {
    }


    constructor(nomCompte: String, solde: Double, includedInBalance: Int) : this(
        -1,
        nomCompte,
        solde,
        includedInBalance
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(idCompte)
        parcel.writeString(nomCompte)
        parcel.writeDouble(solde)
        parcel.writeInt(includedInBalance)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Compte> {
        override fun createFromParcel(parcel: Parcel): Compte {
            return Compte(parcel)
        }

        override fun newArray(size: Int): Array<Compte?> {
            return arrayOfNulls(size)
        }
    }
}