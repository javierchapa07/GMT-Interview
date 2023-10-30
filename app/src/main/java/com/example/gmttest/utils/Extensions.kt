package com.example.gmttest.utils

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaScannerConnection
import android.os.Environment
import android.provider.MediaStore
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.core.graphics.withRotation
import com.example.gmttest.models.Point
import java.io.File
import java.io.OutputStream
import kotlin.math.atan2
import kotlin.math.hypot

fun Canvas.myDrawPath(path: Array<Point>, paint: Paint) {
    for (index in 0 until path.size - 1) { drawLine(path[index].x, path[index].y, path[index + 1].x, path[index + 1].y, paint) }
}

fun MutableList<Point>.smooth(): MutableList<Point> {
    if (size < 2) return this
    val points = mutableListOf<Point>()
    points.add(first())
    for (i in 1 until size - 1) points.add((this[i - 1] + this[i] + this[i + 1]) / 3f)
    points.add(last())
    return points
}

fun Array<Point>.totalDistance(): Float {
    var totalDistance = 0.0f
    for (index in 0 until size - 1) totalDistance += hypot(this[index + 1].x - this[index].x, this[index + 1].y - this[index].y)
    return totalDistance
}

fun Canvas.myDrawTextOnPath(text: String, path: Array<Point>, paint: Paint) {
    val step = path.totalDistance() / text.length
    for (textIndex in text.indices) {
        val indexDistance = textIndex * step
        var segmentDistance = 0.0f
        for (pathIndex in 0 until path.size - 1) {
            val segment =  path[pathIndex + 1] - path[pathIndex]
            val hypotenuse = hypot(segment.x, segment.y)
            if (segmentDistance + hypotenuse > indexDistance) {
                val angle = Math.toDegrees(atan2(segment.y.toDouble(), segment.x.toDouble()))
                val position = path[pathIndex] + (segment * ((indexDistance - segmentDistance) / hypotenuse))
                withRotation(angle.toFloat(), position.x, position.y) {drawText(text[textIndex].toString(), position.x, position.y, paint) }
                break
            }
            segmentDistance += hypotenuse
        }
    }
}

fun Bitmap.getOppositeAverageColor(): Int {
    var redSum = 0
    var greenSum = 0
    var blueSum = 0
    val width = width
    val height = height
    val totalPixels = width * height
    for (x in 0 until width) {
        for (y in 0 until height) {
            val pixel = getPixel(x, y)
            redSum += pixel shr 16 and 0xFF
            greenSum += pixel shr 8 and 0xFF
            blueSum += pixel and 0xFF
        }
    }
    val averageRed = redSum / totalPixels
    val averageGreen = greenSum / totalPixels
    val averageBlue = blueSum / totalPixels
    val averageColor = (0xFF shl 24) or (averageRed shl 16) or (averageGreen shl 8) or averageBlue
    return Color.rgb(255 - averageColor.red, 255 - averageColor.green, 255 - averageColor.blue)
}

fun Bitmap.saveToGallery(context: Context, title: String): Boolean {
    return try {
        ContentValues().put(MediaStore.Images.Media.DISPLAY_NAME, title)
        ContentValues().put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        ContentValues().put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "MGTTest")
        val imageUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues())
        if (imageUri != null) {
            val outputStream: OutputStream? = context.contentResolver.openOutputStream(imageUri)
            if (outputStream != null) {
                compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()
                MediaScannerConnection.scanFile(context, arrayOf(File(imageUri.path!!).absolutePath),null,null)
                true
            } else false
        } else false
    } catch(_: Exception) { false }
}

fun AppCompatActivity.showKeyboard() {
    val imm: InputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(currentFocus, 0)
}

fun AppCompatActivity.hideKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
}