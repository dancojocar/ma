/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.contentproviderpaging

import android.content.*
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import java.io.File
import java.io.IOException
import kotlin.math.min

/**
 * ContentProvider that demonstrates how the paging support works introduced in Android O.
 * This class fetches the images from the local storage but the storage could be
 * other locations such as a remote server.
 */
class ImageProvider : ContentProvider() {

  private lateinit var mBaseDir: File

  override fun onCreate(): Boolean {
    logd("onCreate")

    val context = context ?: return false
    mBaseDir = context.filesDir
    writeDummyFilesToStorage(context)

    return true
  }

  override fun query(uri: Uri, strings: Array<String>?, s: String?,
                     strings1: Array<String>?, s1: String?): Cursor? {
    throw UnsupportedOperationException()
  }

  override fun query(uri: Uri, projection: Array<out String>?, queryArgs: Bundle?,
                     cancellationSignal: CancellationSignal?): Cursor? {
    // We only support a query for multiple images, return null for other form of queries
    // including a query for a single image.
    if (sUriMatcher.match(uri) != IMAGES) {
      return null
    }
    val result = MatrixCursor(resolveDocumentProjection(projection))

    val files = mBaseDir.listFiles()
    val offset = queryArgs?.getInt(ContentResolver.QUERY_ARG_OFFSET, 0)
    val limit = queryArgs?.getInt(ContentResolver.QUERY_ARG_LIMIT, Integer.MAX_VALUE)
    logd("queryChildDocuments with Bundle, Uri: " +
        uri + ", offset: " + offset + ", limit: " + limit)
    require(files != null) { "Missing the files" }
    require(!(offset == null || offset < 0)) { "Offset must not be less than 0" }
    require(!(limit == null || limit < 0)) { "Limit must not be less than 0" }

    if (offset >= files.size) {
      return result
    }

    val maxIndex = min(offset + limit, files.size)
    for (i in offset..maxIndex) {
      includeFile(result, files[i])
    }

    val bundle = constructExtras(queryArgs, files)
    result.extras = bundle
    return result
  }

  private fun constructExtras(queryArgs: Bundle, files: Array<File>): Bundle {
    val bundle = Bundle()
    bundle.putInt(ContentResolver.EXTRA_TOTAL_COUNT, files.size)
    var size = 0
    if (queryArgs.containsKey(ContentResolver.QUERY_ARG_OFFSET)) {
      size++
    }
    if (queryArgs.containsKey(ContentResolver.QUERY_ARG_LIMIT)) {
      size++
    }
    if (size > 0) {
      val honoredArgs = arrayOfNulls<String>(size)
      var index = 0
      if (queryArgs.containsKey(ContentResolver.QUERY_ARG_OFFSET)) {
        honoredArgs[index++] = ContentResolver.QUERY_ARG_OFFSET
      }
      if (queryArgs.containsKey(ContentResolver.QUERY_ARG_LIMIT)) {
        honoredArgs[index] = ContentResolver.QUERY_ARG_LIMIT
      }
      bundle.putStringArray(ContentResolver.EXTRA_HONORED_ARGS, honoredArgs)
    }
    return bundle
  }

  override fun getType(uri: Uri): String? =
      when (sUriMatcher.match(uri)) {
        IMAGES -> "vnd.android.cursor.dir/images"
        IMAGE_ID -> "vnd.android.cursor.item/images"
        else -> throw IllegalArgumentException(String.format("Unknown URI: %s", uri))
      }

  override fun insert(uri: Uri, contentValues: ContentValues?): Uri? {
    throw UnsupportedOperationException()
  }

  override fun delete(uri: Uri, s: String?, strings: Array<String>?): Int {
    throw UnsupportedOperationException()
  }

  override fun update(uri: Uri, contentValues: ContentValues?, s: String?,
                      strings: Array<String>?): Int {
    throw UnsupportedOperationException()
  }

  /**
   * Add a representation of a file to a cursor.

   * @param result the cursor to modify
   * *
   * @param file   the File object representing the desired file (may be null if given docID)
   */
  private fun includeFile(result: MatrixCursor, file: File) {
    val row = result.newRow()
    row.add(ImageContract.Columns.DISPLAY_NAME, file.name)
    row.add(ImageContract.Columns.SIZE, file.length())
    row.add(ImageContract.Columns.ABSOLUTE_PATH, file.absolutePath)
  }

  /**
   * Preload sample files packaged in the apk into the internal storage directory.  This is a
   * dummy function specific to this demo.  The MyCloud mock cloud service doesn't actually
   * have a backend, so it simulates by reading content from the device's internal storage.
   */
  private fun writeDummyFilesToStorage(context: Context) {
    val list = mBaseDir.list()
    if (list != null && list.isNotEmpty()) {
      return
    }

    val imageResIds = getResourceIdArray(context)
    for (i in 0 until REPEAT_COUNT_WRITE_FILES) {
      for (resId in imageResIds) {
        writeFileToInternalStorage(context, resId, "-$i.jpeg")
      }
    }
  }

  /**
   * Write a file to internal storage.  Used to set up our dummy "cloud server".

   * @param context   the Context
   * *
   * @param resId     the resource ID of the file to write to internal storage
   * *
   * @param extension the file extension (ex. .png, .mp3)
   */
  private fun writeFileToInternalStorage(context: Context, resId: Int, extension: String) {
    val ins = context.resources.openRawResource(resId)
    val buffer = ByteArray(1024)
    try {
      val filename = context.resources.getResourceEntryName(resId) + extension
      val fos = context.openFileOutput(filename, Context.MODE_PRIVATE)
      while (true) {
        val size = ins.read(buffer, 0, 1024)
        if (size < 0) break
        fos.write(buffer, 0, size)
      }
      ins.close()
      fos.write(buffer)
      fos.close()
    } catch (e: IOException) {
      throw RuntimeException(e)
    }

  }

  private fun getResourceIdArray(context: Context): IntArray {
    val ar = context.resources.obtainTypedArray(R.array.image_res_ids)
    val len = ar.length()
    val resIds = IntArray(len)
    for (i in 0 until len) {
      resIds[i] = ar.getResourceId(i, 0)
    }
    ar.recycle()
    return resIds
  }

  companion object {

    private const val IMAGES = 1

    private const val IMAGE_ID = 2

    private val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH)

    init {
      sUriMatcher.addURI(ImageContract.AUTHORITY, "images", IMAGES)
      sUriMatcher.addURI(ImageContract.AUTHORITY, "images/#", IMAGE_ID)
    }

    // Indicated how many same images are going to be written as dummy images
    private const val REPEAT_COUNT_WRITE_FILES = 10

    private fun resolveDocumentProjection(projection: Array<out String>?): Array<out String> {
      return projection ?: ImageContract.PROJECTION_ALL
    }
  }
}
