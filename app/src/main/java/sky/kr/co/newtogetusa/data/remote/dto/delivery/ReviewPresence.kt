package sky.kr.co.newtogetusa.data.remote.dto.delivery

import android.os.Parcelable
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReviewPresence(
    val exists: Boolean = true
) : Parcelable

class ReviewPresenceAdapter : TypeAdapter<ReviewPresence>() {
    override fun write(out: JsonWriter, value: ReviewPresence?) {
        if (value == null || !value.exists) {
            out.nullValue()
        } else {
            out.value("Y")
        }
    }

    override fun read(reader: JsonReader): ReviewPresence? {
        return when (reader.peek()) {
            JsonToken.NULL -> {
                reader.nextNull()
                null
            }

            JsonToken.BEGIN_OBJECT -> {
                reader.skipValue()
                ReviewPresence()
            }

            JsonToken.STRING -> {
                val value = reader.nextString()
                if (value.isBlank()) null else ReviewPresence()
            }

            JsonToken.NUMBER -> {
                reader.skipValue()
                ReviewPresence()
            }

            JsonToken.BOOLEAN -> {
                val value = reader.nextBoolean()
                if (value) ReviewPresence() else null
            }

            else -> {
                reader.skipValue()
                null
            }
        }
    }
}
