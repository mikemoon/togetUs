package sky.kr.co.newtogetusa.utils

import android.graphics.Color
import android.location.Location
import sky.kr.co.newtogetusa.data.remote.dto.kakao.KakaoNaviDirectionsResponse
import java.text.NumberFormat
import java.util.Locale

object MapUtil {

    /** 두 좌표 사이 거리(m 단위) */
    fun distanceMeters(
        fromLat: Double, fromLng: Double,
        toLat: Double, toLng: Double
    ): Float {
        val result = FloatArray(1)
        Location.distanceBetween(fromLat, fromLng, toLat, toLng, result)
        return result[0]
    }

    /** 1000m 미만: "850 m", 이상: "1.2 km" */
    fun formatDistance(meters: Float): String {
        val nf = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
            maximumFractionDigits = 1
            minimumFractionDigits = 0
        }
        return if (meters >= 1000f) "${nf.format(meters / 1000f)} km"
        else "${nf.format(meters)} m"
    }

    fun drawRouteOnKakaoMap(
        kakaoMap: com.kakao.vectormap.KakaoMap,
        points: List<com.kakao.vectormap.LatLng>,
        summary: KakaoNaviDirectionsResponse.Summary?,
        moveCamera: Boolean = true,
        clearPrevious: Boolean = false,
        animate: Boolean = false,
        animationDurationMillis: Int = 1_000
    ) {
        if (points.isEmpty()) return

        val routeLineManager = kakaoMap.routeLineManager ?: return
        if (clearPrevious) {
            routeLineManager.getAnimator(ROUTE_PROGRESS_ANIMATOR_ID)?.stop()
            routeLineManager.getLayer()?.removeAll()
        }

        val baseStylesSet = com.kakao.vectormap.route.RouteLineStylesSet.from(
            com.kakao.vectormap.route.RouteLineStyles.from(
                com.kakao.vectormap.route.RouteLineStyle.from(16f, Color.parseColor(ROUTE_BASE_COLOR))
            )
        )
        val progressStylesSet = com.kakao.vectormap.route.RouteLineStylesSet.from(
            com.kakao.vectormap.route.RouteLineStyles.from(
                com.kakao.vectormap.route.RouteLineStyle.from(16f, Color.parseColor(ROUTE_PROGRESS_COLOR))
            )
        )
        val baseSegment = com.kakao.vectormap.route.RouteLineSegment.from(points, baseStylesSet.getStyles(0))
        val progressSegment = com.kakao.vectormap.route.RouteLineSegment.from(points, progressStylesSet.getStyles(0))
        val layer = routeLineManager.getLayer() ?: routeLineManager.addLayer()

        if (animate) {
            layer?.addRouteLine(
                com.kakao.vectormap.route.RouteLineOptions.from(baseSegment)
                    .setStylesSet(baseStylesSet)
                    .setVisible(true)
                    .setZOrder(0)
            )
        }

        val routeLine = layer?.addRouteLine(
            com.kakao.vectormap.route.RouteLineOptions.from(progressSegment)
                .setStylesSet(progressStylesSet)
                .setVisible(!animate)
                .setZOrder(1)
        )
        if (animate && routeLine != null) {
            routeLine.setProgress(0f)
            routeLine.show()
            routeLineManager.getAnimator(ROUTE_PROGRESS_ANIMATOR_ID)?.stop()
            val animation = com.kakao.vectormap.route.animation.ProgressAnimation
                .from(ROUTE_PROGRESS_ANIMATOR_ID, animationDurationMillis)
                .setProgressType(com.kakao.vectormap.route.animation.ProgressType.ToShow)
                .setProgressDirection(com.kakao.vectormap.route.animation.ProgressDirection.StartFirst)
                .setInterpolation(com.kakao.vectormap.animation.Interpolation.CubicOut)
                .setHideAtStop(false)
                .setResetToInitialState(false)
            routeLineManager.addAnimator(animation)
                .apply { addRouteLines(routeLine) }
                .start()
        }
        // RouteLine 사용 가이드 참고. :contentReference[oaicite:6]{index=6}

        // 4) 카메라 경로 맞추기
        if (!moveCamera) return

        //   a) API summary.bound 사용 (x=lng,y=lat)
        summary?.bound?.let { b ->
            val ne = com.kakao.vectormap.LatLng.from(b.max_y, b.max_x)
            val sw = com.kakao.vectormap.LatLng.from(b.min_y, b.min_x)
            val bounds = com.kakao.vectormap.LatLngBounds.Builder()
                .include(ne).include(sw).build()
            kakaoMap.moveCamera(
                com.kakao.vectormap.camera.CameraUpdateFactory.fitMapPoints(bounds, 60)
            )
            return
        }
        //   b) summary가 없으면 points로 bounds 계산
        val builder = com.kakao.vectormap.LatLngBounds.Builder()
        points.forEach { builder.include(it) }
        kakaoMap.moveCamera(
            com.kakao.vectormap.camera.CameraUpdateFactory.fitMapPoints(builder.build(), 60)
        )
        // fitMapPoints 사용 안내(DevTalk) :contentReference[oaicite:7]{index=7}
    }

    private const val ROUTE_PROGRESS_ANIMATOR_ID = "delivery_route_progress"
    private const val ROUTE_BASE_COLOR = "#999999"
    private const val ROUTE_PROGRESS_COLOR = "#4F78FF"
}
