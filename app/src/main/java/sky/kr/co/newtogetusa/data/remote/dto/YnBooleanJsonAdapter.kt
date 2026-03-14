package sky.kr.co.newtogetusa.data.remote.dto

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

class YnBooleanJsonAdapter : TypeAdapter<Boolean>() {

    override fun write(out: JsonWriter, value: Boolean?) {
        if (value == null) {
            out.nullValue()
            return
        }
        out.value(value)
    }

    override fun read(reader: JsonReader): Boolean {
        return when (reader.peek()) {
            JsonToken.BOOLEAN -> reader.nextBoolean()
            JsonToken.STRING -> {
                when (reader.nextString().trim().uppercase()) {
                    "Y", "YES", "TRUE", "1" -> true
                    else -> false
                }
            }

            JsonToken.NUMBER -> reader.nextInt() != 0
            JsonToken.NULL -> {
                reader.nextNull()
                false
            }

            else -> {
                reader.skipValue()
                false
            }
        }
    }
}