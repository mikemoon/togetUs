package sky.kr.co.newtogetusa.utils

import android.content.Context
import android.net.Uri
import com.bumptech.glide.Glide
import java.io.File

object CacheCleanup {

    fun clearGlideCache(context: Context) {
        val appContext = context.applicationContext
        Glide.get(appContext).clearMemory()
        Thread {
            Glide.get(appContext).clearDiskCache()
        }.start()
    }

    fun deleteCacheUri(context: Context, uri: Uri) {
        if (uri.scheme != "file") return
        uri.path?.let(::File)?.let { deleteCacheFile(context, it) }
    }

    fun deleteCacheUris(context: Context, uris: Collection<Uri>) {
        uris.forEach { deleteCacheUri(context, it) }
    }

    fun deleteCacheFile(context: Context, file: File) {
        val cachePath = context.cacheDir.canonicalPath
        if (file.canonicalPath.startsWith(cachePath)) {
            file.delete()
        }
    }
}
