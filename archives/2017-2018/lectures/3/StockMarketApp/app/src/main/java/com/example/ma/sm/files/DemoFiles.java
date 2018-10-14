package com.example.ma.sm.files;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DemoFiles {
  public void demo(Context ctx) throws IOException {
    String FILENAME = "hello_file";
    String string = "hello world!";
    // MODE_APPEND, MODE_WORLD_READABLE, and MODE_WORLD_WRITEABLE.
    FileOutputStream fos = ctx.openFileOutput(FILENAME, Context.MODE_PRIVATE);
    fos.write(string.getBytes());
    fos.close();

/* Other useful commands:
 - getFilesDir() - Gets the absolute path to the filesystem directory where your internal files are saved.
 - getDir() - Creates (or opens an existing) directory within your internal storage space.
 - deleteFile() - Deletes a file saved on the internal storage.
 - fileList() - Returns an array of files currently saved by your application.
 */
  }

  public void externalDemo() {
    /*
    <manifest ...>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    ...
    </manifest>
     */
    if (isExternalStorageWritable()) {
      File externalStorageDirectory = Environment.getExternalStorageDirectory();
      //...
    } else if (isExternalStorageReadable()) {

      // ...
    } else {
      // ...
    }
  }

  /* Checks if external storage is available for read and write */
  private boolean isExternalStorageWritable() {
    String state = Environment.getExternalStorageState();
    return Environment.MEDIA_MOUNTED.equals(state);
  }

  /* Checks if external storage is available to at least read */
  private boolean isExternalStorageReadable() {
    String state = Environment.getExternalStorageState();
    return Environment.MEDIA_MOUNTED.equals(state) ||
        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
  }
}
