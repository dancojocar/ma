package ro.cojocar.dan.fileprovider

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import java.util.*


internal class RandomBitmapFactory {

    companion object {
        private const val BITMAP_SIZE = 256
        private const val MAX_STROKE_WIDTH = 20f
        private const val STROKE_COUNT = 50
    }

    fun createRandomBitmap(): Bitmap {
        val rnd = Random(System.currentTimeMillis())

        val paint = Paint(ANTI_ALIAS_FLAG)
        val bitmap = Bitmap.createBitmap(BITMAP_SIZE, BITMAP_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        for (i in 0 until STROKE_COUNT) {
            val startX = rnd.nextInt(BITMAP_SIZE).toFloat()
            val startY = rnd.nextInt(BITMAP_SIZE).toFloat()
            val stopX = rnd.nextInt(BITMAP_SIZE).toFloat()
            val stopY = rnd.nextInt(BITMAP_SIZE).toFloat()
            val width = rnd.nextFloat() * MAX_STROKE_WIDTH
            val color = rnd.nextInt()

            paint.color = color
            paint.strokeWidth = width

            canvas.drawRect(startX, startY, stopX, stopY, paint)
        }

        return bitmap
    }

}