@file:JvmName("WorkerUtils")

package com.example.background.workers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.background.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID


private const val TAG = "WorkerUtils"

fun makeStatusNotification(message: String, context: Context) {
    // Create the NotificationChannel, but only on API 26+ (Oreo) because it is not available in lower versions.
    val name = VERBOSE_NOTIFICATION_CHANNEL_NAME
    val description = VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION
    val importance = NotificationManager.IMPORTANCE_HIGH
    val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
        this.description = description
    }

    // Add the channel to the system
    val notificationManager = context.getSystemService(NotificationManager::class.java)
    notificationManager?.createNotificationChannel(channel)

    // Create the notification
    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(NOTIFICATION_TITLE)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setVibrate(LongArray(0))

    // Check for notification permissions (Post notifications permission for Android 13+)
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
    } else {
        Log.w(TAG, "Notification permissions not granted.")
    }
}

fun sleep() {
    try {
        Thread.sleep(DELAY_TIME_MILLIS, 0)
    } catch (e: InterruptedException) {
        Log.e(TAG, e.message.toString())
    }
}


@WorkerThread
fun blurBitmap(bitmap: Bitmap, context: Context): Bitmap {
    val rs = RenderScript.create(context)
    val input = Allocation.createFromBitmap(rs, bitmap)
    val output = Allocation.createTyped(rs, input.type)
    val scriptIntrinsicBlur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
    scriptIntrinsicBlur.setInput(input)
    scriptIntrinsicBlur.setRadius(10f) // Set blur radius
    scriptIntrinsicBlur.forEach(output)
    output.copyTo(bitmap)
    Log.i("BlurWorker", "Blur bitmap succeeded")
    return bitmap
}

@Throws(FileNotFoundException::class)
fun writeBitmapToFile(applicationContext: Context, bitmap: Bitmap): Uri {
    val name = "blur-filter-output-${UUID.randomUUID()}.png"
    val outputDir = File(applicationContext.filesDir, OUTPUT_PATH)
    if (!outputDir.exists()) {
        outputDir.mkdirs() // Ensure output directory exists
    }
    val outputFile = File(outputDir, name)
    var out: FileOutputStream? = null
    try {
        out = FileOutputStream(outputFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100 /* Maximum quality */, out)
    } finally {
        out?.let {
            try {
                it.close()
            } catch (ignore: IOException) {
            }
        }
    }
    return Uri.fromFile(outputFile)
}
