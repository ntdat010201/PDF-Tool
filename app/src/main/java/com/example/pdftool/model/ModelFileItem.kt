package com.example.pdftool.model

import android.os.Parcel
import android.os.Parcelable

data class ModelFileItem(
    val id: Int = 0,
    val name: String,
    val path: String,
    val type: String,
    val lastModified: Long,
    val size: Long
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readLong(),
        parcel.readLong()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(path)
        parcel.writeString(type)
        parcel.writeLong(lastModified)
        parcel.writeLong(size)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ModelFileItem> {
        override fun createFromParcel(parcel: Parcel): ModelFileItem {
            return ModelFileItem(parcel)
        }

        override fun newArray(size: Int): Array<ModelFileItem?> {
            return arrayOfNulls(size)
        }
    }

}
