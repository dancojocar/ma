package ro.cojocar.dan.fileprovider

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.ShareCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.view.View
import java.io.*


class MainActivity : AppCompatActivity() {

    private val SHARED_PROVIDER_AUTHORITY = BuildConfig.APPLICATION_ID
    private val SHARED_FOLDER = "myimages"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    @Throws(IOException::class)
    fun share(view: View) {
        // Create a random image and save it in private app folder
        val sharedFile = createFile()

        // Get the shared file's Uri
        val uri = FileProvider.getUriForFile(this, SHARED_PROVIDER_AUTHORITY, sharedFile)
        logd("file uri: $uri")

        // Create a intent
        val intentBuilder = ShareCompat.IntentBuilder.from(this)
            .setType("image/*")
            .addStream(uri)

        // Start the intent
        val chooserIntent = intentBuilder.createChooserIntent()
        startActivity(chooserIntent)
    }

    @Throws(IOException::class)
    private fun createFile(): File {
        val bitmapFactory = RandomBitmapFactory()
        val randomBitmap = bitmapFactory.createRandomBitmap()

        val sharedFolder = File(filesDir, SHARED_FOLDER)
        sharedFolder.mkdirs()

        val sharedFile = File.createTempFile("picture", ".png", sharedFolder)
        sharedFile.createNewFile()

        writeBitmap(sharedFile, randomBitmap)
        return sharedFile
    }

    private fun writeBitmap(destination: File, bitmap: Bitmap) {
        var outputStream: FileOutputStream? = null
        try {
            outputStream = FileOutputStream(destination)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } finally {
            close(outputStream)
        }
    }

    private fun close(closeable: Closeable?) {
        if (closeable == null) return
        try {
            closeable.close()
        } catch (ignored: IOException) {
            loge("Unable to close", ignored)
        }
    }
}
