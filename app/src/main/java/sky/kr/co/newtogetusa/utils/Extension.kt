package sky.kr.co.newtogetusa.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun DialogFragment.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(requireContext(), message, duration).show()
}

fun dialogFragmentShow(fm: FragmentManager, fragment: Fragment) {
    fm.beginTransaction().add(fragment, "").commitAllowingStateLoss()
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
fun ImageView.loadImage(
    url: String?,
    @DrawableRes placeholder: Int? = null,
    @DrawableRes error: Int? = null,
    roundedCorner : Int? = null,
    crossFade: Boolean = false,
    isCircle: Boolean = false
) {
    Glide.with(context)
        .load(url)
        .apply {
            roundedCorner?.let {
                transform(MultiTransformation(FitCenter(), RoundedCorners(roundedCorner)))
            }
            placeholder?.let { placeholderId ->
                placeholder(placeholderId)
            }
            error?.let { errorId ->
                error(errorId)
            }
            if(isCircle){
                circleCrop()
            }
            if (crossFade) {
                transition(DrawableTransitionOptions.withCrossFade())
            }
        }
        .into(this)
}