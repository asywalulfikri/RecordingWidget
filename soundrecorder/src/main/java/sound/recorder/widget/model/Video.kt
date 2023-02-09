package sound.recorder.widget.model

import android.os.Parcel
import android.os.Parcelable

class Video : Parcelable {
    var datepublish: String? = null
    var description: String? = null
    var thumbnail: String? = null
    var title: String? = null
    var url: String? = null

    constructor() {}
    constructor(`in`: Parcel) {
        datepublish = `in`.readString()
        description = `in`.readString()
        thumbnail = `in`.readString()
        title = `in`.readString()
        url = `in`.readString()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        out.writeString(datepublish)
        out.writeString(description)
        out.writeString(thumbnail)
        out.writeString(title)
        out.writeString(url)
    }

    companion object {
        val CREATOR: Parcelable.Creator<Video?> =
            object : Parcelable.Creator<Video?> {
                override fun createFromParcel(`in`: Parcel): Video? {
                    return Video(`in`)
                }

                override fun newArray(size: Int): Array<Video?> {
                    return arrayOfNulls(size)
                }
            }
    }
}