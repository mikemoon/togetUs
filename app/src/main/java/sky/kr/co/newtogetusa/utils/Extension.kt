package sky.kr.co.newtogetusa.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Base64
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import sky.kr.co.newtogetusa.R
import sky.kr.co.newtogetusa.ui.dialog.loading.LoadingDialogFragment
import java.io.ByteArrayOutputStream

fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun DialogFragment.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(requireContext(), message, duration).show()
}

fun dialogFragmentShow(fm: FragmentManager, fragment: Fragment) {
    fm.beginTransaction().add(fragment, "").commitAllowingStateLoss()
}

fun Context.showKeyboard(view: View) {
    val inputMethodManager =
        getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager =
        getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Int.dpToPx(): Int {
    return (this * Resources.getSystem().displayMetrics.density).toInt()
}

fun Float.dpToPx(): Float{
    return (this * Resources.getSystem().displayMetrics.density)
}

fun Fragment.isStorageWritePermissionsGranted(): Boolean {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}

fun Fragment.showLoading() {
    if (childFragmentManager.findFragmentByTag(LoadingDialogFragment.TAG) == null) {
        LoadingDialogFragment().show(
            childFragmentManager,
            LoadingDialogFragment.TAG
        )
    }
}

fun Fragment.hideLoading() {
    (childFragmentManager.findFragmentByTag(LoadingDialogFragment.TAG) as? DialogFragment)
        ?.dismissAllowingStateLoss()
}

fun Context.downloadUrlWithDownloadManager(url: String, fileName: String) {
    try {
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(url)
        val request = DownloadManager.Request(uri).apply {
            setTitle("파일 다운로드")
            setDescription("다운로드 : $fileName")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE
            )
        }

        downloadManager.enqueue(request)

        toast("파일 다운로드가 시작되었습니다.")
    }catch (e: Exception){
        e.printStackTrace()
    }
}

@SuppressLint("CheckResult")
fun ImageView.loadProfile(src: String?, radiusDp: Float = 40f, cacheKey: Any? = null,          // ex) user.updatedAt, user.revision 등
                          noCache: Boolean = false        // 강제 캐시 미사용 옵션
     ) {
    val radiusPx = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, radiusDp, resources.displayMetrics
    ).toInt()

    val reqOpts = RequestOptions()
        .transform(CenterCrop(), RoundedCorners(radiusPx))

    val req = Glide.with(context)
        .load(src)
        .apply(reqOpts)
        .placeholder(R.drawable.profile)
        .error(R.drawable.profile)

    if (noCache) {
        // 정말 캐시를 완전히 끄고 싶을 때만 사용
        req.skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
    } else if (cacheKey != null) {
        // 캐시 버전 키 적용 (값이 바뀔 때만 새로 받아옴)
        req.signature(ObjectKey(cacheKey))
    }

    req.into(this)
}

@SuppressLint("CheckResult")
fun ImageView.loadProfile(
    src: Bitmap,
    radiusDp: Float = 40f,
    noCache: Boolean = false
) {
    val radiusPx = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, radiusDp, resources.displayMetrics
    ).toInt()

    val reqOpts = RequestOptions()
        .transform(CenterCrop(), RoundedCorners(radiusPx))

    val req = Glide.with(context)
        .load(src)
        .apply(reqOpts)
        .placeholder(R.drawable.profile)
        .error(R.drawable.profile)

    if (noCache) {
        req.skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
    }

    req.into(this)
}


@SuppressLint("CheckResult")
fun ImageView.loadImage(
    url: String?,
    @DrawableRes placeholder: Int? = null,
    @DrawableRes error: Int? = null,
    roundedCorner : Int? = null,
    crossFade: Boolean = false,
    isCircle: Boolean = false
) {
    val requestOptions = RequestOptions().apply {
        placeholder?.let { placeholder(it) }
        error?.let { error(it) }

        if (isCircle) {
            circleCrop()
        } else if (roundedCorner != null) {
            transform(MultiTransformation(FitCenter(), RoundedCorners(roundedCorner)))
        }
    }

    Glide.with(context)
        .load(url)
        .apply(requestOptions)
        .also {
            if (crossFade) {
                it.transition(DrawableTransitionOptions.withCrossFade())
            }
        }
        .into(this)
}

fun bitmapToBase64(bitmap: Bitmap): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.NO_WRAP)
}

fun base64ToBitmap(base64Str: String): Bitmap? {
    return try {
        val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: IllegalArgumentException) {
        e.printStackTrace()
        null
    }
}

fun resizeImageUri(context: Context, uri: Uri, maxSize: Int = 1024): Bitmap? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        val width = originalBitmap.width
        val height = originalBitmap.height
        val scale = maxSize.toFloat() / maxOf(width, height)

        if (scale >= 1f) {
            originalBitmap // 이미 작은 경우 원본 그대로
        } else {
            val newWidth = (width * scale).toInt()
            val newHeight = (height * scale).toInt()
            Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}