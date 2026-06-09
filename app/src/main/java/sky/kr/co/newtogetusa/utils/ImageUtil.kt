package sky.kr.co.newtogetusa.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import sky.kr.co.newtogetusa.data.remote.request.delivery.DeliveryRegPhoto
import java.io.ByteArrayOutputStream

object ImageUtil {
    data class EncodedImage(
        val mime: String,
        val base64: String,
        val byteSize: Int
    )

    fun uriToJpegBase64(
        context: Context,
        uri: Uri,
        maxSize: Int = 1280,
        quality: Int = 70
    ): EncodedImage? {
        val bitmap = uriToBitmap(context, uri) ?: return null
        val resized = resizeBitmap(bitmap, maxSize)
        val output = ByteArrayOutputStream()

        resized.compress(Bitmap.CompressFormat.JPEG, quality, output)
        val bytes = output.toByteArray()

        return EncodedImage(
            mime = "image/jpeg",
            base64 = Base64.encodeToString(bytes, Base64.NO_WRAP),
            byteSize = bytes.size
        )
    }

    fun uriListToPhotos(
        context: Context,
        uris: List<Uri>,
        maxSize: Int = 1024    // 긴 변 기준(px)
    ): List<DeliveryRegPhoto> {
        return uris.mapNotNull { uri ->
            val bitmap = uriToBitmap(context, uri) ?: return@mapNotNull null
            val resized = resizeBitmap(bitmap, maxSize)
            val mime = getMimeType(context, uri) ?: "image/jpeg"
            val base64 = bitmapToBase64(resized, mime)
            DeliveryRegPhoto(
                mime = mime,
                base64 = base64
            )
        }
    }

    /**
     * Uri → Bitmap
     */
    private fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    }

    /**
     * Bitmap 리사이즈 (비율 유지)
     */
    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxSize && height <= maxSize) return bitmap

        val ratio = width.toFloat() / height.toFloat()
        val (newWidth, newHeight) =
            if (ratio > 1) {
                maxSize to (maxSize / ratio).toInt()
            } else {
                (maxSize * ratio).toInt() to maxSize
            }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Bitmap → Base64 (mime에 따라 포맷 선택)
     */
    private fun bitmapToBase64(bitmap: Bitmap, mime: String): String {
        val output = ByteArrayOutputStream()

        val format = when {
            mime.contains("png") -> Bitmap.CompressFormat.PNG
            else -> Bitmap.CompressFormat.JPEG
        }

        bitmap.compress(format, 90, output) // 90% 권장
        return Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
    }

    /**
     * mime 타입 추출
     */
    private fun getMimeType(context: Context, uri: Uri): String? {
        return context.contentResolver.getType(uri)
    }
}
