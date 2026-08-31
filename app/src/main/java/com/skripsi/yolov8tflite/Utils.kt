package com.skripsi.yolov8tflite

import android.graphics.Bitmap
import android.graphics.Color

fun calculateRipenessPercentage(bitmap: Bitmap, box: BoundingBox): Float {
    val left = (box.x1 * bitmap.width).toInt().coerceIn(0, bitmap.width - 1)
    val top = (box.y1 * bitmap.height).toInt().coerceIn(0, bitmap.height - 1)
    val right = (box.x2 * bitmap.width).toInt().coerceIn(left + 1, bitmap.width)
    val bottom = (box.y2 * bitmap.height).toInt().coerceIn(top + 1, bitmap.height)

    val croppedBitmap = Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top)

    var totalScore = 0f
    var totalRelevantPixels = 0
    val hsv = FloatArray(3)

    for (y in 0 until croppedBitmap.height) {
        for (x in 0 until croppedBitmap.width) {
            val pixel = croppedBitmap.getPixel(x, y)
            Color.colorToHSV(pixel, hsv)

            val hue = hsv[0]
            val saturation = hsv[1]
            val value = hsv[2]

            if (saturation < 0.3f || value < 0.2f) continue

            totalRelevantPixels++

            // Skor per pixel berdasarkan rentang hue
            val pixelScore = when {
                hue in 0f..15f || hue in 345f..360f -> 1.0f   // Merah pekat = matang penuh
                hue in 15f..45f -> 0.5f                        // Oranye = setengah matang
                hue in 45f..90f -> 0.0f                        // Kuning-hijau = mentah
                else -> 0.0f                                    // Hijau murni atau lainnya = mentah
            }
            totalScore += pixelScore
        }
    }

    if (totalRelevantPixels == 0) return 0f

    val ripenessPercentage = (totalScore / totalRelevantPixels) * 100f
    return ripenessPercentage.coerceIn(0f, 100f)
}