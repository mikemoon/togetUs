package sky.kr.co.newtogetusa.utils

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.UUID

object FileUtil {

    fun copyUriToTempFile(context: Context, uri: Uri): String {
        val resolver = context.contentResolver
        val input = resolver.openInputStream(uri) ?: error("InputStream null")

        val tempFile = File.createTempFile(
            "delivery_${System.currentTimeMillis()}_${UUID.randomUUID()}",
            ".jpg",
            context.cacheDir
        )

        tempFile.outputStream().use { output ->
            input.copyTo(output)
        }
        input.close()

        return tempFile.absolutePath
    }

    fun createFilePart(
        partName: String,
        file: File
    ): MultipartBody.Part {

        val requestFile =
            file.asRequestBody("multipart/form-data".toMediaType())

        return MultipartBody.Part.createFormData(
            partName,
            file.name,
            requestFile
        )
    }

    fun createImagePart(
        file: File
    ): MultipartBody.Part {

        val requestFile =
            file.asRequestBody("image/*".toMediaType())

        return MultipartBody.Part.createFormData(
            "file",   // 서버에서 요구하는 파트 이름
            file.name,
            requestFile
        )
    }

    fun uriToFile(context: Context, uri: Uri): File {

        val contentResolver = context.contentResolver
        val fileName = getFileNameFromUri(context, uri)

        val tempFile = File(
            context.cacheDir,
            "upload_${System.currentTimeMillis()}_$fileName"
        )

        contentResolver.openInputStream(uri)?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return tempFile
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String {
        var name = "unknown_file"

        runCatching {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (it.moveToFirst() && nameIndex >= 0) {
                    name = it.getString(nameIndex)
                }
            }
        }.onFailure { e -> e.printStackTrace() }
        return name
    }
}
