package com.example.ma.sm.location;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ma.sm.R;
import com.example.ma.sm.fragments.BaseActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;


public class LocationDemo extends BaseActivity
    implements GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, LocationListener,
    EasyPermissions.PermissionCallbacks,
    OnMapReadyCallback {


  static final int SETTINGS_SCREEN = 1000;
  static final int REQUEST_ACCESS_FINE_LOCATION = 1001;


  @BindView(R.id.latitude)
  TextView textLatitude;
  @BindView(R.id.longitude)
  TextView textLongitude;
  @BindView(R.id.altitude)
  TextView textAltitude;
  GoogleApiClient client;
  LocationRequest request;

  GoogleMap map;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_location_demo);
    ButterKnife.bind(this);

    client = new GoogleApiClient.Builder(this)
        .addApi(LocationServices.API)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .build();

    SupportMapFragment mapFragment =
        (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);
  }

  @Override
  protected void onStart() {
    super.onStart();
    client.connect();
    Timber.v("onStart");
  }

  @Override
  protected void onStop() {
    //See: https://github.com/googlesamples/android-play-location/issues/26
    client.unregisterConnectionCallbacks(this);
    client.unregisterConnectionFailedListener(this);
    if (client.isConnected())
      LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
    client.disconnect();
    super.onStop();
    Timber.v("onStop");
  }


  public void onConnected(@Nullable Bundle bundle) {
    Timber.v("onConnected");
    request = LocationRequest.create();
//    request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY); // 100m accuracy
    request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // gps accuracy
//    request.setPriority(LocationRequest.PRIORITY_LOW_POWER); // 10km - city level accuracy
//    request.setPriority(LocationRequest.PRIORITY_NO_POWER); //best accuracy without power consumption
    request.setInterval(1000); //update the location every second, more frequent will increase battery consumption


    String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
    if (EasyPermissions.hasPermissions(this, perms)) {
      // Already have permission, do the thing
      whenAllowed();
    } else {
      // Do not have permissions, request them now
      EasyPermissions.requestPermissions(this, "Fine access location is need to retrieve the gps coordinates",
          REQUEST_ACCESS_FINE_LOCATION, perms);
    }
  }


  @AfterPermissionGranted(REQUEST_ACCESS_FINE_LOCATION)
  private void whenAllowed() {
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      return;
    }
    LocationServices.FusedLocationApi.requestLocationUpdates(client, request, this);
  }

  @Override
  public void onConnectionSuspended(int i) {
    Timber.v("onConnectionSuspended");

  }

  @Override
  public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    Timber.v("onConnectionFailed");

  }

  @Override
  public void onLocationChanged(Location location) {
    Timber.v("onLocationChanged");
    double latitude = location.getLatitude();
    textLatitude.setText(Double.toString(latitude));
    double longitude = location.getLongitude();
    textLongitude.setText(Double.toString(longitude));
    textAltitude.setText(Double.toString(location.getAltitude()));
    if (map != null)
      map.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("Marker"));
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    // Forward results to EasyPermissions
    EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
  }

  @Override
  public void onPermissionsGranted(int requestCode, List<String> perms) {
    Timber.v("onPermissionsGranted");
  }

  @Override
  public void onPermissionsDenied(int requestCode, List<String> perms) {
    Timber.v("onPermissionsDenied: %d:%d", requestCode, perms.size());

    // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
    // This will display a dialog directing them to enable the permission in app settings.
    if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
      new AppSettingsDialog.Builder(this, "This app really need to have the fine access permission")
          .setTitle("Access fine permission")
          .setPositiveButton("OK")
          .setNegativeButton("Cancel", null /* click listener */)
          .setRequestCode(SETTINGS_SCREEN)
          .build()
          .show();
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == SETTINGS_SCREEN) {
      // Do something after user returned from app settings screen, like showing a Toast.
      Toast.makeText(this, "returned form settings", Toast.LENGTH_SHORT)
          .show();
    }
  }

  @Override
  public void onMapReady(GoogleMap googleMap) {
    map = googleMap;
  }
}
