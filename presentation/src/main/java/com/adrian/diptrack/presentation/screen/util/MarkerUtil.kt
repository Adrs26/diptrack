package com.adrian.diptrack.presentation.screen.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object MarkerUtil {
    fun createBusStopMarker(context: Context, iconRes: Int): BitmapDescriptor {
        val totalSize = 80
        val outerCircleOffset = 2f
        val outerCircleRadius = 100f
        val innerCircleSize = 60f
        val innerCircleOffset = (totalSize - innerCircleSize) / 2f
        val innerCircleRadius = outerCircleRadius * 0.6f
        val pointerRadius = 8f
        val totalHeight = totalSize + pointerRadius.toInt()
        val bitmap = createBitmap(totalSize, totalHeight)
        val canvas = Canvas(bitmap)

        val whiteBackground = Paint().apply {
            color = "#F8FCFB".toColorInt()
            isAntiAlias = true
        }

        val brownBackground = Paint().apply {
            color = "#795548".toColorInt()
            isAntiAlias = true
        }

        val outerCircleRect = RectF(
            outerCircleOffset,
            outerCircleOffset,
            totalSize - outerCircleOffset,
            totalSize - outerCircleOffset
        )
        val innerCircleRect = RectF(
            innerCircleOffset,
            innerCircleOffset,
            innerCircleOffset + innerCircleSize,
            innerCircleOffset + innerCircleSize
        )

        canvas.drawRoundRect(
            outerCircleRect,
            outerCircleRadius,
            outerCircleRadius,
            brownBackground
        )
        canvas.drawRoundRect(
            innerCircleRect,
            innerCircleRadius,
            innerCircleRadius,
            whiteBackground
        )

        val pointerCenterX = totalSize / 2f
        val pointerCenterY = totalSize - 2f

        canvas.drawCircle(
            pointerCenterX,
            pointerCenterY,
            pointerRadius,
            brownBackground
        )

        val iconDrawable = ContextCompat.getDrawable(context, iconRes)

        iconDrawable?.let { icon ->
            icon.setTint("#795548".toColorInt())

            val iconSize = 48
            val centerX = totalSize / 2
            val centerY = totalSize / 2

            val iconLeft = centerX - iconSize / 2
            val iconTop = centerY - iconSize / 2
            val iconRight = iconLeft + iconSize
            val iconBottom = iconTop + iconSize

            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            icon.draw(canvas)
        }

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    fun createBusMarker(iconRes: Int): BitmapDescriptor {
        return BitmapDescriptorFactory.fromResource(iconRes)
    }

    fun offsetLatLng(
        from: LatLng,
        distanceMeters: Double,
        bearingDegrees: Double
    ): LatLng {
        val radiusEarth = 6371000.0

        val bearing = Math.toRadians(bearingDegrees)
        val lat1 = Math.toRadians(from.latitude)
        val lon1 = Math.toRadians(from.longitude)

        val lat2 = asin(sin(lat1) * cos(distanceMeters / radiusEarth) +
                cos(lat1) * sin(distanceMeters / radiusEarth) * cos(bearing)
        )

        val lon2 = lon1 + atan2(
            sin(bearing) * sin(distanceMeters / radiusEarth) * cos(lat1),
            cos(distanceMeters / radiusEarth) - sin(lat1) * sin(lat2)
        )

        return LatLng(Math.toDegrees(lat2), Math.toDegrees(lon2))
    }

    fun computeBearing(from: LatLng, to: LatLng): Float {
        val lat1 = Math.toRadians(from.latitude)
        val lon1 = Math.toRadians(from.longitude)
        val lat2 = Math.toRadians(to.latitude)
        val lon2 = Math.toRadians(to.longitude)

        val dLon = lon2 - lon1

        val y = sin(dLon) * cos(lat2)
        val x = (cos(lat1) * sin(lat2)) - (sin(lat1) * cos(lat2) * cos(dLon))

        return Math.toDegrees(atan2(y, x)).toFloat()
    }
}