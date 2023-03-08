package com.folioreader.model.locators

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.databind.ObjectMapper
import com.folioreader.util.ObjectMapperSingleton
import org.readium.r2.shared.Locations
import org.readium.r2.shared.Locator
import org.readium.r2.shared.LocatorText

@JsonPropertyOrder("bookId", "href", "created", "locations", "readPage")
@JsonIgnoreProperties(ignoreUnknown = true)
open class ReadLocator : Locator, Parcelable {

    var bookId: String
    var readPage: Int

    @Suppress("unused") // Required for fromJSON()
    constructor() : this("", 0, "", 0, Locations())

    constructor(bookId: String, readPage: Int, href: String, created: Long, locations: Locations) :
            this(bookId, readPage, href, created, "", locations, null)

    constructor(
        bookId: String, readPage: Int, href: String, created: Long, title: String, locations: Locations,
        text: LocatorText?
    ) : super(href, created, title, locations, text) {
        this.bookId = bookId
        this.readPage = readPage
    }

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readInt()!!,
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readSerializable() as Locations,
        parcel.readSerializable() as LocatorText?
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest?.writeString(bookId)
        dest?.writeInt(readPage)
        dest?.writeString(href)
        dest?.writeLong(created)
        dest?.writeString(title)
        dest?.writeSerializable(locations)
        dest?.writeSerializable(text)
    }

    companion object {

        @JvmField
        val LOG_TAG: String = ReadLocator::class.java.simpleName

        @JvmStatic
        fun fromJson(json: String?): ReadLocator? {
            return try {
                ObjectMapperSingleton.getObjectMapper()
                    .reader()
                    .forType(ReadLocator::class.java)
                    .readValue(json)
            } catch (e: Exception) {
                Log.e(LOG_TAG, "-> ", e)
                null
            }
        }

        @JvmField
        val CREATOR = object : Parcelable.Creator<ReadLocator> {
            override fun createFromParcel(parcel: Parcel): ReadLocator {
                return ReadLocator(parcel)
            }

            override fun newArray(size: Int): Array<ReadLocator?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    fun toJson(): String? {

        return try {
            val objectMapper = ObjectMapper()
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
            objectMapper.writeValueAsString(this)
        } catch (e: Exception) {
            Log.e(LOG_TAG, "-> ", e)
            null
        }
    }
}