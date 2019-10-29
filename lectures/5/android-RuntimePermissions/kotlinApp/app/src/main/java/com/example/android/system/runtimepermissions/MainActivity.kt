/*
* Copyright 2017 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.example.android.system.runtimepermissions

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.android.system.runtimepermissions.camera.CameraPreviewFragment
import com.example.android.system.runtimepermissions.contacts.ContactsFragment
import com.example.android.system.runtimepermissions.extensions.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    if (savedInstanceState == null) {
      supportFragmentManager.beginTransaction().apply {
        replace(R.id.sampleContentFragment, RuntimePermissionsFragment())
      }.commit()
    }
  }

  /**
   * Called when the 'show camera' button is clicked.
   * Callback is defined in resource layout definition.
   */
  @Suppress("UNUSED_PARAMETER")
  fun showCamera(view: View) {
    Log.i(TAG, "Show camera button pressed. Checking permission.")
    // Check if the Camera permission is already available.
    if (!isPermissionGranted(Manifest.permission.CAMERA)) {
      // Camera permission has not been granted.
      requestCameraPermission()
    } else {
      // Camera permissions is already available, show the camera preview.
      Log.i(TAG,
          "CAMERA permission has already been granted. Displaying camera preview.")
      showCameraPreview()
    }
  }

  /**
   * Requests the Camera permission.
   * If the permission has been denied previously, a SnackBar will prompt the user to grant the
   * permission, otherwise it is requested directly.
   */
  private fun requestCameraPermission() {
    Log.i(TAG, "CAMERA permission has NOT been granted. Requesting permission.")
    if (shouldShowPermissionRationale(Manifest.permission.CAMERA)) {
      // Provide an additional rationale to the user if the permission was not granted
      // and the user would benefit from additional context for the use of the permission.
      // For example if the user has previously denied the permission.
      Log.i(TAG, "Displaying camera permission rationale to provide additional context.")
      Snackbar.make(mainLayout, R.string.permission_camera_rationale,
          Snackbar.LENGTH_INDEFINITE)
          .setAction(R.string.ok) {
            requestPermission(Manifest.permission.CAMERA, REQUEST_CAMERA)
          }
          .show()
    } else {

      // Camera permission has not been granted yet. Request it directly.
      requestPermission(Manifest.permission.CAMERA, REQUEST_CAMERA)
    }
  }

  /**
   * Called when the 'show camera' button is clicked.
   * Callback is defined in resource layout definition.
   */
  @Suppress("UNUSED_PARAMETER")
  fun showContacts(view: View) {
    Log.i(TAG, "Show contacts button pressed. Checking permissions.")

    // Verify that all required contact permissions have been granted.
    if (!isPermissionGranted(Manifest.permission.READ_CONTACTS) ||
        !isPermissionGranted(Manifest.permission.WRITE_CONTACTS)) {
      // Contacts permissions have not been granted.
      Log.i(TAG, "Contact permissions has NOT been granted. Requesting permissions.")
      requestContactsPermissions()
    } else {
      // Contact permissions have been granted. Show the contacts fragment.
      Log.i(TAG,
          "Contact permissions have already been granted. Displaying contact details.")
      showContactDetails()
    }
  }

  /**
   * Requests the Contacts permissions.
   * If the permission has been denied previously, a Snackbar will prompt the user to grant the
   * permission, otherwise it is requested directly.
   */
  private fun requestContactsPermissions() {
    if (shouldShowPermissionRationale(Manifest.permission.READ_CONTACTS) ||
        shouldShowPermissionRationale(Manifest.permission.WRITE_CONTACTS)) {

      // Provide an additional rationale to the user if the permission was not granted
      // and the user would benefit from additional context for the use of the permission.
      // For example, if the request has been denied previously.
      Log.i(TAG, "Displaying contacts permission rationale to provide additional context.")

      // Display a SnackBar with an explanation and a button to trigger the request.
      Snackbar.make(mainLayout, R.string.permission_contacts_rationale,
          Snackbar.LENGTH_INDEFINITE)
          .setAction(R.string.ok) {
            batchRequestPermissions(PERMISSIONS_CONTACT, REQUEST_CONTACTS)
          }
          .show()
    } else {
      // Contact permissions have not been granted yet. Request them directly.
      batchRequestPermissions(PERMISSIONS_CONTACT, REQUEST_CONTACTS)
    }
  }

  /**
   * Display the [CameraPreviewFragment] in the content area if the required Camera
   * permission has been granted.
   */
  private fun showCameraPreview() {
    supportFragmentManager.beginTransaction()
        .replace(R.id.sampleContentFragment, CameraPreviewFragment.newInstance())
        .addToBackStack("contacts")
        .commit()
  }

  /**
   * Display the [ContactsFragment] in the content area if the required contacts
   * permissions have been granted.
   */
  private fun showContactDetails() {
    supportFragmentManager.beginTransaction()
        .replace(R.id.sampleContentFragment, ContactsFragment.newInstance())
        .addToBackStack("contacts")
        .commit()
  }

  /**
   * Callback received when a permissions request has been completed.
   */
  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                          grantResults: IntArray) {

    if (requestCode == REQUEST_CAMERA) {
      // Received permission result for camera permission.
      Log.i(TAG, "Received response for Camera permission request.")

      // Check if the permission has been granted
      if (grantResults.containsOnly(PackageManager.PERMISSION_GRANTED)) {
        // Camera permission has been granted, preview can be displayed
        Log.i(TAG, "CAMERA permission has now been granted. Showing preview.")
        Snackbar.make(mainLayout, R.string.permision_available_camera,
            Snackbar.LENGTH_SHORT).show()
      } else {
        Log.i(TAG, "CAMERA permission was NOT granted.")
        Snackbar.make(mainLayout, R.string.permissions_not_granted,
            Snackbar.LENGTH_SHORT).show()

      }
    } else if (requestCode == REQUEST_CONTACTS) {
      Log.i(TAG, "Received response for contact permissions request.")

      // We have requested multiple permissions for contacts, so all of them need to be
      // checked.
      if (grantResults.containsOnly(PackageManager.PERMISSION_GRANTED)) {
        // All required permissions have been granted, display contacts fragment.
        Snackbar.make(mainLayout, R.string.permision_available_contacts,
            Snackbar.LENGTH_SHORT)
            .show()
      } else {
        Log.i(TAG, "Contacts permissions were NOT granted.")
        Snackbar.make(mainLayout, R.string.permissions_not_granted,
            Snackbar.LENGTH_SHORT)
            .show()
      }

    } else {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
  }

  @Suppress("UNUSED_PARAMETER")
  fun onBackClick(view: View) {
    supportFragmentManager.popBackStack()
  }

  companion object {

    const val TAG = "MainActivity"
    /**
     * Id to identify a camera permission request.
     */
    const val REQUEST_CAMERA = 0
    /**
     * Id to identify a contacts permission request.
     */
    const val REQUEST_CONTACTS = 1
    /**
     * Permissions required to read and write contacts. Used by the [ContactsFragment].
     */
    val PERMISSIONS_CONTACT = arrayOf(Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_CONTACTS)
  }
}