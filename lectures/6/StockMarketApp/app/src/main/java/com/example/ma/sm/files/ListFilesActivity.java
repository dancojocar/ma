package com.example.ma.sm.files;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ma.sm.R;

import java.io.File;

public class ListFilesActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_list_files);

    loadContent();

  }

  private void loadContent() {
    TextView tv = findViewById(R.id.files);
    tv.setText(getFilesFromDir(getFilesDir()));

    TextView tvCaches = findViewById(R.id.caches);
    tvCaches.setText(getFilesFromDir(getCacheDir()));

    TextView tvExternals = findViewById(R.id.externalFiles);

    String state = Environment.getExternalStorageState();
    if (Environment.MEDIA_MOUNTED.equals(state)) {
      tvExternals.setText(getFilesFromDir(
          Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)));
    } else
      tvExternals.setText("No external storage mounted!");
  }

  public String getFilesFromDir(File filesFromSD) {

    StringBuilder builder = new StringBuilder();

    File listAllFiles[] = filesFromSD.listFiles();

    if (listAllFiles != null && listAllFiles.length > 0) {
      for (File currentFile : listAllFiles) {
        if (currentFile.isDirectory()) {
          getFilesFromDir(currentFile);
        } else {
          builder.append(currentFile.getAbsolutePath()).append("\n");
        }
      }
    }
    return builder.toString();
  }

  public void clearAllFiles(View view) {
    for (String file : fileList())
      deleteFile(file);
    Toast.makeText(getApplication(), "All files were deleted!", Toast.LENGTH_SHORT).show();
    loadContent();
  }

  public void clearAllCaches(View view) {
    for (File file : getCacheDir().listFiles())
      file.delete();
    Toast.makeText(getApplication(), "All caches are deleted!", Toast.LENGTH_SHORT).show();
    loadContent();
  }
}
